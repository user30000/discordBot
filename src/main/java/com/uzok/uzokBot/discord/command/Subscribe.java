package com.uzok.uzokBot.discord.command;

import com.uzok.uzokBot.dataBase.JavaToMySQL;
import com.uzok.uzokBot.dataBase.SubscribeProcedure;
import com.uzok.uzokBot.utils.MessageEventContext;
import org.apache.commons.cli.*;
import reactor.core.publisher.Mono;

public class Subscribe extends BaseCommand {

    Subscribe() {
        commandNames = new String[]{"sub"};
        shortDescription = "Подписка на стримы пользователя";
        description = "Принимает на вход тэг пользователя.\nЕсли этот пользователь начинает трансляцию на Twitch.tv то бот отправит сообщение в личный чат об этом событии.";
        example = "!sub userName#0000";
    }

    @Override
    public Mono<Void> execute(MessageEventContext context) {
        Options options = new Options();
        options.addOption("c", "channel", false, "Subscribe channel on stream");
        options.addOption("e", "everyone", false, "Add @everyone on event message");

        boolean isEveryone = false;

        CommandLine commandLine = context.getCommandLine(options);
        if (commandLine == null || commandLine.getArgs().length != 1) {
            return Mono.empty();
        }
        String streamerTag = commandLine.getArgs()[0];
        if (streamerTag == null || streamerTag.isEmpty() || !streamerTag.matches("\\S+#\\d{4}")) {
            return Mono.empty();
        }

        if (commandLine.hasOption("c")) {
            //options means channel owner want to subscribe this channel
            if (!context.isPrivateChannel() && context.isOwner()) {
                if (commandLine.hasOption("e")) {
                    isEveryone = true;
                }
                new JavaToMySQL().executeCall(new SubscribeProcedure(streamerTag, context.getGuildId().get().asLong(), context.getChannelId().asLong(), isEveryone));
                return context.getChannel().flatMap(channel -> channel.createMessage("Ты подписался на " + streamerTag))
                        .then();
            }
        } else {
            //user subscribe 4 himself
            new JavaToMySQL().executeCall(new SubscribeProcedure(streamerTag, context.getAuthor().getTag()));
            return context.getChannel().flatMap(channel -> channel.createMessage("Ты подписался на " + streamerTag))
                    .then();
        }

        return Mono.empty();
    }
}
