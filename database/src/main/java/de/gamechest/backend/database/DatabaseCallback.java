package de.gamechest.backend.database;

/**
 * Created by ByteList on 05.07.2017.
 * <p>
 * Copyright by ByteList - https://bytelist.de/
 */
public interface DatabaseCallback<T> {

    void run(T result);
}
