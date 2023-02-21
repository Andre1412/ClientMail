package com.example.clientprova;

import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import model.Client;
import model.Email;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class EmailCell extends ListCell<Email> {

    @FXML
    public Label dateLabel;

    @FXML
    public Label mailAccount;

    @FXML
    public Label mailText;
    public ListView parent;
    String fullText;
    Client model;
    Stage stage;
    int nChar;
    public EmailCell(Client model, ListView parent, Stage stage) {
        try {
            this.model = model;
            FXMLLoader loader = new FXMLLoader(this.getClass().getResource("emailCell.fxml"));
            loader.setController(this);
            loader.setRoot(this);
            loader.load();
            this.parent=parent;
            this.stage=stage;
            nChar=40;
            itemProperty().addListener((obs, oldValue, newValue) -> {
                // Empty cell
                if (newValue == null) {
                    return;
                }

                this.fullText=newValue.getText();
                mailAccount.setText(model.getView()=="sent"?  String.join(", ", newValue.getReceivers()).length()>22? String.join(", ", newValue.getReceivers()).substring(0,22)+"...": String.join(", ", newValue.getReceivers()):newValue.getSender());
                mailText.setText(String.join("  -  ", List.of(newValue.getSubject(), newValue.getText().length()>30? newValue.getText().replace("\n","")
                        .substring(0, 30) + "...": newValue.getText().replace("\n",""))));


                if(newValue.toReadProperty()){
                    System.out.println("Mail to read: "+ newValue + newValue.getID() +newValue.toReadProperty());
                    if(!getStyleClass().contains("toRead")) getStyleClass().add("toRead");
                }else {
                    if(getStyleClass().contains("toRead")) getStyleClass().remove("toRead");
                }

                dateLabel.setText(newValue.getDataSpedizione());
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                });


        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    protected void updateItem(Email item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setContentDisplay(ContentDisplay.TEXT_ONLY);
            setDisable(true);
            return;
        }
        setDisable(false);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }

}
