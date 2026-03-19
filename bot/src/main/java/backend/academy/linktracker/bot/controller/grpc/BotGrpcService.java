package backend.academy.linktracker.bot.controller.grpc;

import backend.academy.linktracker.contracts.dto.mapper.BotGrpcMapper;
import backend.academy.linktracker.generated.grpc.BotServiceGrpc;
import backend.academy.linktracker.generated.grpc.GrpcLinkUpdate;
import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "scrapper", name = "transport", havingValue = "grpc")
public class BotGrpcService extends BotServiceGrpc.BotServiceImplBase {

    private final BotGrpcUpdateHandler botGrpcUpdateHandler;

    @Override
    public void sendUpdate(GrpcLinkUpdate request, StreamObserver<Empty> responseObserver) {
        try {
            botGrpcUpdateHandler.handle(BotGrpcMapper.fromGrpcLinkUpdate(request));
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription("Внутренняя ошибка сервера").asRuntimeException());
        }
    }
}
