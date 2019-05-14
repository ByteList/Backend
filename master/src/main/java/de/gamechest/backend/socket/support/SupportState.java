package de.gamechest.backend.socket.support;

import lombok.Getter;

/**
 * Created by ByteList on 06.04.2018.
 * <p>
 * Copyright by ByteList - https://bytelist.de/
 */
public enum SupportState {

    OPEN("open"),
    IN_PROGRESSING("progressing"),
    SECOND_SUPPORT("progressing-2"),
    THIRD_SUPPORT("progressing-3"),
    FOURTH_SUPPORT("progressing-4"),
    CLOSED("closed");

    @Getter
    private String stateString;

    SupportState(String state) {
        this.stateString = state;
    }

    public static SupportState getSupportState(String state) {
        for (SupportState supportState : values()) {
            if(supportState.getStateString().equalsIgnoreCase(state)) {
                return supportState;
            }
        }
        throw new NullPointerException(state+" doesn't exist!");
    }
}
