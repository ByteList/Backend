package de.gamechest.backend.web.socket;

import lombok.Getter;

/**
 * Created by ByteList on 06.04.2018.
 * <p>
 * Copyright by ByteList - https://bytelist.de/
 */
public enum SupportState {

    OPEN("open"),
    IN_PROGRESSING("in_progressing"),
    CLOSED("closed");

    @Getter
    private String stateString;

    SupportState(String state) {
        this.stateString = state;
    }
}
