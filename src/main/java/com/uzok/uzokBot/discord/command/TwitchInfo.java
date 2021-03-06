package com.uzok.uzokBot.discord.command;

import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;
import com.uzok.uzokBot.twitch.Client;
import com.uzok.uzokBot.twitch.dtos.Stream;
import com.uzok.uzokBot.twitch.dtos.User;
import com.uzok.uzokBot.twitch.responses.GamesResponse;
import com.uzok.uzokBot.twitch.responses.StreamsResponse;
import com.uzok.uzokBot.twitch.responses.UserFollowsResponse;
import com.uzok.uzokBot.twitch.responses.UsersResponse;
import com.uzok.uzokBot.utils.context.MessageEventContext;

import java.io.IOException;
import java.time.Instant;

public class TwitchInfo extends BaseCommand {
    TwitchInfo() {
        commandNames = new String[]{"twitch"};
    }

    @Override
    public Mono<Void> execute(MessageEventContext context) throws IOException {
        try {
            String userName = context.getArg();
            UsersResponse usersResponse = Client.getInstance().getUserInfo(userName);
            if(usersResponse.data.isEmpty()){
                return Mono.empty();
            }
            User user = usersResponse.data.get(0);

            UserFollowsResponse userFollowsResponse = Client.getInstance().getUserFollowers(user.id);

            StreamsResponse streamsResponse = Client.getInstance().getStreamInfo(userName);
            if(streamsResponse.data.isEmpty()){
                return context.getChannel().flatMap(channel -> channel.createEmbed(
                        spec -> spec.setColor(Color.of(255, 0, 0))
                                .setAuthor(user.display_name, null, user.profile_image_url)
                                .setImage(user.offline_image_url)
                                .setTitle("Stream offline")
                                .setUrl("https://www.twitch.tv/" + user.display_name)
                                .addField("Фолловеров", String.valueOf(userFollowsResponse.total), true)
                                .setTimestamp(Instant.now())))
                        .then();
            }

            Stream stream = streamsResponse.data.get(0);
            String gameId = stream.game_id;
            GamesResponse gamesResponse = Client.getInstance().getGameInfo(gameId);

            return context.getChannel().flatMap(channel -> channel.createEmbed(
                    spec -> spec.setColor(Color.of(255, 0, 0))
                            .setAuthor(user.display_name, null, user.profile_image_url)
                            .setImage(stream.thumbnail_url
                                    .replace("{width}x{height}", "440x248")
                                    .concat("?r=")
                                    .concat(String.valueOf(System.currentTimeMillis())))
                            .setTitle(stream.title)
                            .setUrl("https://www.twitch.tv/" + user.display_name)
                            .addField("Стримит", stream.game_name, true)
                            .addField("Зрители", String.valueOf(stream.viewer_count), true)
                            .setThumbnail(gamesResponse.data.get(0).box_art_url.replace("{width}x{height}", "285x380"))
                            .setTimestamp(Instant.now())))
                    .then();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
