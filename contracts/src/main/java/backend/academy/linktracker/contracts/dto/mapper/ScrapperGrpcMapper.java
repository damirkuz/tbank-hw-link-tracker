package backend.academy.linktracker.contracts.dto.mapper;

import backend.academy.linktracker.contracts.dto.request.CommonAddLinkRequest;
import backend.academy.linktracker.contracts.dto.request.CommonRemoveLinkRequest;
import backend.academy.linktracker.contracts.dto.response.CommonLinkResponse;
import backend.academy.linktracker.contracts.dto.response.CommonListLinksResponse;
import backend.academy.linktracker.generated.grpc.GrpcAddLinkCommand;
import backend.academy.linktracker.generated.grpc.GrpcAddLinkRequest;
import backend.academy.linktracker.generated.grpc.GrpcChatRequest;
import backend.academy.linktracker.generated.grpc.GrpcDeleteLinkCommand;
import backend.academy.linktracker.generated.grpc.GrpcLinkResponse;
import backend.academy.linktracker.generated.grpc.GrpcListLinksResponse;
import backend.academy.linktracker.generated.grpc.GrpcRemoveLinkRequest;
import java.net.URI;
import java.util.List;

public final class ScrapperGrpcMapper {

    private ScrapperGrpcMapper() {}

    public static GrpcChatRequest toGrpcChatRequest(long chatId) {
        return GrpcChatRequest.newBuilder().setChatId(chatId).build();
    }

    public static GrpcAddLinkCommand toGrpcAddLinkCommand(long chatId, CommonAddLinkRequest request) {
        return GrpcAddLinkCommand.newBuilder()
                .setChatId(chatId)
                .setRequest(toGrpcAddLinkRequest(request))
                .build();
    }

    public static GrpcDeleteLinkCommand toGrpcDeleteLinkCommand(long chatId, CommonRemoveLinkRequest request) {
        return GrpcDeleteLinkCommand.newBuilder()
                .setChatId(chatId)
                .setRequest(toGrpcRemoveLinkRequest(request))
                .build();
    }

    public static CommonAddLinkRequest fromGrpcAddLinkCommand(GrpcAddLinkCommand command) {
        return fromGrpcAddLinkRequest(command.getRequest());
    }

    public static CommonRemoveLinkRequest fromGrpcDeleteLinkCommand(GrpcDeleteLinkCommand command) {
        return fromGrpcRemoveLinkRequest(command.getRequest());
    }

    public static GrpcAddLinkRequest toGrpcAddLinkRequest(CommonAddLinkRequest request) {
        return GrpcAddLinkRequest.newBuilder()
                .setUri(request.uri().toString())
                .addAllTags(request.tags())
                .addAllFilters(request.filters())
                .build();
    }

    public static CommonAddLinkRequest fromGrpcAddLinkRequest(GrpcAddLinkRequest request) {
        return new CommonAddLinkRequest(URI.create(request.getUri()), request.getTagsList(), request.getFiltersList());
    }

    public static GrpcRemoveLinkRequest toGrpcRemoveLinkRequest(CommonRemoveLinkRequest request) {
        return GrpcRemoveLinkRequest.newBuilder()
                .setUri(request.uri().toString())
                .build();
    }

    public static CommonRemoveLinkRequest fromGrpcRemoveLinkRequest(GrpcRemoveLinkRequest request) {
        return new CommonRemoveLinkRequest(URI.create(request.getUri()));
    }

    public static GrpcLinkResponse toGrpcLinkResponse(CommonLinkResponse response) {
        return GrpcLinkResponse.newBuilder()
                .setId(response.id())
                .setUrl(response.url().toString())
                .addAllTags(response.tags())
                .addAllFilters(response.filters())
                .build();
    }

    public static CommonLinkResponse fromGrpcLinkResponse(GrpcLinkResponse response) {
        return new CommonLinkResponse(
                response.getId(), URI.create(response.getUrl()), response.getTagsList(), response.getFiltersList());
    }

    public static GrpcListLinksResponse toGrpcListLinksResponse(CommonListLinksResponse response) {
        GrpcListLinksResponse.Builder builder =
                GrpcListLinksResponse.newBuilder().setSize(response.size());

        if (response.links() != null) {
            response.links().stream()
                    .map(ScrapperGrpcMapper::toGrpcLinkResponse)
                    .forEach(builder::addLinks);
        }

        return builder.build();
    }

    public static CommonListLinksResponse fromGrpcListLinksResponse(GrpcListLinksResponse response) {
        List<CommonLinkResponse> links = response.getLinksList().stream()
                .map(ScrapperGrpcMapper::fromGrpcLinkResponse)
                .toList();

        return new CommonListLinksResponse(links, response.getSize());
    }
}
