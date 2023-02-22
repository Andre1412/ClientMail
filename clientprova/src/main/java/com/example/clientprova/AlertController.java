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

    double xOffset;
    double yOffset;
    Client model;

    public AlertController(Stage owner,String msg1, String msg2, String type, WriteEmailController writeEmailController, Callable<Void> f){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("alert.fxml"));
            loader.setController(this);

            DialogPane dialogPane = loader.load();
            initOwner(owner);
            initModality(Modality.WINDOW_MODAL);
            //initStyle(StageStyle.TRANSPARENT);


            getDialogPane().getScene().setOnMousePressed(event -> {
                // Prende la posizione al click del mouse
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            });

            getDialogPane().getScene().setOnMouseDragged(event -> {
                // Muove la finestra basandosi sulla posizione corrente
                getDialogPane().getScene().getWindow().setX(event.getScreenX() - xOffset);
                getDialogPane().getScene().getWindow().setY(event.getScreenY() - yOffset);
            });
            model = writeEmailController.model;



            noBtn.setOnAction(event->{
                this.setResult(ButtonType.CLOSE);
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
            this.setTitle("Attenzione!");
            setDialogPane(dialogPane);
        }catch (IOException e){
            System.out.println(e);
        }
    }
 //TODO: se vogliamo tenere la barra di pulsanti di sistema, lasciare il metodo sotto
    /*

    @FXML private void initialize() {
        Window window = getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(event -> window.hide());
    } */
}
