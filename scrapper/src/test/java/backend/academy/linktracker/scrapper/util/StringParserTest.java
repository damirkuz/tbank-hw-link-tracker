package backend.academy.linktracker.scrapper.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import backend.academy.linktracker.scrapper.client.provider.github.RepoInfo;
import java.net.URI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("StringParser")
class StringParserTest {

    // ─────────────────── parseGithubRepositoryLink ───────────────────

    @Test
    @DisplayName("Парсит owner и repo из стандартной ссылки")
    void parseGithubStandard() {
        RepoInfo info = StringParser.parseGithubRepositoryLink(URI.create("https://github.com/octocat/Hello-World"));

        assertThat(info.owner()).isEqualTo("octocat");
        assertThat(info.repo()).isEqualTo("Hello-World");
    }

    @Test
    @DisplayName("Убирает .git суффикс из имени репозитория")
    void parseGithubStripsGitSuffix() {
        RepoInfo info =
                StringParser.parseGithubRepositoryLink(URI.create("https://github.com/octocat/Hello-World.git"));

        assertThat(info.repo()).isEqualTo("Hello-World");
    }

    @Test
    @DisplayName("Принимает www.github.com")
    void parseGithubWithWww() {
        RepoInfo info = StringParser.parseGithubRepositoryLink(URI.create("https://www.github.com/user/repo"));

        assertThat(info.owner()).isEqualTo("user");
        assertThat(info.repo()).isEqualTo("repo");
    }

    @Test
    @DisplayName("Бросает IllegalArgumentException для не-GitHub хоста")
    void parseGithubWrongHost() {
        assertThatThrownBy(() -> StringParser.parseGithubRepositoryLink(URI.create("https://gitlab.com/user/repo")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Not a GitHub repository link");
    }

    @Test
    @DisplayName("Бросает IllegalArgumentException если путь короче /owner/repo")
    void parseGithubShortPath() {
        assertThatThrownBy(() -> StringParser.parseGithubRepositoryLink(URI.create("https://github.com/octocat")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid GitHub repository link");
    }

    @Test
    @DisplayName("Бросает IllegalArgumentException если owner пустой")
    void parseGithubEmptyOwner() {
        assertThatThrownBy(() -> StringParser.parseGithubRepositoryLink(URI.create("https://github.com//repo")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Учитывает доп. сегменты пути — берёт только первые два")
    void parseGithubWithExtraPathSegments() {
        RepoInfo info =
                StringParser.parseGithubRepositoryLink(URI.create("https://github.com/user/repo/tree/main/src"));

        assertThat(info.owner()).isEqualTo("user");
        assertThat(info.repo()).isEqualTo("repo");
    }

    // ─────────────────── parseStackoverflowQuestionId ───────────────────

    @Test
    @DisplayName("Парсит id вопроса из стандартной ссылки stackoverflow.com")
    void parseStackoverflowStandard() {
        long id = StringParser.parseStackoverflowQuestionId(
                URI.create("https://stackoverflow.com/questions/12345/some-title"));

        assertThat(id).isEqualTo(12345L);
    }

    @Test
    @DisplayName("Парсит id вопроса из ru.stackoverflow.com")
    void parseStackoverflowRu() {
        long id = StringParser.parseStackoverflowQuestionId(
                URI.create("https://ru.stackoverflow.com/questions/99999/title"));

        assertThat(id).isEqualTo(99999L);
    }

    @Test
    @DisplayName("Бросает IllegalArgumentException для не-SO хоста")
    void parseStackoverflowWrongHost() {
        assertThatThrownBy(() -> StringParser.parseStackoverflowQuestionId(
                        URI.create("https://superuser.com/questions/12345/title")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Not a Stack Overflow question link");
    }

    @Test
    @DisplayName("Бросает IllegalArgumentException если в пути нет 'questions'")
    void parseStackoverflowNoQuestionsSegment() {
        assertThatThrownBy(() ->
                        StringParser.parseStackoverflowQuestionId(URI.create("https://stackoverflow.com/tags/java")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid Stack Overflow question link");
    }

    @Test
    @DisplayName("Бросает IllegalArgumentException если id не является числом")
    void parseStackoverflowNonNumericId() {
        assertThatThrownBy(() -> StringParser.parseStackoverflowQuestionId(
                        URI.create("https://stackoverflow.com/questions/abc/title")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid question id in link");
    }

    @Test
    @DisplayName("Бросает IllegalArgumentException если путь слишком короткий")
    void parseStackoverflowShortPath() {
        assertThatThrownBy(() ->
                        StringParser.parseStackoverflowQuestionId(URI.create("https://stackoverflow.com/questions")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
