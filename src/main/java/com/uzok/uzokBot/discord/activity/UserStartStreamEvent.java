package com.uzok.uzokBot.discord.activity;

import com.uzok.uzokBot.dataBase.GetSubscribersByUserTag;
import com.uzok.uzokBot.dataBase.JavaToMySQL;
import com.uzok.uzokBot.twitch.Client;
import com.uzok.uzokBot.twitch.responses.UsersResponse;
import com.uzok.uzokBot.utils.PresenceEventContext;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class UserStartStreamEvent extends BaseActivityEvent {
    //    private GatewayDiscordClient discordClient;
    private static Map<String, String> twitchDiscordUsersMap;

    public UserStartStreamEvent() {
        activityEventName = "userStartStream";
        if (twitchDiscordUsersMap == null) {
            twitchDiscordUsersMap = new LinkedHashMap<>();
        }
//        discordClient = com.uzok.uzokBot.DiscordBot.getDiscordClient();
    }

    @Override
    public Mono<Void> execute(PresenceEventContext context) {
        String userTag = Objects.requireNonNull(context.getUser().block(Duration.ofMillis(100))).getTag();
        String twitchUserName;

        List<String> subs = (List<String>) (new JavaToMySQL().executeQuery(new GetSubscribersByUserTag(userTag)));

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

            Client.getInstance().postSubOnStreamChange(usersResponse.data.get(0).id);

//            StreamsResponse streamsResponse = Client.getInstance().getStreamInfo(twitchUserName);
//            Stream stream = streamsResponse.data.get(0);
//            String gameId = stream.game_id;
//            GamesResponse gamesResponse = Client.getInstance().getGameInfo(gameId);

//            return (discordClient.getUsers().filter(User -> subs.contains(User.getTag())).flatMap(User::getPrivateChannel))
//                    .flatMap(channel -> channel.createEmbed(
//                            spec -> spec.setColor(new Color(255, 0, 0))
//                                    .setAuthor(user.display_name, null, user.profile_image_url)//, ANY_URL, IMAGE_URL)
//                                    .setImage(stream.thumbnail_url.replace("{width}x{height}", "440x248"))
//                                    .setTitle(stream.title)
//                                    .setUrl(streamingUrl)
//                                    .addField("Стримит", gamesResponse.data.get(0).name, true)
//                                    .setThumbnail(gamesResponse.data.get(0).box_art_url.replace("{width}x{height}", "285x380"))
//                                    .setFooter("Для отписки напиши !unsub " + userTag, null)
//                                    .setTimestamp(Instant.now())))
//                    .then();
        } catch (Exception e) {
            e.printStackTrace();
//            return Mono.empty();
        }
        return Mono.empty();
    }

    public static String getDiscordTagByTwitchName(String twitchName){
        return twitchDiscordUsersMap.remove(twitchName);
    }
}
