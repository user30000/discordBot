package com.uzok.uzokBot.discord.command;

import com.uzok.uzokBot.utils.MessageEventContext;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import reactor.core.publisher.Mono;

import java.util.Random;

public class Randomizer extends BaseCommand {

    Randomizer() {
        commandNames = new String[]{"random", "roll", "r"};
    }

    @Override
    public Mono<Void> execute(MessageEventContext context) {
        Options options = new Options();
        options.addOption("u", "user", false, "Get random non-bot user");

        CommandLine commandLine = context.getCommandLine(options);

        Random r = new Random();

        return context.getChannel().flatMap(messageChannel -> messageChannel.createMessage(String.valueOf(r.nextInt(1000))))
                .then();
    }
}
