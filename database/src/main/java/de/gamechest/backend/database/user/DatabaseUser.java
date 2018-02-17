package de.gamechest.backend.database.user;

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
 * Created by ByteList on 17.02.2018.
 * <p>
 * Copyright by ByteList - https://bytelist.de/
 */
public class DatabaseUser {

    private final DatabaseManager databaseManager;
    private final DatabaseCollection databaseCollection = DatabaseCollection.USERS;

    @Getter
    private final FindIterable<Document> find;

    private int exists;
    private final String userId;

    public DatabaseUser(DatabaseManager databaseManager, String userId) {
        this.databaseManager = databaseManager;
        this.exists = -1;
        this.userId = userId;
        if(userId != null)
            this.find = databaseManager.getCollection(databaseCollection).find(Filters.eq(DatabaseUserObject.ID.getName(), userId));
        else
            this.find = null;
    }

    public DatabaseUser(DatabaseManager databaseManager, String userId, DatabaseUserObject... accesses) {
        this.databaseManager = databaseManager;
        this.userId = userId;
        this.exists = -1;
        if(userId != null) {
            this.find = databaseManager.getCollection(databaseCollection).find(Filters.eq(DatabaseUserObject.ID.getName(), userId));
            this.find.cursorType(CursorType.NonTailable);
            this.find.projection(Projections.include(DatabaseUserObject.toStringList(accesses)));
        }
        else this.find = null;
    }

    public void setDatabaseObject(DatabaseUserObject object, Object value) {
        BasicDBObject doc = new BasicDBObject();
        doc.append("$set", new BasicDBObject().append(object.getName(), value));

        BasicDBObject basicDBObject = new BasicDBObject().append(DatabaseUserObject.ID.getName(), userId);
        databaseManager.getCollection(databaseCollection).updateOne(basicDBObject, doc);
    }

    public DatabaseElement getDatabaseElement(DatabaseUserObject object) {
        if(find == null) return null;
        Document document = find.first();

        if(document == null) return null;

        return new DatabaseElement(document.get(object.getName()));
    }

    public boolean existsUser() {
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

    public void createUser(String name) {
        if(existsUser()) return;
        Document document = new Document()
                .append(DatabaseUserObject.ID.getName(), userId)
                .append(DatabaseUserObject.NAME.getName(), name)
                .append(DatabaseUserObject.PERMISSION_LEVEL.getName(), 0)
                .append(DatabaseUserObject.PASSWORD.getName(), null);

        databaseManager.getAsync().getExecutor().execute(()-> databaseManager.getCollection(databaseCollection).insertOne(document));
    }
}
