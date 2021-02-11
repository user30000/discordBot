package com.uzok.uzokBot.discord.command;

import com.uzok.uzokBot.dataBase.CheckStreamIntoDB;
import com.uzok.uzokBot.dataBase.JavaToMySQL;
import com.uzok.uzokBot.dataBase.SubscribeProcedure;
import com.uzok.uzokBot.twitch.Client;
import com.uzok.uzokBot.twitch.responses.UsersResponse;
import com.uzok.uzokBot.utils.Logger;
import com.uzok.uzokBot.utils.context.MessageEventContext;
import org.apache.commons.cli.*;
import reactor.core.publisher.Mono;

import java.io.IOException;

public class Subscribe extends BaseCommand {

    Subscribe() {
        commandNames = new String[]{"sub"};
        shortDescription = "Подписка на стримы твич пользователя";
        description = "Принимает на вход название канала.\nБот отправит сообщение в личный чат о начале стрима.";
        example = "!sub twitchUser";
    }

    @Override
    public Mono<Void> execute(MessageEventContext context) {
        Options options = new Options();
        options.addOption("e", "everyone", false, "Add @everyone on event message");

        boolean isEveryone = false;

        CommandLine commandLine = context.getCommandLine(options);
        if (commandLine == null || commandLine.getArgs().length != 1) {
            return Mono.empty();
        }
        String twitchChannel = commandLine.getArgs()[0].toLowerCase();
        if (twitchChannel.isEmpty()) {
            return Mono.empty();
        }

        //check twitch for username
        UsersResponse usersResponse;
        try {
            usersResponse = Client.getInstance().getUserInfo(twitchChannel);
        } catch (IOException e) {
            Logger.write(e.getMessage() + " when command was " + context.getCommandLine());
            return Mono.empty();
        }
        if (usersResponse.data.isEmpty()) {
            return Mono.empty();
        }

        //check username into DB
        if (!(boolean) (JavaToMySQL.getInstance().executeQuery(new CheckStreamIntoDB(twitchChannel)))) {
            try {
                Client.getInstance().postSubOnStreamChange(usersResponse.data.get(0).id);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!context.isPrivateChannel() && context.isOwner()) {
            if (commandLine.hasOption("e")) {
                isEveryone = true;
            }
            JavaToMySQL.getInstance().executeCall(new SubscribeProcedure(twitchChannel, context.getGuildId().get().asLong(), context.getChannelId().asLong(), isEveryone));
            return context.getChannel().flatMap(channel -> channel.createMessage("Ты подписался на " + twitchChannel))
                    .then();
        }

        return Mono.empty();
    }
}
