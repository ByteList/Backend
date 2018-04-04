package de.gamechest.backend.web.socket;

import de.gamechest.backend.Backend;
import de.gamechest.backend.log.BackendLogger;
import org.bson.Document;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by ByteList on 04.04.2018.
 * <p>
 * Copyright by ByteList - https://bytelist.de/
 */
public class SocketService {


    private final BackendLogger logger;

    private final int port;
    private final boolean local;

    public SocketService(BackendLogger logger, int port, boolean local) {
        this.logger = logger;
        this.port = port;
        this.local = local;
    }

    public void startSocketServer(Backend backend) {
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

                        logger.info("[S "+client.getInetAddress().getHostAddress()+"] "+target);

                        Document document = Document.parse(target);
                        StringBuilder stringBuilder = new StringBuilder();
                        document.forEach((s, o) -> stringBuilder.append(o.toString()));

                        printWriter.println(stringBuilder.toString());
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
}
