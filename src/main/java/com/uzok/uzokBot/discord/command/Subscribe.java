package com.uzok.uzokBot.discord.command;

import com.uzok.uzokBot.dataBase.CheckStreamIntoDB;
import com.uzok.uzokBot.dataBase.JavaToMySQL;
import com.uzok.uzokBot.dataBase.SubscribeProcedure;
import com.uzok.uzokBot.twitch.Client;
import com.uzok.uzokBot.twitch.responses.UsersResponse;
import com.uzok.uzokBot.utils.Logger;
import com.uzok.uzokBot.utils.MessageEventContext;
import org.apache.commons.cli.*;
import reactor.core.publisher.Mono;

import java.io.IOException;

public class Subscribe extends BaseCommand {

    Subscribe() {
        commandNames = new String[]{"sub"};
        shortDescription = "Подписка на стримы твич пользователя";
        description = "Принимает на вход название канала.\nБот отправит сообщение в личный чат оначале стрима.";
        example = "!sub twitchuser";
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
        String streamerTag = commandLine.getArgs()[0].toLowerCase();
        if (streamerTag.isEmpty()) {
            return Mono.empty();
        }

        //check twitch for username
        UsersResponse usersResponse;
        try {
            usersResponse = Client.getInstance().getUserInfo(streamerTag);
        } catch (IOException e) {
            Logger.write(e.getMessage() + " when command was " + context.getCommandLine());
            return Mono.empty();
        }
        if (usersResponse.data.isEmpty()) {
            return Mono.empty();
        }

        //check username into DB
        if (!(boolean) (JavaToMySQL.getInstance().executeQuery(new CheckStreamIntoDB(streamerTag)))) {
            try {
                Client.getInstance().postSubOnStreamChange(usersResponse.data.get(0).id);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (commandLine.hasOption("c")) {
            //options means channel owner want to subscribe this channel
            if (!context.isPrivateChannel() && context.isOwner()) {
                if (commandLine.hasOption("e")) {
                    isEveryone = true;
                }
                JavaToMySQL.getInstance().executeCall(new SubscribeProcedure(streamerTag, context.getGuildId().get().asLong(), context.getChannelId().asLong(), isEveryone));
                return context.getChannel().flatMap(channel -> channel.createMessage("Ты подписался на " + streamerTag))
                        .then();
            }
        } else {
            //user subscribe by himself
            JavaToMySQL.getInstance().executeCall(new SubscribeProcedure(streamerTag, context.getAuthor().getTag()));
            return context.getChannel().flatMap(channel -> channel.createMessage("Ты подписался на " + streamerTag))
                    .then();
        }

        return Mono.empty();
    }
}
