package com.uzok.uzokBot.discord.activity;

import java.util.HashMap;
import java.util.Map;

public class ActivityEventOrchestrator {
    private static ActivityEventOrchestrator instance;

    static {
        ActivityEventOrchestrator.instance = new ActivityEventOrchestrator();
    }

    private final Map<String, BaseActivityEvent> eventsMap;

    private ActivityEventOrchestrator() {
        eventsMap = init();
    }

    private Map<String, BaseActivityEvent> init(BaseActivityEvent... events) {
        final Map<String, BaseActivityEvent> map = new HashMap<>();
        for (final BaseActivityEvent event : events) {
            map.put(event.activityEventName, event);
        }
        return map;
    }

    public Map<String, BaseActivityEvent> getEvents() {
        return eventsMap;
    }

    public static ActivityEventOrchestrator getInstance() {
        return ActivityEventOrchestrator.instance;
    }
}
