package de.gamechest.backend.console.commands;

import de.gamechest.backend.Backend;
import de.gamechest.backend.console.Command;

/**
 * Created by ByteList on 14.05.2017.
 * <p>
 * Copyright by ByteList - https://bytelist.de/
 */
public class MailTestCommand extends Command {

    public MailTestCommand() {
        super("mailtest", "test the mail client");
    }

    @Override
    public void execute(String[] args) {
        Backend.getInstance().getMailClient().sendTestMail();
    }
}
