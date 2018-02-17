package de.gamechest.backend;

import de.gamechest.backend.config.Document;
import de.gamechest.backend.console.CommandHandler;
import de.gamechest.backend.console.commands.EndCommand;
import de.gamechest.backend.console.commands.HelpCommand;
import de.gamechest.backend.database.DatabaseManager;
import de.gamechest.backend.log.BackendLogger;
import de.gamechest.backend.log.LoggingOutPutStream;
import de.gamechest.backend.setup.Setup;
import de.gamechest.backend.web.WebService;
import jline.console.ConsoleReader;
import lombok.Getter;
import lombok.Setter;
import org.fusesource.jansi.AnsiConsole;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.Level;

/**
 * Created by ByteList on 12.02.2018.
 * <p>
 * Copyright by ByteList - https://bytelist.de/
 */
public class Backend {

    public boolean isRunning;
    @Getter
    private static Backend instance;
    @Getter
    private ConsoleReader consoleReader;
    @Getter
    private CommandHandler commandHandler;
    @Getter
    private BackendLogger logger;
    @Getter
    private String version = "unknown";
    /**
     * The DatabaseManager is used to manage all database things.
     * Here you can find all mongodb data's.
     */
    @Getter
    private DatabaseManager databaseManager;
    @Getter
    private Document backendDocument;
    @Getter
    private String backendUid;
    @Getter
    private File backendFile;
    @Getter@Setter
    private boolean inSetUp;
    @Getter
    private WebService webService;

    private String stopDate;

    public Backend() throws IOException {
        System.out.println("Init backend...");
        instance = this;
        isRunning = false;
        // 2.0.23:00342580cc947e7bf8d1eeb7fb8650ab456dc3e2
        String[] v = this.getClass().getPackage().getImplementationVersion().split(":");
        // 2.0.23:0034258
        version = v[0]+":"+v[1].substring(0, 7);
        stopDate = System.getProperty("de.gamechest.backend.autostop", "false");

        // This is a workaround for quite possibly the weirdest bug I have ever encountered in my life!
        // When jansi attempts to extract its natives, by default it tries to extract a specific version,
        // using the loading class's implementation version. Normally this works completely fine,
        // however when on Windows certain characters such as - and : can trigger special behaviour.
        // Furthermore this behaviour only occurs in specific combinations due to the parsing done by jansi.
        // For example test-test works fine, but test-test-test does not! In order to avoid this all together but
        // still keep our versions the same as they were, we set the override property to the essentially garbage version
        // ByteCloud. This version is only used when extracting the libraries to their temp folder.
        System.setProperty("library.jansi.version", "Backend");

        AnsiConsole.systemInstall();
        consoleReader = new ConsoleReader();
        consoleReader.setExpandEvents(false);

        logger = new BackendLogger("Backend", consoleReader);
        System.setErr(new PrintStream(new LoggingOutPutStream(logger, Level.SEVERE), true));
        System.setOut(new PrintStream(new LoggingOutPutStream(logger, Level.INFO), true));

        backendFile = new File("./", "backend.json");

        if(!backendFile.exists()) {
            if(!isInSetUp()) {
                return;
            }
        }


        backendDocument = Document.loadDocument(backendFile);
        backendUid = backendDocument.getString("BACKEND_UID");
        try {
            System.out.println(backendDocument.getString("db-database"));
            this.databaseManager = new DatabaseManager(backendUid, version, backendDocument.getString("db-host"), backendDocument.getInt("db-port"),
                    backendDocument.getString("db-user"), backendDocument.getString("db-password"), backendDocument.getString("db-database"));
            this.databaseManager.init();
            this.logger.info("Connected to database.");
        } catch (Exception e) {
            e.printStackTrace();
            cleanStop(10);
            return;
        }

        this.webService = new WebService(logger, backendDocument.getInt("web-port"), backendDocument.getBoolean("web-access-local"));

        this.commandHandler = new CommandHandler();

        this.commandHandler.registerCommand(new EndCommand());
        this.commandHandler.registerCommand(new HelpCommand());
    }

    public void start() {
        this.isRunning = true;
        startStopThread();
        this.webService.startWebServer(this);
    }

    public void stop() {
        this.isRunning = false;
        new Thread("Shutdown Thread") {

            @Override
            public void run() {
                logger.info("Shutting down...");
                cleanStop();
            }
        }.start();
    }

    private void cleanStop() {
        cleanStop(0);
    }

    private void cleanStop(int seconds) {
        try {
            Thread.sleep(seconds*1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (Handler handler : getLogger().getHandlers()) {
            handler.close();
        }
        System.exit( 0 );
    }

    public void startStopThread() {
        if(stopDate.equals("false")) {
            this.logger.info("Auto-Stop is disabled.");
            return;
        }

        new Thread("Auto-Stop Thread") {

            @Override
            public void run() {
                logger.info("Auto-Stop will be executed at "+stopDate+".");

                while (isRunning) {
                    try {
                        Thread.sleep(60000L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    String date = new SimpleDateFormat("HH:mm").format(new Date());

                    if(date.equals(stopDate)) {
                        logger.info("** Auto-Stop executed at "+stopDate+" **");
                        try {
                            Thread.sleep(100L);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        instance.stop();
                    }
                }
            }
        }.start();
    }

    public void startSetUp(File backendFile) {
        Thread thread = new Thread(new Setup(this, backendFile), "Setup Thread");
        thread.start();
        setInSetUp(true);
    }
}
