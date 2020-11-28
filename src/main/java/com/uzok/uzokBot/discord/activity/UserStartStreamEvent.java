package com.uzok.uzokBot.discord.activity;

import com.uzok.uzokBot.DiscordBot;
import com.uzok.uzokBot.dataBase.GetSubscribersByUserTag;
import com.uzok.uzokBot.dataBase.JavaToMySQL;
import com.uzok.uzokBot.twitch.Client;
import com.uzok.uzokBot.twitch.dtos.Stream;
import com.uzok.uzokBot.twitch.responses.GamesResponse;
import com.uzok.uzokBot.twitch.responses.StreamsResponse;
import com.uzok.uzokBot.twitch.responses.UsersResponse;
import com.uzok.uzokBot.utils.PresenceEventContext;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class UserStartStreamEvent extends BaseActivityEvent {
    private static Map<String, String> twitchDiscordUsersMap;

    UserStartStreamEvent() {
        activityEventName = "userStartStream";
        if (twitchDiscordUsersMap == null) {
            twitchDiscordUsersMap = new LinkedHashMap<>();
        }
    }

    @Override
    public Mono<Void> execute(PresenceEventContext context) {
        String userTag = Objects.requireNonNull(context.getUser().block(Duration.ofMillis(100))).getTag();
        String twitchUserName;

        List<?> subs = (List<?>) (JavaToMySQL.getInstance().executeQuery(new GetSubscribersByUserTag(userTag)));

        if (subs.isEmpty()) {
            return Mono.empty();
        }

        String streamingUrl = context.getCurrentPresence().getActivities().stream()
                .filter(activity -> activity.getStreamingUrl().isPresent())
                .findFirst().orElseThrow(RuntimeException::new)
                .getStreamingUrl().orElseThrow(RuntimeException::new);

        try {
            twitchUserName = new URL(streamingUrl).getPath().substring(1);
            twitchDiscordUsersMap.put(twitchUserName, userTag);
        } catch (MalformedURLException e) {
            return Mono.empty();
        }

        try {
            UsersResponse usersResponse = Client.getInstance().getUserInfo(twitchUserName);
            if (usersResponse.data.isEmpty()) {
                return Mono.empty();
            }

            StreamsResponse streamsResponse = Client.getInstance().getStreamInfo(twitchUserName);
            if (!streamsResponse.data.isEmpty()) {
                List<String> usersTags = new LinkedList<>();
                List<Long> guildsIds = new LinkedList<>();
                List<Long> channelsIds = new LinkedList<>();

                Stream stream = streamsResponse.data.get(0);
                String gameId = stream.game_id;
                GamesResponse gamesResponse = Client.getInstance().getGameInfo(gameId);

                subs.forEach(subscriber -> {
                    GetSubscribersByUserTag.subscriber castedSub = (GetSubscribersByUserTag.subscriber) subscriber;
                    if (castedSub.subTag != null) {
                        usersTags.add(castedSub.subTag);
                    } else {
                        guildsIds.add(castedSub.guidSnowflake);
                        channelsIds.add(castedSub.channelSnowflake);
                    }
                });

                DiscordBot.getDiscordClient()
                        .getUsers()
                        .filter(User -> usersTags.contains(User.getTag()))
                        .flatMap(discord4j.core.object.entity.User::getPrivateChannel)
                        .flatMap(channel -> channel.createEmbed(
                                spec -> spec.setColor(Color.of(255, 0, 0))
                                        .setAuthor(stream.user_name, null, null)
                                        .setImage(stream.thumbnail_url.replace("{width}x{height}", "440x248"))
                                        .setTitle(stream.title)
                                        .setUrl("https://www.twitch.tv/" + stream.user_name)
                                        .addField("Стримит", gamesResponse.data.get(0).name, true)
                                        .setThumbnail(gamesResponse.data.get(0).box_art_url.replace("{width}x{height}", "285x380"))
                                        .setFooter("Для отписки напиши !unsub " + userTag, null)
                                        .setTimestamp(Instant.now()))).blockLast();

                DiscordBot.getDiscordClient()
                        .getGuilds()
                        .filter(guild -> guildsIds.contains(guild.getId().asLong()))
                        .flatMap(Guild::getChannels)
                        .filter(channel -> channelsIds.contains(channel.getId().asLong()))
                        .flatMap(channel ->
                                ((MessageChannel) channel).createMessage(message -> message.setEmbed(
                                        spec -> spec.setColor(Color.of(255, 0, 0))
                                                .setAuthor(stream.user_name, null, null)
                                                .setImage(stream.thumbnail_url
                                                        .replace("{width}x{height}", "440x248")
                                                        .concat("?r=")
                                                        .concat(String.valueOf(System.currentTimeMillis())))
                                                .setTitle(stream.title)
                                                .setUrl("https://www.twitch.tv/" + stream.user_name)
                                                .addField("Стримит", gamesResponse.data.get(0).name, true)
                                                .setThumbnail(gamesResponse.data.get(0).box_art_url.replace("{width}x{height}", "285x380"))
                                                .setFooter("Для отписки напиши !unsub " + userTag, null)
                                                .setTimestamp(Instant.now())))
                        ).blockLast();

                return Mono.empty();
            }

//todo Возможно, если стрим начнется без игры, то дискорд не зачекает этого.
            Client.getInstance().postSubOnStreamChange(usersResponse.data.get(0).id);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return Mono.empty();
    }

    public static String getDiscordTagByTwitchName(String twitchName) {
        return twitchDiscordUsersMap.remove(twitchName);
    }
}
