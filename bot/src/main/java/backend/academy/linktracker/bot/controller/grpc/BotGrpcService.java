package backend.academy.linktracker.bot.controller.grpc;

import backend.academy.linktracker.contracts.dto.mapper.BotGrpcMapper;
import backend.academy.linktracker.generated.grpc.BotServiceGrpc;
import backend.academy.linktracker.generated.grpc.GrpcLinkUpdate;
import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.spi.LoggingEventBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "scrapper", name = "transport", havingValue = "grpc")
public class BotGrpcService extends BotServiceGrpc.BotServiceImplBase {

    private final BotGrpcUpdateHandler botGrpcUpdateHandler;

    @Override
    public void sendUpdate(GrpcLinkUpdate request, StreamObserver<Empty> responseObserver) {
        logInfo(request).log("Получен gRPC запрос на отправку обновления");

        try {
            botGrpcUpdateHandler.handle(BotGrpcMapper.fromGrpcLinkUpdate(request));
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();

            logInfo(request).log("gRPC запрос успешно обработан");
        } catch (Exception e) {
            handleError(request, e, responseObserver);
        }
    }

    private void handleError(GrpcLinkUpdate request, Exception e, StreamObserver<?> responseObserver) {
        Status status = mapStatus(e);
        String description = mapDescription(e);
        boolean expected = isExpected(e);

        logFailure(request, e, status, expected);
        responseObserver.onError(status.withDescription(description).asRuntimeException());
    }

    private Status mapStatus(Exception e) {
        if (e instanceof IllegalArgumentException) {
            return Status.INVALID_ARGUMENT;
        }
        return Status.INTERNAL;
    }

    private String mapDescription(Exception e) {
        if (e instanceof IllegalArgumentException) {
            return e.getMessage();
        }
        return "Внутренняя ошибка сервера";
    }

    private boolean isExpected(Exception e) {
        return e instanceof IllegalArgumentException;
    }

    private void logFailure(GrpcLinkUpdate request, Exception e, Status status, boolean expected) {
        LoggingEventBuilder builder = expected ? log.atWarn() : log.atError();

        addCommonFields(builder, request)
                .setCause(e)
                .addKeyValue("grpc_status", status.getCode().name())
                .addKeyValue("exception", e.getClass().getSimpleName())
                .log(
                        expected
                                ? "Ожидаемая ошибка при обработке gRPC запроса"
                                : "Неожиданная ошибка при обработке gRPC запроса");
    }

    private LoggingEventBuilder logInfo(GrpcLinkUpdate request) {
        return addCommonFields(log.atInfo(), request);
    }

    private LoggingEventBuilder addCommonFields(LoggingEventBuilder builder, GrpcLinkUpdate request) {
        return builder.addKeyValue("transport", "grpc")
                .addKeyValue("operation", "sendUpdate")
                .addKeyValue("link_id", request.getId())
                .addKeyValue("url", request.getUrl())
                .addKeyValue("tg_chat_ids_count", request.getTgChatIdsCount());
    }
}
