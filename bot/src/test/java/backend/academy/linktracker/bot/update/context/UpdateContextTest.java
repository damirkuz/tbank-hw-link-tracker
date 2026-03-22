package backend.academy.linktracker.bot.update.context;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("UpdateContext")
class UpdateContextTest {

    private UpdateContext ctx(String messageText, String callbackData) {
        return new UpdateContext(1, 10L, 20L, messageText, callbackData);
    }

    // --- hasMessageText ---

    @Test
    @DisplayName("hasMessageText — false при null")
    void hasMessageTextFalseForNull() {
        assertThat(ctx(null, null).hasMessageText()).isFalse();
    }

    @Test
    @DisplayName("hasMessageText — false при пустой строке")
    void hasMessageTextFalseForBlank() {
        assertThat(ctx("   ", null).hasMessageText()).isFalse();
    }

    @Test
    @DisplayName("hasMessageText — true при обычном тексте")
    void hasMessageTextTrue() {
        assertThat(ctx("hello", null).hasMessageText()).isTrue();
    }

    // --- isCommand ---

    @Test
    @DisplayName("isCommand — точное совпадение")
    void isCommandExactMatch() {
        assertThat(ctx("/cancel", null).isCommand("/cancel")).isTrue();
    }

    @Test
    @DisplayName("isCommand — false при неточном совпадении")
    void isCommandFalseForPartial() {
        assertThat(ctx("/cancel now", null).isCommand("/cancel")).isFalse();
    }

    // --- isCommandName ---

    @Test
    @DisplayName("isCommandName — true для /start")
    void isCommandNameSimple() {
        assertThat(ctx("/start", null).isCommandName("start")).isTrue();
    }

    @Test
    @DisplayName("isCommandName — true для /start с параметром")
    void isCommandNameWithParam() {
        assertThat(ctx("/start param", null).isCommandName("start")).isTrue();
    }

    @Test
    @DisplayName("isCommandName — false для другой команды")
    void isCommandNameFalseForOther() {
        assertThat(ctx("/help", null).isCommandName("start")).isFalse();
    }

    @Test
    @DisplayName("isCommandName — false если не команда")
    void isCommandNameFalseForPlainText() {
        assertThat(ctx("start", null).isCommandName("start")).isFalse();
    }

    // --- commandIdentifierForBot ---

    @Test
    @DisplayName("commandIdentifierForBot — возвращает /start для своего бота")
    void commandIdentifierForBotOwn() {
        assertThat(ctx("/start@mybot", null).commandIdentifierForBot("mybot"))
                .isPresent()
                .contains("/start");
    }

    @Test
    @DisplayName("commandIdentifierForBot — empty для другого бота")
    void commandIdentifierForBotEmpty() {
        assertThat(ctx("/start@otherbot", null).commandIdentifierForBot("mybot"))
                .isEmpty();
    }

    @Test
    @DisplayName("commandIdentifierForBot — возвращает /start без mention (команда без @)")
    void commandIdentifierForBotNoMention() {
        assertThat(ctx("/start", null).commandIdentifierForBot("mybot"))
                .isPresent()
                .contains("/start");
    }

    // --- isCallbackOrCommandAction ---

    @Test
    @DisplayName("isCallbackOrCommandAction — true по callback")
    void isCallbackOrCommandActionByCallback() {
        assertThat(ctx(null, "cancel").isCallbackOrCommandAction("cancel")).isTrue();
    }

    @Test
    @DisplayName("isCallbackOrCommandAction — true по команде")
    void isCallbackOrCommandActionByCommand() {
        assertThat(ctx("/cancel", null).isCallbackOrCommandAction("cancel")).isTrue();
    }

    @Test
    @DisplayName("isCallbackOrCommandAction — false если ни то ни другое")
    void isCallbackOrCommandActionFalse() {
        assertThat(ctx("hello", null).isCallbackOrCommandAction("cancel")).isFalse();
    }

    // --- hasCallbackData ---

    @Test
    @DisplayName("hasCallbackData — true при наличии данных")
    void hasCallbackDataTrue() {
        assertThat(ctx(null, "some_data").hasCallbackData()).isTrue();
    }

    @Test
    @DisplayName("hasCallbackData — false при null")
    void hasCallbackDataFalse() {
        assertThat(ctx(null, null).hasCallbackData()).isFalse();
    }
}
