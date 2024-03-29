package de.gamechest.backend.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Created by ByteList on 24.07.2017.
 * <p>
 * Copyright by ByteList - https://bytelist.de/
 */
public class FileFormatter extends Formatter {

    private final DateFormat date = new SimpleDateFormat("HH:mm:ss");

    @Override
    public String format(LogRecord record) {
        StringBuilder formatted = new StringBuilder();

        formatted.append(this.date.format(record.getMillis()));
        formatted.append(" ");
        formatted.append(record.getLevel().getName());
        formatted.append(" | ");
        formatted.append(formatMessage(record));
        formatted.append('\n');

        if (record.getThrown() != null) {
            StringWriter writer = new StringWriter();
            record.getThrown().printStackTrace(new PrintWriter(writer));
            formatted.append(writer);
        }

        return formatted.toString();
    }
}
