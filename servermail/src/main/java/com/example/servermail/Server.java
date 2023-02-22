package com.example.servermail;

import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.ServerLog;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Thread {
    Socket socket = null;
    ServerLog serverLog;
    int port;
    Stage stage;

     public Server(int port, ServerLog serverLog,Stage stage){
         this.stage=stage;
         this.port=port;
         this.serverLog=serverLog;
         this.stage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, event -> {
             interrupt();
             System.exit(0);
         });
     }

    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            Platform.runLater(()->serverLog.setLastMessage("Server pronto, in ascolto..."));
            while (true) {
                Socket socket = serverSocket.accept();
                Runnable r = new ClientThreadHandler(socket, serverLog, stage);
                new Thread(r).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
