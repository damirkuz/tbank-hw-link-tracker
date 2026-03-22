package backend.academy.linktracker.contracts.dto.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.contracts.dto.request.CommonLinkUpdate;
import backend.academy.linktracker.generated.grpc.GrpcLinkUpdate;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("BotGrpcMapper")
class BotGrpcMapperTest {

    private static final URI URI_LINK = URI.create("https://github.com/user/repo");

    @Test
    @DisplayName("toGrpcLinkUpdate — маппит все поля")
    void toGrpcLinkUpdate() {
        CommonLinkUpdate common = new CommonLinkUpdate(1L, URI_LINK, "desc", List.of(10L, 20L));

        GrpcLinkUpdate result = BotGrpcMapper.toGrpcLinkUpdate(common);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUrl()).isEqualTo(URI_LINK.toString());
        assertThat(result.getDescription()).isEqualTo("desc");
        assertThat(result.getTgChatIdsList()).containsExactly(10L, 20L);
    }

    @Test
    @DisplayName("fromGrpcLinkUpdate — маппит все поля")
    void fromGrpcLinkUpdate() {
        GrpcLinkUpdate grpc = GrpcLinkUpdate.newBuilder()
                .setId(3L)
                .setUrl(URI_LINK.toString())
                .setDescription("d")
                .addAllTgChatIds(List.of(99L))
                .build();

        CommonLinkUpdate result = BotGrpcMapper.fromGrpcLinkUpdate(grpc);

        assertThat(result.id()).isEqualTo(3L);
        assertThat(result.url()).isEqualTo(URI_LINK);
        assertThat(result.description()).isEqualTo("d");
        assertThat(result.tgChatIds()).containsExactly(99L);
    }

    @Test
    @DisplayName("toGrpcLinkUpdate → fromGrpcLinkUpdate — roundtrip")
    void roundtrip() {
        CommonLinkUpdate original = new CommonLinkUpdate(5L, URI_LINK, "update", List.of(1L, 2L));

        CommonLinkUpdate result = BotGrpcMapper.fromGrpcLinkUpdate(BotGrpcMapper.toGrpcLinkUpdate(original));

        assertThat(result).isEqualTo(original);
    }
}
