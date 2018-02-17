package de.gamechest.backend.log;

import java.beans.ConstructorProperties;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by ByteList on 14.05.2017.
 *
 * Copyright by ByteList - https://bytelist.de/
 */
public class LoggingOutPutStream extends ByteArrayOutputStream {

    @ConstructorProperties({"logger", "level"})
    public LoggingOutPutStream(Logger logger, Level level) {
        this.logger = logger;
        this.level = level;
    }

    private static final String separator = System.getProperty("line.separator");
    private final Logger logger;
    private final Level level;

    public void flush() throws IOException {
        String contents = toString(Charset.defaultCharset().name());
        super.reset();
        if ((!contents.isEmpty()) && (!contents.equals(separator))) {
            this.logger.logp(this.level, "", "", contents);
        }
    }
}
