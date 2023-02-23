package com.example.servermail;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import model.ServerLog;

import java.io.IOException;

public class ServerMain extends Application {
    private ServerController serverController;
    private ServerLog serverLog;

    public static void main(String[] args) {
        launch();
    }

    public void start(Stage stage) throws IOException {
        this.serverLog=new ServerLog();

        FXMLLoader root = new FXMLLoader(getClass().getResource("serverConsole.fxml"));
        Scene scene = new Scene(root.load(), 700, 300);
        this.serverController = root.getController();
        serverController.setMainController(serverLog);
        Server server = new Server(8085,serverLog,stage);
        server.start();
        stage.setScene(scene);
        stage.setTitle("Console server");
        stage.getIcons().add(new Image("icon-server.png"));
        stage.show();

    }
}
