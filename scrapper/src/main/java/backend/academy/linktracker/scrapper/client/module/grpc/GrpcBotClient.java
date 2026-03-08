package backend.academy.linktracker.scrapper.client.module.grpc;

import backend.academy.linktracker.common.grpc.mapper.BotGrpcMapper;
import backend.academy.linktracker.common.request.LinkUpdate;
import backend.academy.linktracker.generated.grpc.BotServiceGrpc;
import backend.academy.linktracker.scrapper.client.module.BotGateway;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "bot", name = "transport", havingValue = "grpc")
public class GrpcBotClient implements BotGateway {

    private final BotServiceGrpc.BotServiceBlockingStub stub;

    public GrpcBotClient(@Qualifier("botManagedChannel") ManagedChannel managedChannel) {
        this.stub = BotServiceGrpc.newBlockingStub(managedChannel);
    }

    @Override
    public void sendUpdate(LinkUpdate linkUpdate) {
        try {
            stub.sendUpdate(BotGrpcMapper.toGrpcLinkUpdate(linkUpdate));
        } catch (StatusRuntimeException e) {
            throw switch (e.getStatus().getCode()) {
                case INVALID_ARGUMENT ->
                    new IllegalArgumentException(e.getStatus().getDescription(), e);
                default -> e;
            };
        }
    }
}
