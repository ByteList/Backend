package de.gamechest.backend.web;

import lombok.Getter;

/**
 * Created by ByteList on 27.02.2018.
 * <p>
 * Copyright by ByteList - https://bytelist.de/
 */
public enum JsonCoding {

    NORMAL(0),
    STRING(1);


    @Getter
    private int id;


    JsonCoding(int id) {
        this.id = id;
    }

    public static JsonCoding getJsonCodingFromId(int id) {
        for (JsonCoding jsonCoding : values()) {
            if(jsonCoding.getId() == id) {
                return jsonCoding;
            }
        }
        return NORMAL;
    }
}
