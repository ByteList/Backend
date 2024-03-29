package de.gamechest.backend.socket;

import de.gamechest.backend.Backend;
import de.gamechest.backend.log.BackendLogger;
import de.gamechest.backend.socket.support.SupportAction;
import de.gamechest.backend.socket.support.SupportDatabase;
import de.gamechest.backend.socket.support.SupportTab;
import org.bson.Document;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by ByteList on 04.04.2018.
 * <p>
 * Copyright by ByteList - https://bytelist.de/
 */
public class SocketService {


    private final BackendLogger logger;

    private final int port;
    private final boolean local;

    private SupportDatabase database;

    public SocketService(BackendLogger logger, int port, boolean local) {
        this.logger = logger;
        this.port = port;
        this.local = local;
        this.database = new SupportDatabase();
    }

    public void startSocketServer(Backend backend) {
        new Thread(() -> {
            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(port, 50, InetAddress.getByName((local ? "127.0.0.1" : "0.0.0.0")));
            } catch (IOException e) {
                e.printStackTrace();
            }

            while (backend.isRunning) {
                if (serverSocket != null) {
                    try {
                        Socket client = serverSocket.accept();
                        InputStreamReader inputStreamReader = new InputStreamReader(client.getInputStream());
                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                        PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(client.getOutputStream()));
                        String target = bufferedReader.readLine();
                        logger.info("[S " + client.getInetAddress() + ":" + client.getPort() + "] " + target);

                        backend.runDocumentCallbackAsync((send) -> {
                            Document document = Document.parse(target);

                            switch (document.getString("service")) {
                                case "support":
                                    String tabShort = document.getString("tab");
                                    SupportTab supportTab = SupportTab.getSupportTab(tabShort);
                                    String action = document.getString("action");
                                    SupportAction supportAction = SupportAction.getSupportTab(Integer.valueOf(action));

                                    switch (supportTab) {
                                        case DEFAULT:
                                            switch (supportAction) {
                                                case CREATE:
                                                    send.append("error", "wrong action");
                                                    break;
                                                case ANSWER:
                                                    send = answer(document, send);
                                                    break;
                                                case GET_TICKETS:
                                                    if(document.containsKey("tickets_state")) {
                                                        send = getTicketsFromState(document, send);
                                                    } else if(document.containsKey("notify_value")) {
                                                        send = getNotifyCount(document);
                                                    } else {
                                                        send = getTicketsDefault(document, send);
                                                    }
                                                    break;
                                                case GET_TICKET:
                                                    send = getTicket(document);
                                                    break;
                                            }
                                            break;
                                        case MINECRAFT:
                                            switch (supportAction) {
                                                case CREATE:
                                                    send = createMC(document, send);
                                                    break;
                                                case ANSWER:
                                                    send = answer(document, send);
                                                    break;
                                                case GET_TICKETS:
                                                    send.append("error", "wrong action");
                                                    break;
                                                case GET_TICKET:
                                                    send = getTicket(document);
                                                    break;
                                            }
                                            break;
                                        case WEBSITE:
                                            switch (supportAction) {
                                                case CREATE:
                                                    send = createWeb(document, send);
                                                    break;
                                                case ANSWER:
                                                    send = answer(document, send);
                                                    break;
                                                case GET_TICKETS:
                                                    send.append("error", "wrong action");
                                                    break;
                                                case GET_TICKET:
                                                    send = getTicket(document);
                                                    break;
                                            }
                                            break;
                                        case TEAMSPEAK:
                                            switch (supportAction) {
                                                case CREATE:
                                                    send = createTs(document, send);
                                                    break;
                                                case ANSWER:
                                                    send = answer(document, send);
                                                    break;
                                                case GET_TICKETS:
                                                    send.append("error", "wrong action");
                                                    break;
                                                case GET_TICKET:
                                                    send = getTicket(document);
                                                    break;
                                            }
                                            break;
                                        case DISCORD:
                                            switch (supportAction) {
                                                case CREATE:
                                                    send = createDis(document, send);
                                                    break;
                                                case ANSWER:
                                                    send = answer(document, send);
                                                    break;
                                                case GET_TICKETS:
                                                    send.append("error", "wrong action");
                                                    break;
                                                case GET_TICKET:
                                                    send = getTicket(document);
                                                    break;
                                            }
                                            break;
                                        case ANYTHING:
                                            switch (supportAction) {
                                                case CREATE:
                                                    send = createAny(document, send);
                                                    break;
                                                case ANSWER:
                                                    send = answer(document, send);
                                                    break;
                                                case GET_TICKETS:
                                                    send.append("error", "wrong action");
                                                    break;
                                                case GET_TICKET:
                                                    send = getTicket(document);
                                                    break;
                                            }
                                            break;
                                    }

                                    break;
                                default:
                                    send.append("error", "wrong service");
                                    break;
                            }

                            printWriter.println(send.toJson());
                            printWriter.flush();
                            printWriter.close();
                        });
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                }
            }
        }).start();
        System.out.println("Socket-Server started!");
    }

    public void stopSocketServer() {
        if (database != null) {
            if (this.database.close()) {
                System.out.println("SqlLite - SocketService closed!");
            }
        }
        System.out.println("Socket-Server stopped!");
    }

    private Document getTicketsDefault(Document document, Document send) {
        String creator = document.getString("creator");
        ArrayList<Document> ids = this.database.getTicketIds(creator);
        int i = 0;
        for (Document d : ids) {
            send.append(String.valueOf(i), d);
            i++;
        }
        return send;
    }

    private Document getTicketsFromState(Document document, Document send) {
        String state = document.getString("tickets_state");
        String creator = document.containsKey("creator") ? document.getString("creator") : null;

        ArrayList<Document> ids = this.database.getTicketIdsFromState(state, creator);
        int i = 0;
        for (Document d : ids) {
            send.append(String.valueOf(i), d);
            i++;
        }
        return send;
    }

    private Document getNotifyCount(Document document) {
        String user = document.getString("user");
        String notifyValue = document.getString("notify_value");

        return this.database.getNotify(user, notifyValue);
    }

    private Document getTicket(Document document) {
        int ticketId = Integer.valueOf(document.getString("ticket_id"));
        String sender = document.getString("sender");
        return this.database.getTicket(ticketId, sender);
    }

    private Document answer(Document document, Document send) {
        int ticketId = Integer.valueOf(document.getString("ticket_id"));
        String user = document.getString("user");
        String msg = document.getString("message");

        if(this.database.answer(ticketId, user, msg)) {
            send.append("id", ticketId);
        } else {
            send.append("error", "answer()");
        }

        return send;
    }

    private Document createMC(Document document, Document send) {
        int ticketId = this.database.getTicketsTable().count() + 1;

        if (this.database.createMinecraftTicket(ticketId, document.getString("creator"), document.getString("topic"),
                document.getString("mcv"), document.getString("sid"), document.getString("subject"), document.getString("msg"))) {
            send.append("id", ticketId);
        } else {
            send.append("error", "createMC()");
        }

        return send;
    }

    private Document createWeb(Document document, Document send) {
        int ticketId = this.database.getTicketsTable().count() + 1;

        if (this.database.createWebsiteTicket(ticketId, document.getString("creator"), document.getString("topic"),
                document.getString("url"), document.getString("subject"), document.getString("msg"))) {
            send.append("id", ticketId);
        } else {
            send.append("error", "createWeb()");
        }

        return send;
    }

    private Document createTs(Document document, Document send) {
        int ticketId = this.database.getTicketsTable().count() + 1;

        if (this.database.createTeamspeakTicket(ticketId, document.getString("creator"), document.getString("topic"),
                document.getString("name"), document.getString("uid"), document.getString("subject"), document.getString("msg"))) {
            send.append("id", ticketId);
        } else {
            send.append("error", "createTs()");
        }

        return send;
    }

    private Document createDis(Document document, Document send) {
        int ticketId = this.database.getTicketsTable().count() + 1;

        if (this.database.createDiscordTicket(ticketId, document.getString("creator"), document.getString("topic"),
                document.getString("name"), document.getString("channel"), document.getString("subject"), document.getString("msg"))) {
            send.append("id", ticketId);
        } else {
            send.append("error", "createDis()");
        }

        return send;
    }

    private Document createAny(Document document, Document send) {
        int ticketId = this.database.getTicketsTable().count() + 1;

        if (this.database.createAnythingTicket(ticketId, document.getString("creator"), document.getString("topic"),
                document.getString("subject"), document.getString("msg"))) {
            send.append("id", ticketId);
        } else {
            send.append("error", "createAny()");
        }

        return send;
    }
}
