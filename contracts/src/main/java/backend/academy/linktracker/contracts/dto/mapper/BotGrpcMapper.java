package backend.academy.linktracker.contracts.dto.mapper;

import backend.academy.linktracker.contracts.dto.request.CommonLinkUpdate;
import backend.academy.linktracker.generated.grpc.GrpcLinkUpdate;
import java.net.URI;

public final class BotGrpcMapper {

    private BotGrpcMapper() {}

    public static GrpcLinkUpdate toGrpcLinkUpdate(CommonLinkUpdate commonLinkUpdate) {
        return GrpcLinkUpdate.newBuilder()
                .setId(commonLinkUpdate.id())
                .setUrl(commonLinkUpdate.url().toString())
                .setDescription(commonLinkUpdate.description())
                .addAllTgChatIds(commonLinkUpdate.tgChatIds())
                .build();
    }

    public static CommonLinkUpdate fromGrpcLinkUpdate(GrpcLinkUpdate linkUpdate) {
        return new CommonLinkUpdate(
                linkUpdate.getId(),
                URI.create(linkUpdate.getUrl()),
                linkUpdate.getDescription(),
                linkUpdate.getTgChatIdsList());
    }
}
