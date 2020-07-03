package com.uzok.uzokBot.discord.listener;

import com.uzok.uzokBot.discord.activity.ActivityEventOrchestrator;
import com.uzok.uzokBot.discord.activity.BaseActivityEvent;
import discord4j.core.event.domain.PresenceUpdateEvent;
import discord4j.core.object.presence.Presence;
import reactor.core.publisher.Mono;
import com.uzok.uzokBot.utils.PresenceEventContext;

import java.util.Optional;

public class PresenceUpdateListener implements EventListener<PresenceUpdateEvent> {
    @Override
    public Class<PresenceUpdateEvent> getEventType() {
        return PresenceUpdateEvent.class;
    }

    @Override
    public Mono<Void> execute(PresenceUpdateEvent event) {
        final Presence currentPresence = event.getCurrent();
        final Optional<Presence> oldPresence = event.getOld();

        String actName = discriminateEvent(currentPresence, oldPresence.orElse(null));

        final PresenceEventContext eventContext = new PresenceEventContext(event);
        final BaseActivityEvent activityEvent = ActivityEventOrchestrator.getInstance().getEvents().get(actName);
        if (activityEvent == null) {
            return Mono.empty();
        }
        return activityEvent.execute(eventContext);
    }

    private String discriminateEvent(Presence current, Presence old) {
        int activityTypeDiff = current.getActivities().stream().mapToInt(x -> x.getType().getValue()).sum();

        if (old != null) {
            activityTypeDiff -= old.getActivities().stream().mapToInt(x -> x.getType().getValue()).sum();
        }
        switch (activityTypeDiff) {
            case 1:
                return "userStartStream";
            case -1:
                return "userEndStream";
            default:
                return "none";
        }
    }
}
