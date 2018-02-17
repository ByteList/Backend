package de.gamechest.backend.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Created by ByteList on 27.01.2017.
 *
 * Copyright by ByteList - https://bytelist.de/
 */
public class LogFormatter extends Formatter {

    private final DateFormat date = new SimpleDateFormat("HH:mm:ss");

    @Override
    public String format(LogRecord record) {
        StringBuilder formatted = new StringBuilder();

        formatted.append(" ");
        formatted.append(AnsiColor.GREEN);
        formatted.append(this.date.format(record.getMillis()));
        formatted.append(AnsiColor.DEFAULT);
        formatted.append(" ");
        if(record.getLevel().getName().startsWith("INFO")) {
            formatted.append(AnsiColor.YELLOW);
        } else {
            formatted.append(AnsiColor.RED);
        }
        formatted.append(record.getLevel().getName());
        formatted.append(AnsiColor.GRAY);
        formatted.append(" | ");
        formatted.append(AnsiColor.WHITE);
        formatted.append(formatMessage(record));
        formatted.append(AnsiColor.DEFAULT);
        formatted.append('\n');

        if (record.getThrown() != null) {
            StringWriter writer = new StringWriter();
            record.getThrown().printStackTrace(new PrintWriter(writer));
            formatted.append(writer);
        }

        return formatted.toString();
    }
}
