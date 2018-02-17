package de.gamechest.backend.database.settings;

import com.mongodb.BasicDBObject;
import com.mongodb.CursorType;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import de.gamechest.backend.database.DatabaseCollection;
import de.gamechest.backend.database.DatabaseElement;
import de.gamechest.backend.database.DatabaseManager;
import lombok.Getter;
import org.bson.Document;

/**
 * Created by ByteList on 12.02.2018.
 * <p>
 * Copyright by ByteList - https://bytelist.de/
 */
public class DatabaseSettings {
    private final DatabaseManager databaseManager;
    private final DatabaseCollection databaseCollection = DatabaseCollection.SETTINGS;

    @Getter
    private final FindIterable<Document> find;

    private int exists;
    private final String backendUid;

    public DatabaseSettings(DatabaseManager databaseManager, String backendUid) {
        this.databaseManager = databaseManager;
        this.exists = -1;
        this.backendUid = backendUid;
        if(backendUid != null)
            this.find = databaseManager.getCollection(databaseCollection).find(Filters.eq(DatabaseSettingsObject.BACKEND_UID.getName(), backendUid));
        else
            this.find = null;
    }

    public DatabaseSettings(DatabaseManager databaseManager, String backendUid, DatabaseSettingsObject... accesses) {
        this.databaseManager = databaseManager;
        this.backendUid = backendUid;
        this.exists = -1;
        if(backendUid != null) {
            this.find = databaseManager.getCollection(databaseCollection).find(Filters.eq(DatabaseSettingsObject.BACKEND_UID.getName(), backendUid));
            this.find.cursorType(CursorType.NonTailable);
            this.find.projection(Projections.include(DatabaseSettingsObject.toStringList(accesses)));
        }
        else this.find = null;
    }

    public void setDatabaseObject(DatabaseSettingsObject databaseSettingsObject, Object value) {
        BasicDBObject doc = new BasicDBObject();
        doc.append("$set", new BasicDBObject().append(databaseSettingsObject.getName(), value));

        BasicDBObject basicDBObject = new BasicDBObject().append(DatabaseSettingsObject.BACKEND_UID.getName(), backendUid);
        databaseManager.getCollection(databaseCollection).updateOne(basicDBObject, doc);
    }

    public DatabaseElement getDatabaseElement(DatabaseSettingsObject databaseSettingsObject) {
        if(find == null) return null;
        Document document = find.first();

        if(document == null) return null;

        return new DatabaseElement(document.get(databaseSettingsObject.getName()));
    }

    public boolean existsSettings() {
        if(exists == -1) {
            if(find == null) {
                this.exists = 0;
                return false;
            }
            Document document = find.first();
            if(document != null) {
                this.exists = 1;
                return true;
            }
        } else {
            if(exists == 1)
                return true;
        }
        return false;
    }

    public void createSettings(String host, String version) {
        if(existsSettings()) return;
        Document document = new Document()
                .append(DatabaseSettingsObject.BACKEND_UID.getName(), backendUid)
                .append(DatabaseSettingsObject.BACKEND_HOST.getName(), host)
                .append(DatabaseSettingsObject.BACKEND_VERSION.getName(), version)
                .append(DatabaseSettingsObject.BACKEND_STARTED.getName(), null);

        databaseManager.getAsync().getExecutor().execute(()-> databaseManager.getCollection(databaseCollection).insertOne(document));
    }

}
