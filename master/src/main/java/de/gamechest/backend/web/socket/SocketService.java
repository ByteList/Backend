package de.gamechest.backend.web.socket;

import de.gamechest.backend.Backend;
import org.bson.Document;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by ByteList on 04.04.2018.
 * <p>
 * Copyright by ByteList - https://bytelist.de/
 */
public class SocketService {

    public void startSocketServer() {
        new Thread(()-> {
            ServerSocket server = null;
            try {
                server = new ServerSocket(6000);
            } catch (IOException e) {
                e.printStackTrace();
            }

            while(Backend.getInstance().isRunning){
                try {
                    if (server != null) {
                        Socket client  = server.accept();
                        InputStreamReader inputStreamReader =  new InputStreamReader(client.getInputStream());
                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                        PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(client.getOutputStream()));
                        String text = bufferedReader.readLine();

                        System.out.println(text);

                        Document document = Document.parse(text);

                        printWriter.println(document.getString("tab"));
                        printWriter.flush();
                        printWriter.close();
                    }
                }
                catch (IOException ex){
                    ex.printStackTrace();
                }
            }
        }).start();

    }
}
