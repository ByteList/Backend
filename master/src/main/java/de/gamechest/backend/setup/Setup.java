package de.gamechest.backend.setup;

import de.gamechest.backend.Backend;
import de.gamechest.backend.Callback;
import de.gamechest.backend.config.BackendDocument;
import de.gamechest.backend.config.Document;
import de.gamechest.backend.database.DatabaseManager;
import de.gamechest.backend.log.BackendLogger;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.Executors;

/**
 * Created by ByteList on 13.02.2018.
 * <p>
 * Copyright by ByteList - https://bytelist.de/
 */
public class Setup implements Runnable {

    public static int setupState;
    @Getter
    private static String setupStateValue;

    private final Backend backend;
    private final BackendLogger logger;
    private final File backendFile;

    public Setup(Backend backend, File backendFile) {
        setupState = 0;
        setupStateValue = "{null}";
        this.backend = backend;
        this.logger = backend.getLogger();
        this.backendFile = backendFile;
    }

    @Override
    public void run() {
        logger.info("*****************************************************************************");
        logger.info("*** Can not find backend-file! If you are setting up the system continue. ***");
        logger.info("***                                                                       ***");
        logger.info("***                        You entered the setup.                         ***");
        logger.info("***   The setup will start in 15 seconds. Please save the backend-uid.    ***");
        logger.info("*****************************************************************************");
//        sleep(15000);
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.info(" ");
        logger.info("*** Starting with setup...");
        String backendUid = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        logger.info("***                                                                       ***");
        logger.info("***                          Your backend-uid:                            ***");
        logger.info("--->  "+backendUid+"   <---");
        logger.info("*****************************************************************************");
//        sleep(4000);
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.info(" ");
        logger.info("*** Creating data structure...");
        createFiles();
        BackendDocument backendDocument = new BackendDocument(backendUid);
        if(!save(backendDocument, backendFile)) {
            return;
        }
        logger.info("*****************************************************************************");
//        sleep(4000);
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.info(" ");
        logger.info("*** Now setup the mongodb-database...");
        Document databaseDocument = new Document();
        logger.info(" ");
        logger.info("Please enter the database host:");
        setupState = 1;
        waitOnInput((input)-> databaseDocument.append("host", input));
        setupState = 2;
        logger.info(" ");
        logger.info("Please enter the database port:");
        waitOnInput((input)-> databaseDocument.append("port", input));
        setupState = 3;
        logger.info(" ");
        logger.info("Please enter the database user:");
        waitOnInput((input)-> databaseDocument.append("user", input));
        setupState = 4;
        logger.info(" ");
        logger.info("Please enter the database password:");
        waitOnInput((input)-> databaseDocument.append("password", input));
        setupState = 5;
        logger.info(" ");
        logger.info("Please enter the database:");
        waitOnInput((input)-> databaseDocument.append("database", input));
        logger.info(" ");

        backendDocument.append("database", databaseDocument);
        if(!save(backendDocument, backendFile)) {
            return;
        }
        setupState = 6;
        if(!testDatabaseConnection(databaseDocument.getString("host"), databaseDocument.getInt("port"),
                databaseDocument.getString("user"), databaseDocument.getString("password"), databaseDocument.getString("database"))) {
            logger.warning("***");
            logger.warning("*** You can change the database-values in backend-file");
            logger.warning("*** and test it while running the command: testdb");
        }
        logger.info("*****************************************************************************");
//        sleep(4000);
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.info(" ");
        logger.info("*** Now setup the json-web-api...");
        logger.info(" ");
        logger.info("Please enter a port for the api service:");
        waitOnInput((input)-> backendDocument.append("web-api-port", Integer.parseInt(input)));
        if(!save(backendDocument, backendFile)) {
            return;
        }
        setupState = 7;
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void createFiles() {
        try {
            backendFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean save(Document document, File file) {
        if(document.saveAsConfig(file)) {
            logger.info("Saved "+document.getName()+" to "+file.getAbsolutePath());
            return true;
        } else {
            if(file.exists()) file.delete();
            cancelSetup();
            return false;
        }
    }

    private void cancelSetup() {
        logger.warning("*****************************************************************************");
        logger.warning("***                Setup cancelled! Check log for details!                ***");
        logger.warning("*****************************************************************************");
        sleep(1500);
        backend.setInSetUp(false);
        backend.stop();
    }

    private void waitOnInput(Callback<String> callback) {
        while (true) {
            if(!getSetupStateValue().equals("{null}")) {
                Executors.newSingleThreadExecutor().execute(()-> {
                    callback.run(getSetupStateValue());
                    System.out.println("waitOnInput()-> "+ getSetupStateValue());
                });
                break;
            }
        }
    }

    public static boolean execute(String line) {
        setupStateValue = line;
        return true;
    }

    public static boolean testDatabaseConnection(String host, int port, String user, String password, String database) {
        BackendLogger logger = Backend.getInstance().getLogger();
        logger.info("Testing database connection...");
        try {
            DatabaseManager databaseManager = new DatabaseManager(Backend.getInstance().getBackendUid(), Backend.getInstance().getVersion(), host, port, user, password, database);
            logger.info("Database connection test successful.");
            databaseManager.getMongoClient().close();
            return true;
        } catch (Exception e) {
            logger.warning("*****************************************************************************");
            logger.warning("***                  Error while test database connection:                ***");
            e.printStackTrace();
            logger.warning("*****************************************************************************");
        }
        return false;
    }
}
