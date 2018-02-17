package de.gamechest.backend.database.user;

import lombok.Getter;

/**
 * Created by ByteList on 12.02.2018.
 * <p>
 * Copyright by ByteList - https://bytelist.de/
 */
public enum DatabaseUserObject {

    ID("id"),
    NAME("name"),
    PERMISSION_LEVEL("permission-level"),
    PASSWORD("password");


    @Getter
    private String name;

    DatabaseUserObject(String name) {
        this.name = name;
    }

    public static String[] toStringList(DatabaseUserObject... rules) {
        String[] list = new String[rules.length];
        for (int i = 0; i < rules.length; i++)
            list[i] = rules[i].getName();
        return list;
    }
}
