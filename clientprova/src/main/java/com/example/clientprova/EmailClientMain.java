package com.example.clientprova;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class EmailClientMain extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        System.out.println("Starting client...");
        FXMLLoader root = new FXMLLoader(getClass().getResource("index.fxml"));
        Scene scene = new Scene(root.load(), 300, 200);
        stage.setScene(scene);
        stage.setTitle("Chatty");
        stage.getIcons().add(new Image("icon-client.png"));
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}

