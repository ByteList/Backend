package de.gamechest.backend.log;

import org.fusesource.jansi.Ansi;

/**
 * Created by ByteList on 24.07.2017.
 * <p>
 * Copyright by ByteList - https://bytelist.de/
 */
public final class AnsiColor {
    public static final String DEFAULT;
    public static final String WHITE;
    public static final String YELLOW;
    public static final String RED;
    public static final String GREEN;
    public static final String BLUE;
    public static final String CYAN;
    public static final String BLACK;
    public static final String GRAY;

    private AnsiColor() {
    }

    static {
        DEFAULT = Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.DEFAULT).boldOff().toString();
        WHITE = Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.WHITE).bold().toString();
        YELLOW = Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.YELLOW).bold().toString();
        RED = Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.RED).bold().toString();
        GREEN = Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.GREEN).bold().toString();
        BLUE = Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.BLUE).bold().toString();
        CYAN = Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.CYAN).bold().toString();
        BLACK = Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.BLACK).bold().toString();
        GRAY = Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.WHITE).boldOff().toString();
    }
}
