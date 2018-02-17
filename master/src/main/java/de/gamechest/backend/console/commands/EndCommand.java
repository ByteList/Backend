package de.gamechest.backend.console.commands;

import de.gamechest.backend.Backend;
import de.gamechest.backend.console.Command;

/**
 * Created by ByteList on 14.05.2017.
 * <p>
 * Copyright by ByteList - https://bytelist.de/
 */
public class EndCommand extends Command {

    public EndCommand() {
        super("end", "shutdown the updater");
    }

    @Override
    public void execute(String[] args) {
        Backend.getInstance().stop();
    }
}
