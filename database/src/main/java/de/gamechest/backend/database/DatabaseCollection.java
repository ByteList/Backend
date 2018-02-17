package de.gamechest.backend.database;

import lombok.Getter;

/**
 * Created by ByteList on 09.04.2017.
 */
public enum DatabaseCollection {

    SETTINGS("web-settings"),
    USERS("web-users");

    @Getter
    private String name;

    DatabaseCollection(String name) {
        this.name = name;
    }
}
