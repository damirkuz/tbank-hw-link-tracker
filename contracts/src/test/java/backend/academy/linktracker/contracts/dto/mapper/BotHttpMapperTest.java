package backend.academy.linktracker.contracts.dto.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.bot.generated.dto.LinkUpdate;
import backend.academy.linktracker.contracts.dto.request.CommonLinkUpdate;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("BotHttpMapper")
class BotHttpMapperTest {

    private static final URI URI_LINK = URI.create("https://github.com/user/repo");

    @Test
    @DisplayName("toLinkUpdate — маппит все поля")
    void toLinkUpdate() {
        CommonLinkUpdate common = new CommonLinkUpdate(1L, URI_LINK, "desc", List.of(10L, 20L));

        LinkUpdate result = BotHttpMapper.toLinkUpdate(common);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUrl()).isEqualTo(URI_LINK);
        assertThat(result.getDescription()).isEqualTo("desc");
        assertThat(result.getTgChatIds()).containsExactly(10L, 20L);
    }

    @Test
    @DisplayName("fromLinkUpdate — маппит все поля")
    void fromLinkUpdate() {
        LinkUpdate update = new LinkUpdate();
        update.setId(2L);
        update.setUrl(URI_LINK);
        update.setDescription("desc2");
        update.setTgChatIds(List.of(5L));

        CommonLinkUpdate result = BotHttpMapper.fromLinkUpdate(update);

        assertThat(result.id()).isEqualTo(2L);
        assertThat(result.url()).isEqualTo(URI_LINK);
        assertThat(result.description()).isEqualTo("desc2");
        assertThat(result.tgChatIds()).containsExactly(5L);
    }

    @Test
    @DisplayName("toLinkUpdate → fromLinkUpdate — roundtrip")
    void roundtrip() {
        CommonLinkUpdate original = new CommonLinkUpdate(7L, URI_LINK, "hello", List.of(1L, 2L, 3L));

        CommonLinkUpdate result = BotHttpMapper.fromLinkUpdate(BotHttpMapper.toLinkUpdate(original));

        assertThat(result).isEqualTo(original);
    }
}
