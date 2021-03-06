package com.uzok.uzokBot.discord.command;

import java.util.HashMap;
import java.util.Map;

public class CommandOrchestrator {
    private static CommandOrchestrator instance;

    static {
        CommandOrchestrator.instance = new CommandOrchestrator();
    }

    private final Map<String, BaseCommand> commandsMap;

    private CommandOrchestrator() {
        commandsMap = init(new Subscribe(),
                new Unsubscribe(),
                new TwitchInfo(),
                new Help(),
                new About(),
                new InviteLink(),
                // new Randomizer(),
                new Weather());
    }

    private Map<String, BaseCommand> init(BaseCommand... commands) {
        final Map<String, BaseCommand> map = new HashMap<>();
        for (final BaseCommand command : commands) {
            for (String commandName : command.commandNames)
                map.put(commandName, command);
        }
        return map;
    }

    public BaseCommand getCommand(String commandName) {
        return commandsMap.get(commandName);
    }

    Map<String, BaseCommand> getCommands() {
        return commandsMap;
    }

    public static CommandOrchestrator getInstance() {
        return CommandOrchestrator.instance;
    }
}
