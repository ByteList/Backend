package de.gamechest.backend.socket;

import lombok.Getter;

/**
 * Created by ByteList on 06.04.2018.
 * <p>
 * Copyright by ByteList - https://bytelist.de/
 */
public enum SupportTab {

    DEFAULT("default"),
    MINECRAFT("mc"),
    WEBSITE("web"),
    TEAMSPEAK("ts"),
    DISCORD("dis"),
    ANYTHING("any");

    @Getter
    private String tabShort;

    SupportTab(String tab) {
        this.tabShort = tab;
    }

    public static SupportTab getSupportTab(String tabShort) {
        for (SupportTab supportTab : values()) {
            if(supportTab.getTabShort().equalsIgnoreCase(tabShort)) {
                return supportTab;
            }
        }
        throw new NullPointerException(tabShort+" doesn't exist!");
    }
}
