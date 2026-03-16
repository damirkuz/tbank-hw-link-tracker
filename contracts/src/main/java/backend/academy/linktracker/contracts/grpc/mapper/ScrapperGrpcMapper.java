package backend.academy.linktracker.contracts.grpc.mapper;

import backend.academy.linktracker.contracts.dto.request.AddLinkRequest;
import backend.academy.linktracker.contracts.dto.request.RemoveLinkRequest;
import backend.academy.linktracker.contracts.dto.response.LinkResponse;
import backend.academy.linktracker.contracts.dto.response.ListLinksResponse;
import backend.academy.linktracker.generated.grpc.GrpcAddLinkCommand;
import backend.academy.linktracker.generated.grpc.GrpcAddLinkRequest;
import backend.academy.linktracker.generated.grpc.GrpcChatRequest;
import backend.academy.linktracker.generated.grpc.GrpcDeleteLinkCommand;
import backend.academy.linktracker.generated.grpc.GrpcLinkResponse;
import backend.academy.linktracker.generated.grpc.GrpcListLinksResponse;
import backend.academy.linktracker.generated.grpc.GrpcRemoveLinkRequest;
import java.util.Arrays;
import java.util.List;

public final class ScrapperGrpcMapper {

    private ScrapperGrpcMapper() {}

    public static GrpcChatRequest toGrpcChatRequest(long chatId) {
        return GrpcChatRequest.newBuilder().setChatId(chatId).build();
    }

    public static GrpcAddLinkCommand toGrpcAddLinkCommand(long chatId, AddLinkRequest request) {
        return GrpcAddLinkCommand.newBuilder()
                .setChatId(chatId)
                .setRequest(toGrpcAddLinkRequest(request))
                .build();
    }

    public static GrpcDeleteLinkCommand toGrpcDeleteLinkCommand(long chatId, RemoveLinkRequest request) {
        return GrpcDeleteLinkCommand.newBuilder()
                .setChatId(chatId)
                .setRequest(toGrpcRemoveLinkRequest(request))
                .build();
    }

    public static AddLinkRequest fromGrpcAddLinkCommand(GrpcAddLinkCommand command) {
        return fromGrpcAddLinkRequest(command.getRequest());
    }

    public static RemoveLinkRequest fromGrpcDeleteLinkCommand(GrpcDeleteLinkCommand command) {
        return fromGrpcRemoveLinkRequest(command.getRequest());
    }

    public static GrpcAddLinkRequest toGrpcAddLinkRequest(AddLinkRequest request) {
        return GrpcAddLinkRequest.newBuilder()
                .setUri(request.uri())
                .addAllTags(toStringList(request.tags()))
                .addAllFilters(toStringList(request.filters()))
                .build();
    }

    public static AddLinkRequest fromGrpcAddLinkRequest(GrpcAddLinkRequest request) {
        return new AddLinkRequest(
                request.getUri(),
                request.getTagsList().toArray(String[]::new),
                request.getFiltersList().toArray(String[]::new));
    }

    public static GrpcRemoveLinkRequest toGrpcRemoveLinkRequest(RemoveLinkRequest request) {
        return GrpcRemoveLinkRequest.newBuilder().setUri(request.uri()).build();
    }

    public static RemoveLinkRequest fromGrpcRemoveLinkRequest(GrpcRemoveLinkRequest request) {
        return new RemoveLinkRequest(request.getUri());
    }

    public static GrpcLinkResponse toGrpcLinkResponse(LinkResponse response) {
        return GrpcLinkResponse.newBuilder()
                .setId(response.id())
                .setUrl(response.url())
                .addAllTags(toStringList(response.tags()))
                .addAllFilters(toStringList(response.filters()))
                .build();
    }

    public static LinkResponse fromGrpcLinkResponse(GrpcLinkResponse response) {
        return new LinkResponse(
                response.getId(),
                response.getUrl(),
                response.getTagsList().toArray(String[]::new),
                response.getFiltersList().toArray(String[]::new));
    }

    public static GrpcListLinksResponse toGrpcListLinksResponse(ListLinksResponse response) {
        GrpcListLinksResponse.Builder builder =
                GrpcListLinksResponse.newBuilder().setSize(response.size());

        if (response.links() != null) {
            Arrays.stream(response.links())
                    .map(ScrapperGrpcMapper::toGrpcLinkResponse)
                    .forEach(builder::addLinks);
        }

        return builder.build();
    }

    public static ListLinksResponse fromGrpcListLinksResponse(GrpcListLinksResponse response) {
        LinkResponse[] links = response.getLinksList().stream()
                .map(ScrapperGrpcMapper::fromGrpcLinkResponse)
                .toArray(LinkResponse[]::new);

        return new ListLinksResponse(links, response.getSize());
    }

    private static List<String> toStringList(String[] values) {
        return values == null ? List.of() : Arrays.asList(values);
    }
}
