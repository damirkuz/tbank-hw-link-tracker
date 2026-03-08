package backend.academy.linktracker.common.grpc.mapper;

import backend.academy.linktracker.common.request.LinkUpdate;
import backend.academy.linktracker.generated.grpc.GrpcLinkUpdate;
import java.util.Arrays;
import java.util.List;

public final class BotGrpcMapper {

    private BotGrpcMapper() {}

    public static GrpcLinkUpdate toGrpcLinkUpdate(LinkUpdate linkUpdate) {
        return GrpcLinkUpdate.newBuilder()
                .setId(linkUpdate.id())
                .setUrl(linkUpdate.url())
                .setDescription(linkUpdate.description())
                .addAllTgChatIds(toLongList(linkUpdate.tgChatIds()))
                .build();
    }

    public static LinkUpdate fromGrpcLinkUpdate(GrpcLinkUpdate linkUpdate) {
        return new LinkUpdate(
                linkUpdate.getId(),
                linkUpdate.getUrl(),
                linkUpdate.getDescription(),
                linkUpdate.getTgChatIdsList().toArray(Long[]::new));
    }

    private static List<Long> toLongList(Long[] values) {
        return values == null ? List.of() : Arrays.asList(values);
    }
}
