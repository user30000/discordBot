package com.uzok.uzokBot.utils;

import discord4j.core.event.domain.PresenceUpdateEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.presence.Presence;
import reactor.core.publisher.Mono;

public class PresenceEventContext {
    private final PresenceUpdateEvent event;

    public PresenceEventContext(PresenceUpdateEvent event) {
        this.event = event;
    }

    public Mono<User> getUser() {
        return event.getUser();
    }

    public Presence getCurrentPresence() {
        return event.getCurrent();
    }
}
