package backend.academy.linktracker.scrapper.link;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import backend.academy.linktracker.scrapper.link.handlers.GithubLinkHandler;
import backend.academy.linktracker.scrapper.link.handlers.LinkHandler;
import backend.academy.linktracker.scrapper.link.handlers.StackoverflowHandler;
import backend.academy.linktracker.scrapper.model.TrackedResource;
import java.net.URI;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("TrackedResourceResolver")
class TrackedResourceResolverTest {

    private TrackedResourceResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new TrackedResourceResolver(List.of(new GithubLinkHandler(), new StackoverflowHandler()));
    }

    // ─────────────────── resolve ───────────────────

    @Test
    @DisplayName("resolve — возвращает GITHUB для github.com")
    void resolveGithub() {
        TrackedResource result = resolver.resolve(URI.create("https://github.com/user/repo"));
        assertThat(result).isEqualTo(TrackedResource.GITHUB);
    }

    @Test
    @DisplayName("resolve — возвращает GITHUB для www.github.com (нормализация www)")
    void resolveGithubWithWww() {
        TrackedResource result = resolver.resolve(URI.create("https://www.github.com/user/repo"));
        assertThat(result).isEqualTo(TrackedResource.GITHUB);
    }

    @Test
    @DisplayName("resolve — возвращает STACKOVERFLOW для stackoverflow.com")
    void resolveStackoverflow() {
        TrackedResource result = resolver.resolve(URI.create("https://stackoverflow.com/questions/1/title"));
        assertThat(result).isEqualTo(TrackedResource.STACKOVERFLOW);
    }

    @Test
    @DisplayName("resolve — возвращает STACKOVERFLOW для ru.stackoverflow.com")
    void resolveStackoverflowRu() {
        TrackedResource result = resolver.resolve(URI.create("https://ru.stackoverflow.com/questions/1/title"));
        assertThat(result).isEqualTo(TrackedResource.STACKOVERFLOW);
    }

    @Test
    @DisplayName("resolve — нечувствителен к регистру хоста")
    void resolveCaseInsensitive() {
        TrackedResource result = resolver.resolve(URI.create("https://GITHUB.COM/user/repo"));
        assertThat(result).isEqualTo(TrackedResource.GITHUB);
    }

    @Test
    @DisplayName("resolve — бросает IllegalArgumentException для неизвестного хоста")
    void resolveUnknownHost() {
        assertThatThrownBy(() -> resolver.resolve(URI.create("https://unknown.com/path")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ресурс не поддерживается");
    }

    @Test
    @DisplayName("resolve — бросает IllegalArgumentException если URI без хоста")
    void resolveUriWithoutHost() {
        assertThatThrownBy(() -> resolver.resolve(URI.create("/relative/path")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("resolve — бросает IllegalArgumentException для null URI")
    void resolveNullUri() {
        assertThatThrownBy(() -> resolver.resolve(null)).isInstanceOf(IllegalArgumentException.class);
    }

    // ─────────────────── constructor — дублирующиеся хосты ───────────────────

    @Test
    @DisplayName("Конструктор — бросает IllegalStateException при конфликте обработчиков")
    void constructorDuplicateHandlerThrows() {
        LinkHandler duplicate = new LinkHandler() {
            @Override
            public Set<String> supportedHosts() {
                return Set.of("github.com"); // конфликт с GithubLinkHandler
            }

            @Override
            public TrackedResource resource() {
                return TrackedResource.GITHUB;
            }
        };

        assertThatThrownBy(() -> new TrackedResourceResolver(List.of(new GithubLinkHandler(), duplicate)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Duplicate handler for host");
    }

    @Test
    @DisplayName("Конструктор — успешно регистрирует все хосты обработчиков")
    void constructorRegistersAllHosts() {
        // StackoverflowHandler регистрирует 2 хоста — оба должны резолвиться
        assertThat(resolver.resolve(URI.create("https://stackoverflow.com/questions/1/t")))
                .isEqualTo(TrackedResource.STACKOVERFLOW);
        assertThat(resolver.resolve(URI.create("https://ru.stackoverflow.com/questions/1/t")))
                .isEqualTo(TrackedResource.STACKOVERFLOW);
    }
}
