package backend.academy.linktracker.scrapper.client.protocol.grpc;

import backend.academy.linktracker.contracts.dto.mapper.BotGrpcMapper;
import backend.academy.linktracker.contracts.dto.request.CommonLinkUpdate;
import backend.academy.linktracker.generated.grpc.BotServiceGrpc;
import backend.academy.linktracker.scrapper.client.protocol.BotGateway;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "bot", name = "transport", havingValue = "grpc")
public class GrpcBotClient implements BotGateway {

    private final BotServiceGrpc.BotServiceBlockingStub stub;

    public GrpcBotClient(@Qualifier("botManagedChannel") ManagedChannel managedChannel) {
        this.stub = BotServiceGrpc.newBlockingStub(managedChannel);
    }

    @Override
    public void sendUpdate(CommonLinkUpdate commonLinkUpdate) {
        try {
            stub.sendUpdate(BotGrpcMapper.toGrpcLinkUpdate(commonLinkUpdate));
        } catch (StatusRuntimeException e) {
            throw mapSendUpdateException(e);
        }
    }

    private RuntimeException mapSendUpdateException(StatusRuntimeException e) {
        return switch (e.getStatus().getCode()) {
            case INVALID_ARGUMENT -> {
                logExpected("sendUpdate", e);
                yield new IllegalArgumentException(e.getStatus().getDescription(), e);
            }
            default -> {
                logUnexpected("sendUpdate", e);
                yield e;
            }
        };
    }

    private void logExpected(String operation, StatusRuntimeException e) {
        Status status = e.getStatus();

        log.atWarn()
                .setCause(e)
                .addKeyValue("client", "bot")
                .addKeyValue("transport", "grpc")
                .addKeyValue("operation", operation)
                .addKeyValue("grpc_code", status.getCode())
                .addKeyValue("grpc_description", status.getDescription())
                .log("Ожидаемая ошибка при вызове bot");
    }

    private void logUnexpected(String operation, StatusRuntimeException e) {
        Status status = e.getStatus();

        log.atError()
                .setCause(e)
                .addKeyValue("client", "bot")
                .addKeyValue("transport", "grpc")
                .addKeyValue("operation", operation)
                .addKeyValue("grpc_code", status.getCode())
                .addKeyValue("grpc_description", status.getDescription())
                .log("Неожиданная ошибка при вызове bot");
    }
}
