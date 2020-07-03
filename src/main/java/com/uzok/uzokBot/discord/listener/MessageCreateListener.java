package com.uzok.uzokBot.discord.listener;

import com.uzok.uzokBot.utils.MessageEventContext;
import com.uzok.uzokBot.discord.command.BaseCommand;
import com.uzok.uzokBot.discord.command.CommandOrchestrator;
import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

public class MessageCreateListener implements EventListener<MessageCreateEvent> {
    @Override
    public Class<MessageCreateEvent> getEventType() {
        return MessageCreateEvent.class;
    }

    @Override
    public Mono<Void> execute(MessageCreateEvent event) {
        final String messageContent = event.getMessage().getContent();
        if (!messageContent.startsWith("!")) {
            return Mono.empty();
        }

        final MessageEventContext eventContext = new MessageEventContext(event);
        final BaseCommand command = CommandOrchestrator.getInstance().getCommand(eventContext.getCommandName());

        if (command == null) {
            return Mono.empty();
        }
        return command.execute(eventContext);
    }
}
