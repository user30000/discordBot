package com.uzok.uzokBot.discord.command;

import com.uzok.uzokBot.utils.MessageEventContext;
import reactor.core.publisher.Mono;

public class Help extends BaseCommand {
    Help() {
        commandName = "help";
    }

    @Override
    public Mono<Void> execute(MessageEventContext context) {
        String arg = context.getArg();
        if (arg == null || arg.isEmpty()) {
            StringBuilder commandsStringBuilder = new StringBuilder().append("Доступные команды:\n");

            CommandOrchestrator.getInstance().getCommands().forEach((s, baseCommand) -> {
                commandsStringBuilder.append("!").append(baseCommand.commandName);
                if (baseCommand.shortDescription != null) {
                    commandsStringBuilder.append(" - ").append(baseCommand.shortDescription);
                }
                commandsStringBuilder.append("\n");
            });
            return context.getAuthor().getPrivateChannel()
                    .flatMap(privateChannel ->
                            privateChannel.createMessage(message ->
                                    message.setEmbed(spec ->
                                            spec.setDescription(commandsStringBuilder.toString()))))
                    .then();
        } else if (context.isPrivateChannel()) {
            BaseCommand command = CommandOrchestrator.getInstance().getCommand(arg);
            if (command == null || (command.description == null && command.example == null)) {
                return Mono.empty();
            }

            return context.getChannel()
                    .flatMap(channel ->
                            channel.createMessage(message ->
                            {
                                if (command.description != null) {
                                    message.setContent(command.description);
                                }
                                if (command.example != null) {
                                    message.setEmbed(spec ->
                                            spec.setDescription(command.example));
                                }
                            }))
                    .then();
        }
        return Mono.empty();
    }
}
