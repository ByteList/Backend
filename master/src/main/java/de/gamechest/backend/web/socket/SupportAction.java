package de.gamechest.backend.web.socket;

import lombok.Getter;

/**
 * Created by ByteList on 06.04.2018.
 * <p>
 * Copyright by ByteList - https://bytelist.de/
 */
public enum SupportAction {

    CREATE(2),
    CHANGE_STATE(1),
    ANSWER(3);

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
