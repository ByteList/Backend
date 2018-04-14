package de.gamechest.backend;

import com.mongodb.client.MongoCollection;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by ByteList on 14.04.2018.
 * <p>
 * Copyright by ByteList - https://bytelist.de/
 */
@AllArgsConstructor
public class CachedCollection<T> {
    @Getter
    private long timestamp;
    @Getter
    private MongoCollection<T> collection;
}
