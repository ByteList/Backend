package de.gamechest.backend.socket;

import lombok.Getter;

/**
 * Created by ByteList on 06.04.2018.
 * <p>
 * Copyright by ByteList - https://bytelist.de/
 */
public enum SupportAction {

    CREATE(0),
    CHANGE_STATE(1),
    ANSWER(2),
    GET_TICKETS(3),
    GET_TICKET(4);

    @Getter
    private int action;

    SupportAction(int action) {
        this.action = action;
    }

    public static SupportAction getSupportTab(int action) {
        for (SupportAction supportAction : values()) {
            if(supportAction.getAction() == action) {
                return supportAction;
            }
        }
        throw new NullPointerException(action+" doesn't exist!");
    }
}
