package de.gamechest.backend.console.commands;

import de.gamechest.backend.Backend;
import de.gamechest.backend.console.Command;
import de.gamechest.backend.console.CommandHandler;

/**
 * Created by ByteList on 10.06.2017.
 * <p>
 * Copyright by ByteList - https://bytelist.de/
 */
public class HelpCommand extends Command {

    public HelpCommand() {
        super("help", "list all commands");
    }

    private final CommandHandler commandHandler = Backend.getInstance().getCommandHandler();

    @Override
    public void execute(String[] args) {
        System.out.println("All cloud commands: ");
        for(String command : commandHandler.getCommands().keySet()) {
            System.out.println(command+" - "+commandHandler.getCommand(command).getDescription());
        }
    }
}
