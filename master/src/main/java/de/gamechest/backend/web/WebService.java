package de.gamechest.backend.web;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;
import de.gamechest.backend.Backend;
import de.gamechest.backend.database.DatabaseCollection;
import de.gamechest.backend.database.DatabaseManager;
import de.gamechest.backend.database.user.DatabaseUser;
import de.gamechest.backend.database.user.DatabaseUserObject;
import de.gamechest.backend.log.BackendLogger;
import org.bson.Document;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static de.gamechest.backend.database.settings.DatabaseSettingsObject.*;

/**
 * Created by ByteList on 17.02.2018.
 * <p>
 * Copyright by ByteList - https://bytelist.de/
 */
public class WebService {

    private final BackendLogger logger;

    private final int port;
    private final boolean local;

    private HttpServer httpServer;

    private final String HEADER_ALLOW = "Allow";
    private final String HEADER_CONTENT_TYPE = "Content-Type";

    private final Charset CHARSET = StandardCharsets.UTF_8;

    private final int STATUS_OK = 200;
    private final int STATUS_METHOD_NOT_ALLOWED = 405;

    private final int NO_RESPONSE_LENGTH = -1;

    private final String METHOD_GET = "GET";
    private final String METHOD_OPTIONS = "OPTIONS";
    private final String ALLOWED_METHODS = METHOD_GET + "," + METHOD_OPTIONS;


    public WebService(BackendLogger logger, int port, boolean local) {
        this.logger = logger;
        this.port = port;
        this.local = local;
    }

    public void startWebServer(Backend backend) {
        logger.info("Starting Web-Server...");
        final DatabaseManager databaseManager = backend.getDatabaseManager();

        try {
            this.httpServer = HttpServer.create(new InetSocketAddress((local ? "127.0.0.1" : "0.0.0.0"), port), 1);

            this.httpServer.createContext("/", httpExchange -> {
                try {
                    final Headers headers = httpExchange.getResponseHeaders();
                    final String requestMethod = httpExchange.getRequestMethod().toUpperCase();
                    switch (requestMethod) {
                        case METHOD_GET:
                            org.bson.Document first = databaseManager.getSettings(BACKEND_HOST, BACKEND_VERSION, BACKEND_STARTED).getFind().first();
                            first.remove("_id");
                            first.append("auth", "false");

                            final Map<String, List<String>> requestParameters = getRequestParameters(httpExchange.getRequestURI());

                            if (requestParameters.containsKey("uid")) {
                                String uid = requestParameters.get("uid").get(0);
                                if (uid.equals(backend.getBackendUid())) {
                                    first.append("auth", "true");

                                    if (requestParameters.containsKey("webUserById")) {
                                        String userId = requestParameters.get("webUserById").get(0);
                                        DatabaseUser dbUser = backend.getDatabaseManager().getUser(userId);

                                        if (!dbUser.existsUser()) {
                                            first = new Document();
                                            first.append(DatabaseUserObject.ID.getName(), userId);
                                            first.append("error", "user doesn't exist");
                                        } else {
                                            first = dbUser.getFind().first();
                                            first.remove("_id");
//                                            first.append(DatabaseUserObject.NAME.getName(), dbUser.getDatabaseElement(DatabaseUserObject.NAME).getAsString());
//                                            first.append(DatabaseUserObject.PERMISSION_LEVEL.getName(), dbUser.getDatabaseElement(DatabaseUserObject.PERMISSION_LEVEL).getAsString());
//                                            first.append(DatabaseUserObject.PASSWORD.getName(), dbUser.getDatabaseElement(DatabaseUserObject.PASSWORD).getAsString());
                                        }
                                    }


                                    if(requestParameters.containsKey("db")) {
                                        String dbId = requestParameters.get("db").get(0),
                                                dbName = "null";

                                        try {
                                            int id = Integer.parseInt(dbId);
                                            FindIterable<Document> find;
                                            MongoCollection<Document> collection;
                                            if(id > 999) {
                                                dbName = DatabaseCollection.getDatabaseCollectionFromId(id).getName();
                                                collection = databaseManager.getCollection(DatabaseCollection.getDatabaseCollectionFromId(id));
                                            } else {
                                                dbName = de.gamechest.database.DatabaseCollection.getDatabaseCollectionFromId(id).getName();
                                                collection = databaseManager.getParentDatabaseManager()
                                                        .getCollection(de.gamechest.database.DatabaseCollection.getDatabaseCollectionFromId(id));
                                            }

                                            if(requestParameters.containsKey("filter")) {
                                                String[] filter = requestParameters.get("filter").get(0).split(":");
                                                find = collection.find(Filters.eq(filter[0], filter[1]));
                                            } else {
                                                find = collection.find();
                                            }
                                            first = find.first();
                                            first.remove("_id");

                                        } catch (NullPointerException ex) {
                                            first = new Document("db", dbId+"{"+dbName+"}").append("error", "database doesn't exist");
                                            ex.printStackTrace();
                                        } catch (NumberFormatException ex) {
                                            first = new Document("db", dbId+"{"+dbName+"}").append("error", "database must be an integer");
                                            ex.printStackTrace();
                                        } catch (Exception ex) {
                                            first = new Document("db", dbId+"{"+dbName+"}").append("error", "unknown error");
                                            ex.printStackTrace();
                                        }
                                    }
                                }
                            }

                            final String responseBody = first.toJson();

                            headers.set(HEADER_CONTENT_TYPE, String.format("application/json; charset=%s", CHARSET));
                            final byte[] rawResponseBody = responseBody.getBytes(CHARSET);
                            httpExchange.sendResponseHeaders(STATUS_OK, rawResponseBody.length);
                            httpExchange.getResponseBody().write(rawResponseBody);
                            break;
                        case METHOD_OPTIONS:
                            headers.set(HEADER_ALLOW, ALLOWED_METHODS);
                            httpExchange.sendResponseHeaders(STATUS_OK, NO_RESPONSE_LENGTH);
                            break;
                        default:
                            headers.set(HEADER_ALLOW, ALLOWED_METHODS);
                            httpExchange.sendResponseHeaders(STATUS_METHOD_NOT_ALLOWED, NO_RESPONSE_LENGTH);
                            break;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    httpExchange.close();
                }
            });

            this.httpServer.setExecutor(null);
            this.httpServer.start();
            logger.info("Web-Server started!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Map<String, List<String>> getRequestParameters(final URI requestUri) {
        final Map<String, List<String>> requestParameters = new LinkedHashMap<>();
        final String requestQuery = requestUri.getRawQuery();
        if (requestQuery != null) {
            final String[] rawRequestParameters = requestQuery.split("[&;]", -1);
            for (final String rawRequestParameter : rawRequestParameters) {
                final String[] requestParameter = rawRequestParameter.split("=", 2);
                final String requestParameterName = decodeUrlComponent(requestParameter[0]);
                requestParameters.putIfAbsent(requestParameterName, new ArrayList<>());
                final String requestParameterValue = requestParameter.length > 1 ? decodeUrlComponent(requestParameter[1]) : null;
                requestParameters.get(requestParameterName).add(requestParameterValue);
            }
        }
        return requestParameters;
    }

    private String decodeUrlComponent(final String urlComponent) {
        try {
            return URLDecoder.decode(urlComponent, CHARSET.name());
        } catch (final UnsupportedEncodingException ex) {
            throw new InternalError(ex);
        }
    }
}
