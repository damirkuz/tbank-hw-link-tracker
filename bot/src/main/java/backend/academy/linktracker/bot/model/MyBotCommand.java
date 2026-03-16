package backend.academy.linktracker.bot.model;

import backend.academy.linktracker.bot.update.handler.command.CommandHandler;

public record MyBotCommand(String command, String description, CommandHandler handler) {}
