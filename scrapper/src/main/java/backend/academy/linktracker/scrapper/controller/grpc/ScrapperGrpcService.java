package backend.academy.linktracker.scrapper.controller.grpc;

import backend.academy.linktracker.contracts.dto.mapper.ScrapperGrpcMapper;
import backend.academy.linktracker.contracts.exception.ChatAlreadyExistsException;
import backend.academy.linktracker.contracts.exception.ChatNotFoundException;
import backend.academy.linktracker.contracts.exception.LinkAlreadyTrackedException;
import backend.academy.linktracker.contracts.exception.LinkNotFoundException;
import backend.academy.linktracker.generated.grpc.GrpcAddLinkCommand;
import backend.academy.linktracker.generated.grpc.GrpcChatRequest;
import backend.academy.linktracker.generated.grpc.GrpcDeleteLinkCommand;
import backend.academy.linktracker.generated.grpc.GrpcListLinksResponse;
import backend.academy.linktracker.generated.grpc.ScrapperServiceGrpc;
import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "bot", name = "transport", havingValue = "grpc")
public class ScrapperGrpcService extends ScrapperServiceGrpc.ScrapperServiceImplBase {

    private final ScrapperGrpcHandler scrapperGrpcHandler;

    @Override
    public void registerChat(GrpcChatRequest request, StreamObserver<Empty> responseObserver) {
        String operation = "registerChat";
        long chatId = request.getChatId();

        logRequest(operation, chatId, null);

        try {
            scrapperGrpcHandler.registerChat(chatId);
            completeEmpty(responseObserver);
            logSuccess(operation, chatId, null, null);
        } catch (Exception e) {
            handleError(operation, chatId, null, e, responseObserver);
        }
    }

    @Override
    public void deleteChat(GrpcChatRequest request, StreamObserver<Empty> responseObserver) {
        String operation = "deleteChat";
        long chatId = request.getChatId();

        logRequest(operation, chatId, null);

        try {
            scrapperGrpcHandler.deleteChat(chatId);
            completeEmpty(responseObserver);
            logSuccess(operation, chatId, null, null);
        } catch (Exception e) {
            handleError(operation, chatId, null, e, responseObserver);
        }
    }

    @Override
    public void addLink(GrpcAddLinkCommand request, StreamObserver<Empty> responseObserver) {
        String operation = "addLink";
        long chatId = request.getChatId();
        String link = request.getRequest().getUri();

        logRequest(operation, chatId, link);

        try {
            scrapperGrpcHandler.addLink(chatId, ScrapperGrpcMapper.fromGrpcAddLinkCommand(request));
            completeEmpty(responseObserver);
            logSuccess(operation, chatId, link, null);
        } catch (Exception e) {
            handleError(operation, chatId, link, e, responseObserver);
        }
    }

    @Override
    public void deleteLink(GrpcDeleteLinkCommand request, StreamObserver<Empty> responseObserver) {
        String operation = "deleteLink";
        long chatId = request.getChatId();
        String link = request.getRequest().getUri();

        logRequest(operation, chatId, link);

        try {
            scrapperGrpcHandler.deleteLink(chatId, ScrapperGrpcMapper.fromGrpcDeleteLinkCommand(request));
            completeEmpty(responseObserver);
            logSuccess(operation, chatId, link, null);
        } catch (Exception e) {
            handleError(operation, chatId, link, e, responseObserver);
        }
    }

    @Override
    public void getLinks(GrpcChatRequest request, StreamObserver<GrpcListLinksResponse> responseObserver) {
        String operation = "getLinks";
        long chatId = request.getChatId();

        logRequest(operation, chatId, null);

        try {
            var response = scrapperGrpcHandler.getLinks(chatId);
            logSuccess(operation, chatId, null, response.size());
            responseObserver.onNext(ScrapperGrpcMapper.toGrpcListLinksResponse(response));
            responseObserver.onCompleted();
        } catch (Exception e) {
            handleError(operation, chatId, null, e, responseObserver);
        }
    }

    private void completeEmpty(StreamObserver<Empty> responseObserver) {
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    private void handleError(
            String operation, long chatId, String link, Exception e, StreamObserver<?> responseObserver) {
        Status status = mapStatus(e);
        String description = mapDescription(e);
        boolean expected = isExpected(e);

        logFailure(operation, chatId, link, status, e, expected);
        responseObserver.onError(status.withDescription(description).asRuntimeException());
    }

    private Status mapStatus(Exception e) {
        if (e instanceof IllegalArgumentException) {
            return Status.INVALID_ARGUMENT;
        }
        if (e instanceof ChatAlreadyExistsException || e instanceof LinkAlreadyTrackedException) {
            return Status.ALREADY_EXISTS;
        }
        if (e instanceof ChatNotFoundException || e instanceof LinkNotFoundException) {
            return Status.NOT_FOUND;
        }
        return Status.INTERNAL;
    }

    private String mapDescription(Exception e) {
        if (e instanceof IllegalArgumentException) {
            return e.getMessage();
        }
        if (e instanceof ChatAlreadyExistsException) {
            return "Чат уже существует";
        }
        if (e instanceof LinkAlreadyTrackedException) {
            return "Ссылка уже отслеживается";
        }
        if (e instanceof ChatNotFoundException || e instanceof LinkNotFoundException) {
            return "Чат не существует или ссылка не найдена";
        }
        return "Внутренняя ошибка сервера";
    }

    private boolean isExpected(Exception e) {
        return e instanceof IllegalArgumentException
                || e instanceof ChatAlreadyExistsException
                || e instanceof ChatNotFoundException
                || e instanceof LinkAlreadyTrackedException
                || e instanceof LinkNotFoundException;
    }

    private void logRequest(String operation, long chatId, String link) {
        var builder = log.atInfo()
                .addKeyValue("transport", "grpc")
                .addKeyValue("operation", operation)
                .addKeyValue("chat_id", chatId);

        if (link != null) {
            builder = builder.addKeyValue("link", link);
        }

        builder.log("Получен gRPC запрос");
    }

    private void logSuccess(String operation, long chatId, String link, Integer linksCount) {
        var builder = log.atInfo()
                .addKeyValue("transport", "grpc")
                .addKeyValue("operation", operation)
                .addKeyValue("chat_id", chatId);

        if (link != null) {
            builder = builder.addKeyValue("link", link);
        }
        if (linksCount != null) {
            builder = builder.addKeyValue("links_count", linksCount);
        }

        builder.log("gRPC запрос успешно обработан");
    }

    private void logFailure(String operation, long chatId, String link, Status status, Exception e, boolean expected) {
        var builder = expected ? log.atWarn() : log.atError();

        if (!expected) {
            builder = builder.setCause(e);
        }

        builder = builder.addKeyValue("transport", "grpc")
                .addKeyValue("operation", operation)
                .addKeyValue("chat_id", chatId)
                .addKeyValue("grpc_status", status.getCode().name())
                .addKeyValue("exception", e.getClass().getSimpleName());

        if (link != null) {
            builder = builder.addKeyValue("link", link);
        }

        builder.log(
                expected
                        ? "Ожидаемая ошибка при обработке gRPC запроса"
                        : "Неожиданная ошибка при обработке gRPC запроса");
    }
}
