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
        log.atInfo().addKeyValue("chat_id", request.getChatId()).log("gRPC запрос на регистрацию чата");

        try {
            scrapperGrpcHandler.registerChat(request.getChatId());
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (ChatAlreadyExistsException e) {
            responseObserver.onError(
                    Status.ALREADY_EXISTS.withDescription("Чат уже существует").asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription("Внутренняя ошибка сервера").asRuntimeException());
        }
    }

    @Override
    public void deleteChat(GrpcChatRequest request, StreamObserver<Empty> responseObserver) {
        log.atInfo().addKeyValue("chat_id", request.getChatId()).log("gRPC запрос на удаление чата");

        try {
            scrapperGrpcHandler.deleteChat(request.getChatId());
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (ChatNotFoundException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("Чат не существует или ссылка не найдена")
                    .asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription("Внутренняя ошибка сервера").asRuntimeException());
        }
    }

    @Override
    public void addLink(GrpcAddLinkCommand request, StreamObserver<Empty> responseObserver) {
        log.atInfo().addKeyValue("chat_id", request.getChatId()).log("gRPC запрос на добавление ссылки");

        try {
            scrapperGrpcHandler.addLink(request.getChatId(), ScrapperGrpcMapper.fromGrpcAddLinkCommand(request));
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (ChatNotFoundException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("Чат не существует или ссылка не найдена")
                    .asRuntimeException());
        } catch (LinkAlreadyTrackedException e) {
            responseObserver.onError(Status.ALREADY_EXISTS
                    .withDescription("Ссылка уже отслеживается")
                    .asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription("Внутренняя ошибка сервера").asRuntimeException());
        }
    }

    @Override
    public void deleteLink(GrpcDeleteLinkCommand request, StreamObserver<Empty> responseObserver) {
        log.atInfo().addKeyValue("chat_id", request.getChatId()).log("gRPC запрос на удаление ссылки");

        try {
            scrapperGrpcHandler.deleteLink(request.getChatId(), ScrapperGrpcMapper.fromGrpcDeleteLinkCommand(request));
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (ChatNotFoundException | LinkNotFoundException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("Чат не существует или ссылка не найдена")
                    .asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription("Внутренняя ошибка сервера").asRuntimeException());
        }
    }

    @Override
    public void getLinks(GrpcChatRequest request, StreamObserver<GrpcListLinksResponse> responseObserver) {
        try {
            var response = scrapperGrpcHandler.getLinks(request.getChatId());

            log.atInfo()
                    .addKeyValue("chat_id", request.getChatId())
                    .addKeyValue("links_count", response.size())
                    .log("gRPC запрос на получение списка ссылок");

            responseObserver.onNext(ScrapperGrpcMapper.toGrpcListLinksResponse(response));
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (ChatNotFoundException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("Чат не существует или ссылка не найдена")
                    .asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription("Внутренняя ошибка сервера").asRuntimeException());
        }
    }
}
