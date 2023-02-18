package com.example.clientprova;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.Email;
import model.ServerLog;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Server extends Thread {
    Socket socket = null;
    ServerLog serverLog;
    int port;

     public Server(int port, ServerLog serverLog){
         this.port=port;
         this.serverLog=serverLog;
     }

    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            serverLog.setLastMessage("Server pronto, in ascolto...");
            while (true) {
                Socket socket = serverSocket.accept();
                Runnable r = new ClientThreadHandler(socket, serverLog);
                new Thread(r).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
