package de.gamechest.backend;

import lombok.Getter;

/**
 * Created by ByteList on 14.04.2018.
 * <p>
 * Copyright by ByteList - https://bytelist.de/
 */
public class Cached<T> {
    @Getter
    private long timestamp;
    @Getter
    private T cached;

    public Cached(T value) {
        this.timestamp = System.currentTimeMillis();
        this.cached = value;
    }
}
