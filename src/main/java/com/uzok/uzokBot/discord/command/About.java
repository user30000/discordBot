package com.uzok.uzokBot.discord.command;

import com.uzok.uzokBot.utils.MessageEventContext;
import com.uzok.uzokBot.utils.Prop;
import reactor.core.publisher.Mono;

public class About extends BaseCommand {
    About() {
        commandNames = new String[]{"about"};
    }

    @Override
    public Mono<Void> execute(MessageEventContext context) {

        String commandsStringBuilder = "Автор: " +
                Prop.getProp("author") +
                "\nGit repo: " +
                Prop.getProp("gitLink");
        return context.getAuthor().getPrivateChannel()
                .flatMap(privateChannel ->
                        privateChannel.createMessage(message ->
                                message.setContent(Prop.getProp("discordLink"))
                                        .setEmbed(spec ->
                                                spec.setDescription(commandsStringBuilder))))
                .then();
    }
}
