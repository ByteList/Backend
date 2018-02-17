package de.gamechest.backend.config;

/**
 * Created by ByteList on 13.02.2018.
 * <p>
 * Copyright by ByteList - https://bytelist.de/
 */
public class BackendDocument extends Document {

    public BackendDocument(String backendUid) {
        super("backend-file");
        append("BACKEND_UID", backendUid);
    }
}
