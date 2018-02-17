package de.gamechest.backend.database;

import de.gamechest.backend.database.settings.DatabaseSettings;
import de.gamechest.backend.database.user.DatabaseUser;
import lombok.Getter;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by ByteList on 05.07.2017.
 * <p>
 * Copyright by ByteList - https://bytelist.de/
 */
public class AsyncDatabaseManager {

    private final DatabaseManager databaseManager;

    @Getter
    private Executor executor;

    public AsyncDatabaseManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        this.executor = Executors.newCachedThreadPool();
    }

    public void getSettings(DatabaseCallback<DatabaseSettings> callback) {
        this.executor.execute(()-> callback.run(new DatabaseSettings(databaseManager, databaseManager.backendUid)));
    }

    public void getUser(String userId, DatabaseCallback<DatabaseUser> callback) {
        this.executor.execute(()-> callback.run(new DatabaseUser(databaseManager, userId)));
    }
}