package backend.academy.linktracker.bot.client.grpc;

import backend.academy.linktracker.bot.client.ScrapperGateway;
import backend.academy.linktracker.common.exception.ChatAlreadyExistsException;
import backend.academy.linktracker.common.exception.ChatNotFoundException;
import backend.academy.linktracker.common.exception.LinkAlreadyTrackedException;
import backend.academy.linktracker.common.grpc.mapper.ScrapperGrpcMapper;
import backend.academy.linktracker.common.request.AddLinkRequest;
import backend.academy.linktracker.common.request.RemoveLinkRequest;
import backend.academy.linktracker.common.response.ListLinksResponse;
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
    public void addLink(long chatId, AddLinkRequest addLinkRequest) {
        try {
            stub.addLink(ScrapperGrpcMapper.toGrpcAddLinkCommand(chatId, addLinkRequest));
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
    public void deleteLink(long chatId, RemoveLinkRequest removeLinkRequest) {
        try {
            stub.deleteLink(ScrapperGrpcMapper.toGrpcDeleteLinkCommand(chatId, removeLinkRequest));
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
    public ListLinksResponse getLinks(long chatId) {
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
