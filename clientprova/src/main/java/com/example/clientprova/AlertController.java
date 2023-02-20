package com.example.clientprova;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import model.Client;

import java.io.IOException;
import java.util.concurrent.Callable;

public class AlertController extends Dialog<ButtonType> {

    @FXML
    public Label info;
    @FXML
    public Label msg1;
    @FXML
    public Label msg2;
    @FXML
    public Button okBtn;
    @FXML
    public Button noBtn;
    private FXMLLoader loader;

    Client model;

    public AlertController(String msg1, String msg2, String type, WriteEmailController writeEmailController, Callable<Void> f){
        try{
            Window window = getDialogPane().getScene().getWindow();
            window.setOnCloseRequest(event -> window.hide());
            this.loader = new FXMLLoader(getClass().getResource("alert.fxml"));
            loader.setController(this);
            DialogPane dialogPane = loader.load();
            model = writeEmailController.model;
            initOwner(null);
            initModality(Modality.APPLICATION_MODAL);
            initStyle(StageStyle.TRANSPARENT);



            noBtn.setOnAction(event->{
                this.setResult(ButtonType.CLOSE);
                if(!model.isWriting()) {
                    model.setWriting(true);
                }
                close();
            });

            if(type=="ERROR"){
                info.setText("Errore!");
                noBtn.setText("OK");
                okBtn.setVisible(false);
            }else{
                noBtn.setText("Cancel");
                okBtn.setText("Yes");
                okBtn.setOnAction(event->{
                    this.setResult(ButtonType.CLOSE);
                    close();
                    try {
                        f.call();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                });
                info.setText("Attenzione!");
            }
            this.msg2.setText(msg2);
            this.msg1.setText(msg1);
            setDialogPane(dialogPane);
        }catch (IOException e){
            System.out.println(e);
        }
    }



}
