package com.uzok.uzokBot.discord.command;

import com.uzok.uzokBot.utils.context.MessageEventContext;
import reactor.core.publisher.Mono;

import java.io.IOException;

public abstract class BaseCommand {
    String[] commandNames;
    String shortDescription;
    String description;
    String example;
    public abstract Mono<Void> execute(MessageEventContext context) throws IOException;
}
