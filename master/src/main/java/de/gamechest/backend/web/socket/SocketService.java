package de.gamechest.backend.web.socket;

import de.gamechest.backend.Backend;
import de.gamechest.backend.log.BackendLogger;
import de.gamechest.backend.sql.SqlLite;
import org.bson.Document;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by ByteList on 04.04.2018.
 * <p>
 * Copyright by ByteList - https://bytelist.de/
 */
public class SocketService {


    private final BackendLogger logger;

    private final int port;
    private final boolean local;

    private SqlLite sqlLite;

    public SocketService(BackendLogger logger, int port, boolean local) {
        this.logger = logger;
        this.port = port;
        this.local = local;
        try {
            this.sqlLite = new SqlLite("socketService");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startSocketServer(Backend backend) {
        if(this.sqlLite.createTableIfNotExists("support (id string, topic string, creator string)")) {
            System.out.println("SqlLite - SocketService support table created!");
        }
        if(this.sqlLite.createTableIfNotExists("mc (id string, player string, uuid string, version string, sid string, subject string, msg string, answers string)")) {
            System.out.println("SqlLite - SocketService mc table created!");
        }
        if(this.sqlLite.createTableIfNotExists("mcanswers (id string, number string, answer string, timestamp string)")) {
            System.out.println("SqlLite - SocketService mcanswers table created!");
        }
        new Thread(()-> {
            ServerSocket server = null;
            try {
                server = new ServerSocket(port, 50, InetAddress.getByName((local ? "127.0.0.1" : "0.0.0.0")));
            } catch (IOException e) {
                e.printStackTrace();
            }

            while(backend.isRunning){
                try {
                    if (server != null) {
                        Socket client  = server.accept();
                        InputStreamReader inputStreamReader =  new InputStreamReader(client.getInputStream());
                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                        PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(client.getOutputStream()));
                        String target = bufferedReader.readLine();

                        logger.info("[S "+client.getInetAddress()+":"+client.getPort()+"] "+target);

                        Document document = Document.parse(target);
                        Document send = new Document();

                        switch (document.getString("service")) {
                            case "support":
                                String tabShort = document.getString("tab");
                                SupportTab supportTab = SupportTab.getSupportTab(tabShort);

                                switch (supportTab) {
                                    case MINECRAFT:
                                        ResultSet resultSet = this.sqlLite.executeQuery("SELECT COUNT(id) AS rowcount FROM support");
                                        int ticketId = -2;
                                        String topic = document.getString("topic");
                                        String creator = document.getString("creator");
                                        String player = document.getString("player");
                                        String uuid = document.getString("uuid");
                                        String version = document.getString("mcv");
                                        String serverId = document.getString("sid");
                                        String subject = document.getString("subject");
                                        String msg = document.getString("msg");

                                        try {
                                            while (resultSet.next()) {
                                                ticketId = resultSet.getInt("rowcount")+1;
                                            }
                                        } catch (SQLException e) {
                                            e.printStackTrace();
                                        } finally {
                                            try {
                                                resultSet.close();
                                            } catch (SQLException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        if(this.sqlLite.executeSupportInsert(ticketId, topic, creator)) {
                                            if(this.sqlLite.executeSupportInsertMinecraft(ticketId, player, uuid, version, serverId, subject, msg)) {
                                                send.append("id", ticketId);
                                                send.append("topic", topic);
                                                send.append("creator", creator);
                                            } else {
                                                send.append("error", "executeSupportInsertMinecraft()");
                                            }
                                        } else {
                                            send.append("error", "executeSupportInsert()");
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
                    }
                }
                catch (IOException ex){
                    ex.printStackTrace();
                }
            }
        }).start();
        System.out.println("Socket-Server started!");
    }

    public void stopSocketServer() {
        if(sqlLite != null) {
            if( this.sqlLite.close()) {
                System.out.println("SqlLite - SocketService closed!");
            }
        }
        System.out.println("Socket-Server stopped!");
    }
}
