package com.example.clientprova;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.Glow;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import model.Client;

public class ButtonTabController {

    @FXML
    public Button btnIncoming;
    @FXML
    public Button btnSent;
    @FXML
    public Button btnWrite;
    @FXML
    public Circle newEmails;
    @FXML
    public Label numberEmails;

    private Client model;
    MainController mainController;

    ClientController clientController;

    public void setMainController(MainController m, Client model, ClientController clientController){
        this.mainController = m;
        this.model = model;
        this.clientController = clientController;
        model.newEmailsProperty().addListener(((observableValue, oldValue, newValue) -> {
            newEmails.setFill(newValue.equals(0) ? Color.TRANSPARENT : Color.BLUE);
            numberEmails.setText(newValue.equals(0) ? "" : newValue.toString());
            System.out.println("Listener");
        }));
        btnIncoming.setStyle("-fx-background-color: #57598C");
        this.model.getViewProperty().addListener(((observableValue, oldV, newV) ->{
            switch (newV){
                case "incoming":
                    selectIncomingEmail();
                break;
                case "sent":
                    selectSentEmail();
                break;
                case "write":
                    selectWriteEmail();
                break;

            }
        } ));
    }


    @FXML
    public void showIncomingEmail(ActionEvent event){
        mainController.selectEmail("incoming");
    }

    @FXML
    public void showSentEmail(ActionEvent event){
        mainController.selectEmail("sent");
    }

    @FXML
    public void showWriteEmail(ActionEvent event){
        mainController.selectEmail("write");
    }

    public void selectIncomingEmail() {
        btnIncoming.setStyle("-fx-background-color: #57598C");
        btnSent.setStyle("-fx-background-color: none");
        btnWrite.setStyle("-fx-background-color: none");

    }


    public void selectSentEmail() {
        btnSent.setStyle("-fx-background-color: #57598C");
        btnIncoming.setStyle("-fx-background-color: none");
        btnWrite.setStyle("-fx-background-color: none");
        mainController.selectEmail("sent");
    }

    public void selectWriteEmail() {
        btnWrite.setStyle("-fx-background-color: #57598C");
        btnSent.setStyle("-fx-background-color: none");
        btnIncoming.setStyle("-fx-background-color: none");
        mainController.writeEmail("",null);
    }
}
