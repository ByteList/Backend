package de.gamechest.backend;

/**
 * Created by ByteList on 15.02.2018.
 * <p>
 * Copyright by ByteList - https://bytelist.de/
 */
public interface Callback<T> {

    void run(T result);
}
