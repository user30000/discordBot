package com.uzok.uzokBot.discord.listener;

import com.uzok.uzokBot.utils.Logger;
import com.uzok.uzokBot.utils.context.MessageEventContext;
import com.uzok.uzokBot.discord.command.BaseCommand;
import com.uzok.uzokBot.discord.command.CommandOrchestrator;
import com.uzok.uzokBot.utils.emoji.Emoji;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.reaction.ReactionEmoji;
import reactor.core.publisher.Mono;

public class MessageCreateListener implements EventListener<MessageCreateEvent> {
    @Override
    public Class<MessageCreateEvent> getEventType() {
        return MessageCreateEvent.class;
    }

    @Override
    public Mono<Void> execute(MessageCreateEvent event) {
        if (!event.getMessage().getContent().startsWith("!")) {
            return Mono.empty();
        }

        final MessageEventContext eventContext = new MessageEventContext(event);

        if(eventContext.isBot()) {
            return Mono.empty();
        }

        final BaseCommand command = CommandOrchestrator.getInstance().getCommand(eventContext.getCommandName());

        if (command == null) {
            return Mono.empty();
        }

        try {
            Mono<Void> result = command.execute(eventContext);
            event.getMessage().addReaction(ReactionEmoji.unicode(Emoji.CHECK_MARK)).block();
            return result;
        } catch (Exception e) {
            Logger.write(e.toString());
            event.getMessage().addReaction(ReactionEmoji.unicode(Emoji.CROSS_MARK)).block();
            return Mono.empty();
        }
    }
}
