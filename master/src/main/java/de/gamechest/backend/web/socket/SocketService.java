package de.gamechest.backend.web.socket;

import de.gamechest.backend.Backend;
import de.gamechest.backend.log.BackendLogger;
import de.gamechest.backend.web.socket.support.SupportDatabase;
import de.gamechest.backend.web.socket.support.minecraft.MinecraftTable;
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
    private MinecraftTable minecraftTable;

    public SocketService(BackendLogger logger, int port, boolean local) {
        this.logger = logger;
        this.port = port;
        this.local = local;
        this.database = new SupportDatabase();
        this.minecraftTable = this.database.getMinecraftTable();
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

                        Document document = Document.parse(target);

                        backend.runDocumentCallbackAsync((send) -> {

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
                                                case CHANGE_STATE:
                                                    send.append("error", "wrong action");
                                                    break;
                                                case ANSWER:
                                                    send.append("error", "wrong action");
                                                    break;
                                                case GET_TICKETS:
                                                    send = getTicketsDefault(document, send);
                                                    break;
                                            }
                                            break;
                                        case MINECRAFT:
                                            switch (supportAction) {
                                                case CREATE:
                                                    send = createMC(document, send);
                                                    break;
                                                case CHANGE_STATE:
                                                    send = changeStateMC(document, send);
                                                    break;
                                                case ANSWER:
                                                    send = answerMC(document, send);
                                                    break;
                                                case GET_TICKETS:
                                                    send.append("error", "wrong action");
                                                    break;
                                            }
                                            break;
                                        case WEBSITE:
                                            break;
                                        case TEAMSPEAK:
                                            break;
                                        case DISCORD:
                                            break;
                                        case ANYTHING:
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
            send.append(String.valueOf(i), d.toJson());
            i++;
        }
        return send;
    }

    private Document createMC(Document document, Document send) {
        int ticketId = this.minecraftTable.count() + 1;
        String creator = document.getString("creator");
        String topic = document.getString("topic");
        String version = document.getString("mcv");
        String serverId = document.getString("sid");
        String subject = document.getString("subject");
        String msg = document.getString("msg");


        if (this.database.createMinecraftTicket(ticketId, creator, topic, version, serverId, subject, msg)) {
            send.append("id", ticketId);
        } else {
            send.append("error", "createMinecraftTicket()");
        }

        return send;
    }

    private Document changeStateMC(Document document, Document send) {
        return send;
    }

    private Document answerMC(Document document, Document send) {
        return send;
    }
}
