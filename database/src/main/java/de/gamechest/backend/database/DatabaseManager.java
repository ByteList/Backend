package de.gamechest.backend.database;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import de.gamechest.backend.database.settings.DatabaseSettings;
import de.gamechest.backend.database.settings.DatabaseSettingsObject;
import de.gamechest.backend.database.user.DatabaseUser;
import de.gamechest.backend.database.user.DatabaseUserObject;
import lombok.Getter;
import org.bson.Document;
import org.bson.UuidRepresentation;
import org.bson.codecs.UuidCodec;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static de.gamechest.backend.database.settings.DatabaseSettingsObject.BACKEND_STARTED;
import static de.gamechest.backend.database.settings.DatabaseSettingsObject.BACKEND_VERSION;

/**
 * Created by ByteList on 09.04.2017.
 *
 * Copyright by ByteList - https://bytelist.de/
 */
public class DatabaseManager {

    private HashMap<DatabaseCollection, MongoCollection<Document>> collections = new HashMap<>();

    final String backendUid, backendVersion;

    @Getter
    private MongoClient mongoClient;
    @Getter
    private MongoDatabase mongoDatabase;

    @Getter
    private AsyncDatabaseManager async;

    public DatabaseManager(String backendUid, String backendVersion, String host, int port, String username, String password, String database) throws Exception {
        this.backendUid = backendUid;
        this.backendVersion = backendVersion;

        // Disable the stupid log messages from mongodb
        Logger mongoLog = Logger.getLogger("org.mongodb.driver");
        mongoLog.setLevel(Level.OFF);

        // Support for new mongodb standard uuid's
        CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
                CodecRegistries.fromCodecs(new UuidCodec(UuidRepresentation.STANDARD)),
                MongoClient.getDefaultCodecRegistry()
        );
        MongoClientOptions options = MongoClientOptions.builder().codecRegistry(codecRegistry).build();

        if(username != null && password != null)
            this.mongoClient = new MongoClient(new ServerAddress(host, port), Collections.singletonList(MongoCredential.createCredential(username, database, password.toCharArray())), options);
        else
            this.mongoClient = new MongoClient(new ServerAddress(host, port), options);
        this.mongoDatabase = this.mongoClient.getDatabase(database);

        this.async = new AsyncDatabaseManager(this);
    }

    public void init() {
        for(DatabaseCollection col : DatabaseCollection.values()) {
            if (!existsCollection(col.getName())) mongoDatabase.createCollection(col.getName());
            collections.put(col, mongoDatabase.getCollection(col.getName()));
        }

        async.getSettings(dbSettings-> {
            if(!dbSettings.existsSettings()) {
                dbSettings.createSettings(this.mongoClient.getAddress().getHost(), backendVersion);
            }
            Calendar now = Calendar.getInstance();
            SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            String started = formatter.format(now.getTime());
            dbSettings.setDatabaseObject(BACKEND_STARTED, started);

            dbSettings.setDatabaseObject(BACKEND_VERSION, backendVersion);
        });
    }

    public boolean existsCollection(String collection) {
        for(String cl : mongoDatabase.listCollectionNames()) {
            if(cl.equalsIgnoreCase(collection)) {
                return true;
            }
        }
        return false;
    }

    public MongoCollection<Document> getCollection(DatabaseCollection collectionEnum) {
        return collections.get(collectionEnum);
    }

    public DatabaseSettings getSettings(DatabaseSettingsObject... objects) {
        return new DatabaseSettings(this, backendUid, objects);
    }

    public DatabaseUser getUser(String userId, DatabaseUserObject... objects) {
        return new DatabaseUser(this, userId, objects);
    }
}
