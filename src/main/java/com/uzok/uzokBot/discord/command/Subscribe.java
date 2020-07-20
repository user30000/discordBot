package com.uzok.uzokBot.discord.command;

import com.uzok.uzokBot.dataBase.JavaToMySQL;
import com.uzok.uzokBot.dataBase.SubscribeProcedure;
import com.uzok.uzokBot.utils.MessageEventContext;
import org.apache.commons.cli.*;
import reactor.core.publisher.Mono;

public class Subscribe extends BaseCommand {

    Subscribe() {
        commandName = "sub";
        shortDescription = "Подписка на стримы пользователя";
        description = "Принимает на вход тэг пользователя.\nЕсли этот пользователь начинает трансляцию на Twitch.tv то бот отправит сообщение в личный чат об этом событии.";
        example = "!sub userName#0000";
    }

    @Override
    public Mono<Void> execute(MessageEventContext context) {
        CommandLineParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("c", "channel", false, "Subscribe channel on stream");
        options.addOption("e", "everyone", false, "Add @everyone on event message");

        if (context.getArg() == null || context.getArg().isEmpty()) {
            return Mono.empty();
        }

        String[] args = context.getArg().split(" ");
        String streamerTag;
        boolean isEveryone = false;

        try {
            CommandLine commandLine = parser.parse(options, args);
            if (commandLine.getArgs().length != 1) {
                return Mono.empty();
            }
            streamerTag = commandLine.getArgs()[0];
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
        } catch (ParseException e) {
            System.out.println("Parse error:" + e.getMessage());
        } catch (Exception e) {
            System.out.println("Unknown error:" + e.getMessage());
        }

        return Mono.empty();
    }
}
