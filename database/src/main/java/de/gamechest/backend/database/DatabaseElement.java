package de.gamechest.backend.database;

import lombok.Getter;
import org.bson.Document;

/**
 * Created by ByteList on 10.04.2017.
 */
public class DatabaseElement {

    @Getter
    private Object object;

    public DatabaseElement(Object value) {
        this.object = value;
    }

    public int getAsInt() {
        int i = -1;
        try {
            i = Integer.parseInt(object.toString());
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        }
        return i;
    }

    public boolean getAsBoolean() {
        boolean b = false;
        try {
            b = Boolean.parseBoolean(object.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return b;
    }

    public String getAsString() {
        return object.toString();
    }

    public double getAsDouble() {
        double d = -1;
        try {
            d = Double.parseDouble(object.toString());
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        }
        return d;
    }

    public Document getAsDocument() {
        if (object.getClass().equals(Document.class)) {
            return ((Document)object);
        }
        throw new IllegalArgumentException(object.toString()+" can not be a document!");
    }

    public long getAsLong() {
        long l = -1;
        try {
            l = Long.parseLong(object.toString());
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        }
        return l;
    }
}
