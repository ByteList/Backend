package de.gamechest.backend.bootstrap;

import de.gamechest.backend.Backend;
import de.gamechest.backend.config.Document;
import de.gamechest.backend.setup.Setup;

/**
 * Created by ByteList on 12.02.2018.
 * <p>
 * Copyright by ByteList - https://bytelist.de/
 */
public class Launcher {

    public static void main(String[] args) throws Exception {
        Backend backend = new Backend();
        String line;

        if(!backend.getBackendFile().exists()) {
            if(!backend.isInSetUp()) {
                backend.startSetUp(backend.getBackendFile());
            }

            while (backend.isInSetUp() && (line = backend.getConsoleReader().readLine(">")) != null) {
                int state = Setup.setupState;

                if(state > 0 && state < 6) {
                    if(!Setup.execute(line)) {
                        Backend.getInstance().getLogger().info("Error");
                    }
                }

                if(line.equalsIgnoreCase("testdb")) {
                    Document backendDocument = Document.loadDocument(backend.getBackendFile()),
                            databaseDocument = backendDocument.getDocument("database");
                    Setup.testDatabaseConnection(databaseDocument.getString("host"), databaseDocument.getInt("port"),
                            databaseDocument.getString("user"), databaseDocument.getString("password"), databaseDocument.getString("database"));
                }
            }
        } else {
            backend.getLogger().info("Enabled Backend version " + backend.getVersion() + ".");
            backend.start();

            while (backend.isRunning && (line = backend.getConsoleReader().readLine(">")) != null) {
                if (!backend.getCommandHandler().dispatchCommand(line)) {
                    backend.getLogger().info("** Command not found");
                }
            }
        }

    }

}
