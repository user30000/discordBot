package com.uzok.uzokBot.discord.activity;

import com.uzok.uzokBot.utils.context.EventContext;
import reactor.core.publisher.Mono;
import com.uzok.uzokBot.utils.context.PresenceEventContext;

public abstract class BaseActivityEvent {
    String activityEventName;
    public abstract Mono<Void> execute(EventContext context);
}
