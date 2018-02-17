package de.gamechest.backend.log;

import jline.console.ConsoleReader;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Created by ByteList on 27.01.2017.
 *
 * Copyright by ByteList - https://bytelist.de/
 */
public class BackendLogger extends Logger {

    private final LogDispatcher dispatcher = new LogDispatcher( this );


    @SuppressWarnings(
            {
                    "CallToPrintStackTrace", "CallToThreadStartDuringObjectConstruction"
            })
    public BackendLogger(String name, ConsoleReader reader) {

        super(name, null);
        setLevel(Level.ALL);

        try {
            File file = new File("./", "logs");
            if(!file.exists()) file.mkdirs();
            String logFileName = "./logs/"
                    + (file.exists() ? file.list().length : 0)
                    + new SimpleDateFormat("_dd-MM-yyyy").format(Calendar.getInstance().getTime()) + ".log";

            FileHandler fileHandler = new FileHandler(logFileName);
            fileHandler.setFormatter(new FileFormatter());
            addHandler(fileHandler);

            LogWriter consoleHandler = new LogWriter(reader);
            consoleHandler.setLevel(Level.INFO);
            consoleHandler.setFormatter(new LogFormatter());
            addHandler(consoleHandler);

        } catch (IOException ex) {
            System.err.println("FileLogging failed.");
            ex.printStackTrace();
        }

        dispatcher.start();
    }


    @Override
    public void log(LogRecord record)
    {
        dispatcher.queue( record );
    }

    void doLog(LogRecord record)
    {
        super.log( record );
    }
}
