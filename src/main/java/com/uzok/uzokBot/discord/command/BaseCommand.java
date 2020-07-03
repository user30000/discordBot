package com.uzok.uzokBot.discord.command;

import com.uzok.uzokBot.utils.MessageEventContext;
import reactor.core.publisher.Mono;

public abstract class BaseCommand {
    String commandName;
    String shortDescription;
    String description;
    String example;
    public abstract Mono<Void> execute(MessageEventContext context);
}
