package com.example.clientprova;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import model.Email;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class EmailCell extends ListCell<Email> {
    @FXML
    public Circle readMark;
    @FXML
    public Label dateLabel;
    @FXML
    public Label mailLabel;

    public EmailCell() {
        try {
            FXMLLoader loader = new FXMLLoader(this.getClass().getResource("emailCell.fxml"));
            loader.setController(this);
            loader.setRoot(this);
            loader.load();
            itemProperty().addListener((obs, oldValue, newValue) -> {
                // Empty cell
                if (newValue == null) {
                    return;
                }
                System.out.println("Da leggere mail "+ newValue + " " + newValue.isToRead());
                mailLabel.setText(newValue.toString());

                Calendar today = Calendar.getInstance();

                Calendar date = Calendar.getInstance();


                DateFormat df;
                 /*   if (date.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                            && date.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
                        df = new SimpleDateFormat("HH:mm");
                    } else {
                        df = new SimpleDateFormat("dd MMM yy, HH:mm");
                    }*/
                    dateLabel.setText(newValue.getDataSpedizione());
                    readMark.setFill(newValue.isToRead() ? Color.BLUE : Color.TRANSPARENT);
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
