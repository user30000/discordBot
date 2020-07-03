package com.uzok.uzokBot.discord.command;

import com.uzok.uzokBot.utils.MessageEventContext;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

import java.time.Instant;

public class Test extends BaseCommand {
    Test() {
        commandName = "test";
    }

    @Override
    public Mono<Void> execute(MessageEventContext context) {
        return context.getChannel().flatMap(channel -> channel.createEmbed(spec -> spec.setColor(new Color(255, 0, 0))
                                .setAuthor(context.getUsername(), null, context.getAvatarUrl())
                                .setImage(context.getAvatarUrl())
                                .setTitle("setTitle/setUrl")
                                .setUrl("https://www.twitch.tv/user30000")
                                .setDescription("setDescription\n" +
                                        "big D: is setImage\n" +
                                        "small D: is setThumbnail\n" +
                                        "<-- setColor")
                                .addField("addField", "inline = true", true)
                                .addField("addFIeld", "inline = true", true)
                                .addField("addFile", "inline = false", false)
                                .setThumbnail(context.getAvatarUrl())
                                .setFooter("setFooter --> setTimestamp", context.getAvatarUrl())
                                .setTimestamp(Instant.now())))
                .then();
    }
}
