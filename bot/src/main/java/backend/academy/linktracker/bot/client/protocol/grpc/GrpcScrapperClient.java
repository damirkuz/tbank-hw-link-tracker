package backend.academy.linktracker.bot.client.protocol.grpc;

import backend.academy.linktracker.bot.client.protocol.ScrapperGateway;
import backend.academy.linktracker.contracts.dto.mapper.ScrapperGrpcMapper;
import backend.academy.linktracker.contracts.dto.request.CommonAddLinkRequest;
import backend.academy.linktracker.contracts.dto.request.CommonRemoveLinkRequest;
import backend.academy.linktracker.contracts.dto.response.CommonListLinksResponse;
import backend.academy.linktracker.contracts.exception.ChatAlreadyExistsException;
import backend.academy.linktracker.contracts.exception.ChatNotFoundException;
import backend.academy.linktracker.contracts.exception.LinkAlreadyTrackedException;
import backend.academy.linktracker.generated.grpc.ScrapperServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "scrapper", name = "transport", havingValue = "grpc")
public class GrpcScrapperClient implements ScrapperGateway {

    private final ScrapperServiceGrpc.ScrapperServiceBlockingStub stub;

    public GrpcScrapperClient(@Qualifier("scrapperManagedChannel") ManagedChannel managedChannel) {
        this.stub = ScrapperServiceGrpc.newBlockingStub(managedChannel);
    }

    @Override
    public void registerChat(long chatId) throws ChatAlreadyExistsException {
        try {
            stub.registerChat(ScrapperGrpcMapper.toGrpcChatRequest(chatId));
        } catch (StatusRuntimeException e) {
            throw mapRegisterChatException(chatId, e);
        }
    }

    @Override
    public void deleteChat(long chatId) {
        try {
            stub.deleteChat(ScrapperGrpcMapper.toGrpcChatRequest(chatId));
        } catch (StatusRuntimeException e) {
            throw mapDeleteChatException(chatId, e);
        }
    }

    @Override
    public void addLink(long chatId, CommonAddLinkRequest request)
            throws ChatNotFoundException, LinkAlreadyTrackedException {
        try {
            stub.addLink(ScrapperGrpcMapper.toGrpcAddLinkCommand(chatId, request));
        } catch (StatusRuntimeException e) {
            throw mapAddLinkException(chatId, request, e);
        }
    }

    @Override
    public void deleteLink(long chatId, CommonRemoveLinkRequest request) throws ChatNotFoundException {
        try {
            stub.deleteLink(ScrapperGrpcMapper.toGrpcDeleteLinkCommand(chatId, request));
        } catch (StatusRuntimeException e) {
            throw mapDeleteLinkException(chatId, request, e);
        }
    }

    @Override
    public CommonListLinksResponse getLinks(long chatId) throws ChatNotFoundException {
        try {
            return ScrapperGrpcMapper.fromGrpcListLinksResponse(
                    stub.getLinks(ScrapperGrpcMapper.toGrpcChatRequest(chatId)));
        } catch (StatusRuntimeException e) {
            throw mapGetLinksException(chatId, e);
        }
    }

    private RuntimeException mapRegisterChatException(long chatId, StatusRuntimeException e)
            throws ChatAlreadyExistsException {
        return switch (e.getStatus().getCode()) {
            case INVALID_ARGUMENT -> {
                logExpected("registerChat", chatId, null, e);
                yield new IllegalArgumentException(e.getStatus().getDescription(), e);
            }
            case ALREADY_EXISTS -> {
                logExpected("registerChat", chatId, null, e);
                throw new ChatAlreadyExistsException();
            }
            default -> {
                logUnexpected("registerChat", chatId, null, e);
                yield e;
            }
        };
    }

    private RuntimeException mapDeleteChatException(long chatId, StatusRuntimeException e)
            throws ChatNotFoundException {
        return switch (e.getStatus().getCode()) {
            case INVALID_ARGUMENT -> {
                logExpected("deleteChat", chatId, null, e);
                yield new IllegalArgumentException(e.getStatus().getDescription(), e);
            }
            case NOT_FOUND -> {
                logExpected("deleteChat", chatId, null, e);
                throw new ChatNotFoundException();
            }
            default -> {
                logUnexpected("deleteChat", chatId, null, e);
                yield e;
            }
        };
    }

    private RuntimeException mapAddLinkException(long chatId, CommonAddLinkRequest request, StatusRuntimeException e)
            throws ChatNotFoundException, LinkAlreadyTrackedException {
        URI link = request != null ? request.uri() : null;

        return switch (e.getStatus().getCode()) {
            case INVALID_ARGUMENT -> {
                logExpected("addLink", chatId, link, e);
                yield new IllegalArgumentException(e.getStatus().getDescription(), e);
            }
            case NOT_FOUND -> {
                logExpected("addLink", chatId, link, e);
                throw new ChatNotFoundException("Не найден чат: " + chatId);
            }
            case ALREADY_EXISTS -> {
                logExpected("addLink", chatId, link, e);
                throw new LinkAlreadyTrackedException();
            }
            default -> {
                logUnexpected("addLink", chatId, link, e);
                yield e;
            }
        };
    }

    private RuntimeException mapDeleteLinkException(
            long chatId, CommonRemoveLinkRequest request, StatusRuntimeException e) throws ChatNotFoundException {
        URI link = request != null ? request.uri() : null;

        return switch (e.getStatus().getCode()) {
            case INVALID_ARGUMENT -> {
                logExpected("deleteLink", chatId, link, e);
                yield new IllegalArgumentException(e.getStatus().getDescription(), e);
            }
            case NOT_FOUND -> {
                logExpected("deleteLink", chatId, link, e);
                throw new ChatNotFoundException();
            }
            default -> {
                logUnexpected("deleteLink", chatId, link, e);
                yield e;
            }
        };
    }

    private RuntimeException mapGetLinksException(long chatId, StatusRuntimeException e) throws ChatNotFoundException {
        return switch (e.getStatus().getCode()) {
            case INVALID_ARGUMENT -> {
                logExpected("getLinks", chatId, null, e);
                yield new IllegalArgumentException(e.getStatus().getDescription(), e);
            }
            case NOT_FOUND -> {
                logExpected("getLinks", chatId, null, e);
                throw new ChatNotFoundException();
            }
            default -> {
                logUnexpected("getLinks", chatId, null, e);
                yield e;
            }
        };
    }

    private void logExpected(String operation, long chatId, URI link, StatusRuntimeException e) {
        Status status = e.getStatus();

        log.atWarn()
                .addKeyValue("client", "scrapper")
                .addKeyValue("transport", "grpc")
                .addKeyValue("operation", operation)
                .addKeyValue("chat_id", chatId)
                .addKeyValue("link", link)
                .addKeyValue("grpc_code", status.getCode())
                .addKeyValue("grpc_description", status.getDescription())
                .log("Ожидаемая ошибка при вызове scrapper");
    }

    private void logUnexpected(String operation, long chatId, URI link, StatusRuntimeException e) {
        Status status = e.getStatus();

        log.atError()
                .setCause(e)
                .addKeyValue("client", "scrapper")
                .addKeyValue("transport", "grpc")
                .addKeyValue("operation", operation)
                .addKeyValue("chat_id", chatId)
                .addKeyValue("link", link)
                .addKeyValue("grpc_code", status.getCode())
                .addKeyValue("grpc_description", status.getDescription())
                .log("Неожиданная ошибка при вызове scrapper");
    }
}
