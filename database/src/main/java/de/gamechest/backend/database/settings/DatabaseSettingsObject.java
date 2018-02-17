package de.gamechest.backend.database.settings;

import lombok.Getter;

/**
 * Created by ByteList on 12.02.2018.
 * <p>
 * Copyright by ByteList - https://bytelist.de/
 */
public enum  DatabaseSettingsObject {

    BACKEND_UID("backend-uid"),
    BACKEND_VERSION("backend-version"),
    BACKEND_STARTED("backend-started"),
    BACKEND_HOST("backend-host");


    @Getter
    private String name;

    DatabaseSettingsObject(String name) {
        this.name = name;
    }

    public static String[] toStringList(DatabaseSettingsObject... rules) {
        String[] list = new String[rules.length];
        for (int i = 0; i < rules.length; i++)
            list[i] = rules[i].getName();
        return list;
    }
}
