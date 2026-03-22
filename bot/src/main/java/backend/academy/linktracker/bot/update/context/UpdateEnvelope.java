package backend.academy.linktracker.bot.update.context;

import com.pengrad.telegrambot.model.Update;

public record UpdateEnvelope(Update update, UpdateContext context) {}
