package backend.academy.linktracker.bot.client.grpc;

import backend.academy.linktracker.bot.client.ScrapperGateway;
import backend.academy.linktracker.contracts.dto.mapper.ScrapperGrpcMapper;
import backend.academy.linktracker.contracts.dto.request.CommonAddLinkRequest;
import backend.academy.linktracker.contracts.dto.request.CommonRemoveLinkRequest;
import backend.academy.linktracker.contracts.dto.response.CommonListLinksResponse;
import backend.academy.linktracker.contracts.exception.ChatAlreadyExistsException;
import backend.academy.linktracker.contracts.exception.ChatNotFoundException;
import backend.academy.linktracker.contracts.exception.LinkAlreadyTrackedException;
import backend.academy.linktracker.generated.grpc.ScrapperServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "scrapper", name = "transport", havingValue = "grpc")
public class GrpcScrapperClient implements ScrapperGateway {

    private final ScrapperServiceGrpc.ScrapperServiceBlockingStub stub;

    public GrpcScrapperClient(@Qualifier("scrapperManagedChannel") ManagedChannel managedChannel) {
        this.stub = ScrapperServiceGrpc.newBlockingStub(managedChannel);
    }

    @Override
    public void registerChat(long chatId) {
        try {
            stub.registerChat(ScrapperGrpcMapper.toGrpcChatRequest(chatId));
        } catch (StatusRuntimeException e) {
            throw switch (e.getStatus().getCode()) {
                case INVALID_ARGUMENT ->
                    new IllegalArgumentException(e.getStatus().getDescription(), e);
                case ALREADY_EXISTS -> new ChatAlreadyExistsException();
                default -> e;
            };
        }
    }

    @Override
    public void deleteChat(long chatId) {
        try {
            stub.deleteChat(ScrapperGrpcMapper.toGrpcChatRequest(chatId));
        } catch (StatusRuntimeException e) {
            throw switch (e.getStatus().getCode()) {
                case INVALID_ARGUMENT ->
                    new IllegalArgumentException(e.getStatus().getDescription(), e);
                case NOT_FOUND -> new ChatNotFoundException();
                default -> e;
            };
        }
    }

    @Override
    public void addLink(long chatId, CommonAddLinkRequest commonAddLinkRequest) {
        try {
            stub.addLink(ScrapperGrpcMapper.toGrpcAddLinkCommand(chatId, commonAddLinkRequest));
        } catch (StatusRuntimeException e) {
            throw switch (e.getStatus().getCode()) {
                case INVALID_ARGUMENT ->
                    new IllegalArgumentException(e.getStatus().getDescription(), e);
                case NOT_FOUND -> new ChatNotFoundException("Не найден чат: " + chatId);
                case ALREADY_EXISTS -> new LinkAlreadyTrackedException();
                default -> e;
            };
        }
    }

    @Override
    public void deleteLink(long chatId, CommonRemoveLinkRequest commonRemoveLinkRequest) {
        try {
            stub.deleteLink(ScrapperGrpcMapper.toGrpcDeleteLinkCommand(chatId, commonRemoveLinkRequest));
        } catch (StatusRuntimeException e) {
            throw switch (e.getStatus().getCode()) {
                case INVALID_ARGUMENT ->
                    new IllegalArgumentException(e.getStatus().getDescription(), e);
                case NOT_FOUND -> new ChatNotFoundException();
                default -> e;
            };
        }
    }

    @Override
    public CommonListLinksResponse getLinks(long chatId) {
        try {
            return ScrapperGrpcMapper.fromGrpcListLinksResponse(
                    stub.getLinks(ScrapperGrpcMapper.toGrpcChatRequest(chatId)));
        } catch (StatusRuntimeException e) {
            throw switch (e.getStatus().getCode()) {
                case INVALID_ARGUMENT ->
                    new IllegalArgumentException(e.getStatus().getDescription(), e);
                case NOT_FOUND -> new ChatNotFoundException();
                default -> e;
            };
        }
    }
}
