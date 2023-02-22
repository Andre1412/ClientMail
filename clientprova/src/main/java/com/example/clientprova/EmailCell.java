package com.example.clientprova;

import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
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

    @FXML
    public Button cellDeleteBtn;
    public ListView parent;
    String fullText;
    Client model;
    Stage stage;
    int nChar;
    public EmailCell(Client model, ListView parent, Stage stage, ReadEmailController readController) {
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

                cellDeleteBtn.setOnAction((event)->{
                    readController.setSelectedEmail(newValue);
                    readController.onDeleteButtonClick(event);
                });
                mailAccount.setText(model.getView()=="sent"? "A: " + (String.join(", ", newValue.getReceivers()).length()>20? String.join(", ", newValue.getReceivers()).substring(0,20)+"...": String.join(", ", newValue.getReceivers())):newValue.getSender());

                String text=(newValue.getSubject()+ " - " + newValue.getText()).length()>45? (newValue.getSubject() + " - " + newValue.getText().replace("\n",""))
                        .substring(0, 45) + "...": (newValue.getSubject()+ " - " + newValue.getText().replace("\n",""));
                mailText.setText(text);


                if(newValue.toReadProperty()){
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
