package de.gamechest.backend.web;

import com.mongodb.BasicDBObject;
import com.mongodb.Block;
import com.mongodb.CursorType;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;
import de.gamechest.backend.Backend;
import de.gamechest.backend.Cached;
import de.gamechest.backend.database.DatabaseCollection;
import de.gamechest.backend.database.DatabaseManager;
import de.gamechest.backend.log.BackendLogger;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

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

    private final boolean CACHE_ENABLED = false;

    private final HashMap<String, Cached<Document>> findCache = new HashMap<>();

    public WebService(BackendLogger logger, int port, boolean local) {
        this.logger = logger;
        this.port = port;
        this.local = local;
    }

    public void startWebServer(Backend backend) {
        logger.info("Starting Web-Server...");

        backend.runAsync(()-> {
            while (backend.isRunning) {
                long currentSeconds = System.currentTimeMillis() / 1000;

                new HashMap<>(findCache).forEach((url, cached) -> {
                    long timestamp = cached.getTimestamp() / 1000;

                    if(currentSeconds > timestamp + 25) {
                        findCache.remove(url);
                    }
                });

                try {
                    Thread.sleep(500L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            findCache.clear();
        });

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


                                    if(requestParameters.containsKey("db")) {
                                        String dbId = requestParameters.get("db").get(0),
                                                dbName = "null";
                                        FindIterable<Document> find;
                                        MongoCollection<Document> collection;
                                        JsonCoding jsonCoding = JsonCoding.NORMAL;
                                        boolean set = false;

                                        try {
                                            int id = Integer.parseInt(dbId);
                                            String url = httpExchange.getRequestURI().toString();

                                            if(canCache(url) && findCache.containsKey(url)) {
                                                first = findCache.get(url).getCached();
                                                logger.info("[W "+httpExchange.getRemoteAddress().toString()+" | cached] "+httpExchange.getRequestURI().toString());
                                            } else {
                                                logger.info("[W "+httpExchange.getRemoteAddress().toString()+"] "+httpExchange.getRequestURI().toString());

                                                if(id > 999) {
                                                    dbName = DatabaseCollection.getDatabaseCollectionFromId(id).getName();
                                                    collection = databaseManager.getCollection(DatabaseCollection.getDatabaseCollectionFromId(id));
                                                } else {
                                                    dbName = de.gamechest.database.DatabaseCollection.getDatabaseCollectionFromId(id).getName();
                                                    collection = databaseManager.getParentDatabaseManager()
                                                            .getCollection(de.gamechest.database.DatabaseCollection.getDatabaseCollectionFromId(id));
                                                }
                                                String[] filter = null;
                                                if(requestParameters.containsKey("filter")) {
                                                    filter = requestParameters.get("filter").get(0).split(":");
                                                    Bson f = Filters.eq(filter[0], filter[1]);
                                                    find = collection.find(f);
                                                } else {
                                                    find = collection.find();
                                                }

                                                if(requestParameters.containsKey("accesses")) {
                                                    String[] accesses = requestParameters.get("accesses").get(0).split(":");

                                                    find.cursorType(CursorType.NonTailable);
                                                    find.projection(Projections.include(accesses));
                                                }

                                                if(requestParameters.containsKey("coding")) {
                                                    String coding = requestParameters.get("coding").get(0);

                                                    jsonCoding = JsonCoding.getJsonCodingFromId(Integer.parseInt(coding));
                                                }

                                                if(requestParameters.containsKey("set")) {
                                                    String[] args = requestParameters.get("set").get(0).split("~;~");
                                                    for(String arg : args) {
                                                        String setter = arg.split("§:§")[0];
                                                        String value = arg.split("§:§")[1];

                                                        BasicDBObject uDoc = new BasicDBObject();
                                                        uDoc.append("$set", new BasicDBObject().append(setter, value));

                                                        BasicDBObject basicDBObject = new BasicDBObject();
                                                        if(filter != null) {
                                                            basicDBObject = new BasicDBObject(filter[0], filter[1]);
                                                        }
                                                        collection.updateOne(basicDBObject, uDoc);
                                                    }
                                                    set = true;
                                                }

                                                Document doc = new Document();
                                                final int[] i = {0};

                                                switch (jsonCoding) {
                                                    case NORMAL:
                                                        find.forEach((Block<? super Document>) document -> {
                                                            document.remove("_id");
                                                            doc.append(String.valueOf(i[0]++), document);
                                                        });
                                                        break;
                                                    case STRING:
                                                        find.forEach((Block<? super Document>) document -> {
                                                            document.remove("_id");

                                                            Document stringed = new Document();
                                                            document.forEach((s, o) -> {
                                                                if(o instanceof Document) {
                                                                    stringed.append(s,((Document) o).toJson());
                                                                } else {
                                                                    stringed.append(s, (o == null ? "null" : o.toString()));
                                                                }
                                                            });

                                                            doc.append(String.valueOf(i[0]++), stringed);

                                                        });
                                                        break;
                                                }

                                                first = doc;
                                                first.append("size", i[0]);

                                                if(!set) findCache.put(url, new Cached<>(first));
                                            }

                                        } catch (Exception ex) {
                                            first = new Document("db", dbId+"{"+dbName+"}").append("error", ex.getClass().getSimpleName()).append("msg", ex.getMessage());
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

    private boolean canCache(String url) {
        if(!CACHE_ENABLED) return false;

        return !url.contains("session");
    }
}
