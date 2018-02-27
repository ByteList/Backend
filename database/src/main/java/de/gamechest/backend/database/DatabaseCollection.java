package de.gamechest.backend.database;

import lombok.Getter;

/**
 * Created by ByteList on 09.04.2017.
 */
public enum DatabaseCollection {

    SETTINGS(1000, "web-settings"),
    USERS(1001, "web-users");

    @Getter
    private int id;
    @Getter
    private String name;
    DatabaseCollection(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public static DatabaseCollection getDatabaseCollectionFromId(int id) {
        for(DatabaseCollection collection : values()) {
            if(id == collection.getId()) {
                return collection;
            }
        }
        throw new IllegalArgumentException("This collection doesn't exist! ("+id+")");
    }
}
