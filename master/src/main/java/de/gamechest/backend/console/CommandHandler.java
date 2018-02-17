package de.gamechest.backend.console;

import lombok.Getter;

import java.util.HashMap;
import java.util.regex.Pattern;

import static de.gamechest.backend.util.Java15Compat.Arrays_copyOfRange;

/**
 * Created by ByteList on 30.05.2017.
 * <p>
 * Copyright by ByteList - https://bytelist.de/
 */
public class CommandHandler {

    private static final Pattern PATTERN_ON_SPACE = Pattern.compile(" ", Pattern.LITERAL);

    @Getter
    private HashMap<String, Command> commands;

    public CommandHandler() {
        this.commands = new HashMap<>();
    }

    public void registerCommand(Command command) {
        this.commands.put(command.getName(), command);
    }

    public Command getCommand(String command) {
        if(command.contains(" "))
            command = command.split(" ")[0];
        return commands.getOrDefault(command, null);
    }

    public boolean existsCommand(String command) {
        if(command.contains(" "))
            command = command.split(" ")[0];
        return commands.containsKey(command);
    }

    public boolean dispatchCommand(String commandLine) {
        String[] args = PATTERN_ON_SPACE.split(commandLine);

        if (args.length == 0) {
            return false;
        }

        String sentCommandLabel = args[0].toLowerCase();
        Command target = getCommand(sentCommandLabel);

        if (target == null) {
            return false;
        }

        target.execute(Arrays_copyOfRange(args, 1, args.length));

        return true;
    }
}
