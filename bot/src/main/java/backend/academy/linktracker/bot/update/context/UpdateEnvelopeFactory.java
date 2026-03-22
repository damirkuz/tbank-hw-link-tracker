package backend.academy.linktracker.bot.update.context;

import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdateEnvelopeFactory {
    private final UpdateContextExtractor updateContextExtractor;

    public UpdateEnvelope create(Update update) {
        return new UpdateEnvelope(update, updateContextExtractor.extract(update));
    }
}
