package com.uzok.uzokBot.discord.listener;

import com.uzok.uzokBot.discord.activity.ActivityEventOrchestrator;
import com.uzok.uzokBot.discord.activity.BaseActivityEvent;
import com.uzok.uzokBot.utils.context.ReactionAddEventContext;
import discord4j.core.event.domain.message.ReactionAddEvent;
import reactor.core.publisher.Mono;

public class ReactionAddListener implements EventListener<ReactionAddEvent> {
    @Override
    public Class<ReactionAddEvent> getEventType() {
        return ReactionAddEvent.class;
    }

    @Override
    public Mono<Void> execute(ReactionAddEvent event) {
//        final Presence currentPresence = event.getCurrent();
//        final Optional<Presence> oldPresence = event.getOld();

        final ReactionAddEventContext eventContext = new ReactionAddEventContext(event);
        final BaseActivityEvent activityEvent = ActivityEventOrchestrator.getInstance().getEvents().get("userAddReaction");
        if (activityEvent == null) {
            return Mono.empty();
        }
        return activityEvent.execute(eventContext);
    }
}
