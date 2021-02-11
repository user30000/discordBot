package com.uzok.uzokBot.discord.activity;

import com.uzok.uzokBot.utils.context.EventContext;
import com.uzok.uzokBot.utils.context.ReactionAddEventContext;
import reactor.core.publisher.Mono;

public class UserAddReactionEvent extends BaseActivityEvent {
    UserAddReactionEvent() {
        activityEventName = "userAddReaction";
    }

    @Override
    public Mono<Void> execute(EventContext context) {
        ReactionAddEventContext currentContext = (ReactionAddEventContext) context;
        if (!currentContext.isBot()) {
            System.out.println(currentContext.getUser());
        }
        return Mono.empty();
    }
}
