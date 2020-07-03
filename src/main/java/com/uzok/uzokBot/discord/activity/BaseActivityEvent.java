package com.uzok.uzokBot.discord.activity;

import reactor.core.publisher.Mono;
import com.uzok.uzokBot.utils.PresenceEventContext;

public abstract class BaseActivityEvent {
    public String activityEventName;
    public abstract Mono<Void> execute(PresenceEventContext context);
}
