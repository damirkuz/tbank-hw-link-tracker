package backend.academy.linktracker.bot.handler.registry;

import backend.academy.linktracker.bot.handler.command.CommandHandler;

public record MyBotCommand(
    String command,
    String description,
    CommandHandler handler
) {}
