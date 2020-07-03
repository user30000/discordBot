package com.uzok.uzokBot;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.Event;
import com.uzok.uzokBot.discord.listener.EventListener;
import com.uzok.uzokBot.discord.listener.MessageCreateListener;
import com.uzok.uzokBot.discord.listener.PresenceUpdateListener;
import com.uzok.uzokBot.twitch.Client;
import com.uzok.uzokBot.utils.Prop;

/**
 * Created by BigDuke on 23.09.2018.
 */
public class DiscordBot {
    private static GatewayDiscordClient discordClient;

    public static void main(String[] args) throws Exception {
        Client.getInstance().build(Prop.getProp("client_id"), Prop.getProp("client_secret"));

        final String token = args[0];
        discordClient = DiscordClientBuilder.create(token).build().login().block();

        WebHookReceiver.start();

        assert discordClient != null;

        register(discordClient, new PresenceUpdateListener());
        register(discordClient, new MessageCreateListener());

        discordClient.onDisconnect().block();
    }

    private static <T extends Event> void register(GatewayDiscordClient gateway, EventListener<T> eventListener) {
        gateway.getEventDispatcher().on(eventListener.getEventType()).flatMap(eventListener::execute).subscribe();
    }

    static GatewayDiscordClient getDiscordClient(){
        return discordClient;
    }
}
