package com.example.clientprova;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
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
    public Circle readMark;
    @FXML
    public Label dateLabel;

    @FXML
    public Label mailAccount;

    @FXML
    public Label mailText;

    Client model;
    public EmailCell(Client model) {
        try {
            this.model = model;
            FXMLLoader loader = new FXMLLoader(this.getClass().getResource("emailCell.fxml"));
            loader.setController(this);
            loader.setRoot(this);
            loader.load();
            itemProperty().addListener((obs, oldValue, newValue) -> {
                // Empty cell
                if (newValue == null) {
                    return;
                }
                System.out.println("Da leggere mail "+ newValue + " " + newValue.toReadProperty());
                mailAccount.setText(model.getView()=="incoming"? newValue.getSender(): newValue.getReceivers().toString().length()>20? newValue.getReceivers().toString().substring(0,20)+"...": newValue.getReceivers().toString());
                mailText.setText(String.join("  -  ", List.of(newValue.getSubject(),  newValue.getText().length()>40? newValue.getText().replace("\n","")
                        .substring(0, 40) + "...": newValue.getText().replace("\n",""))));
                Calendar today = Calendar.getInstance();

                Calendar date = Calendar.getInstance();


                dateLabel.setText(newValue.getDataSpedizione());


                //DateFormat df;
                 /*   if (date.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                            && date.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
                        df = new SimpleDateFormat("HH:mm");
                    } else {
                        df = new SimpleDateFormat("dd MMM yy, HH:mm");
                    }*/
                    //dateLabel.setText(newValue.getDataSpedizione());
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
