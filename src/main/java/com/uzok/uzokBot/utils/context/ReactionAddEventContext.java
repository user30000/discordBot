package com.uzok.uzokBot.utils.context;

import discord4j.core.event.domain.PresenceUpdateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.presence.Presence;
import reactor.core.publisher.Mono;

public class ReactionAddEventContext extends EventContext {
    private final ReactionAddEvent event;

    public ReactionAddEventContext(ReactionAddEvent event) {
        this.event = event;
    }

    public User getUser() {
        return this.event.getUser().block();
    }

    public boolean isBot() {
       return getUser().isBot();
    }

//    public Presence getCurrentPresence() {
//        return event.getCurrent();
//    }
}
