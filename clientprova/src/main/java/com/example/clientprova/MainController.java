package com.example.clientprova;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.Client;
import model.Email;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainController {
    @FXML
    public BorderPane root;
    @FXML
    public TextField txtAccount;

    ButtonTabController buttonTab;
    ReadEmailController readEmail;
    WriteEmailController writeEmail;

    ClientController clientController;
    String view="incoming";

    private Client model;
    Parent readEmailNode;
    Parent writeEmailNode;

    Stage stage;

    @FXML
    public void loadMainController(ActionEvent mouseEvent){
        String account;
        if(checkAccount(txtAccount.getText())){
            account = txtAccount.getText();
            stage = (Stage) root.getScene().getWindow();
            stage.setHeight(600);
            stage.setWidth(977);
            stage.centerOnScreen();
            stage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, event -> {
                if (clientController != null){
                    System.out.println("Utente " + account + " disconnesso");
                    clientController.closeThreadpool();
                }
            });
            try {
                if (this.model != null)
                    throw new IllegalStateException("Model can only be initialized once");
                model = new Client(account);

                this.clientController = new ClientController(model, this);

                FXMLLoader buttonTabLoader = new FXMLLoader(getClass().getResource("buttonTab.fxml"));
                root.setLeft(buttonTabLoader.load());
                this.buttonTab = buttonTabLoader.getController();
                buttonTab.setMainController(this, model, clientController);

                FXMLLoader readEmailLoader = new FXMLLoader(getClass().getResource("readEmail.fxml"));
                readEmailNode = readEmailLoader.load();
                root.setCenter(readEmailNode);
                this.readEmail = readEmailLoader.getController();
                readEmail.setMainController(this, model, clientController);

                clientController.communicate("localhost", 8085);

                FXMLLoader writeEmailLoader = new FXMLLoader(getClass().getResource("writeEmail.fxml"));
                writeEmailNode = writeEmailLoader.load();
                this.writeEmail = writeEmailLoader.getController();

            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (RuntimeException e){
                //Se il server non è disponibile
                System.out.println(e.getMessage());
                System.exit(1);
            }
        }else {
                txtAccount.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                Label error=new Label();
                error.setText("Inserire email valida");
                error.setId("errorLabel");
                root.setBottom(error);
        }
    }

    public void selectEmail(String newVue){
        if(view!=newVue) {
            if(view=="write"){
                if(writeEmail.lblTo.getText().length()==0 && writeEmail.txtEmail.getText().length()==0 && writeEmail.lblSubject.getText().length()==0)
                    model.setWriting(false);
                root.setCenter(readEmailNode);
            }
            view=newVue;
            readEmail.changeView(newVue);
        }
    }


    /*
     * stuff può essere l'oggetto della mail, il sender, o i receiver+sender
     * */
    public void showWriteEmail(){
        if(!root.getCenter().equals(writeEmailNode)) {
            view="write";
            model.setView("write");
            root.setCenter(writeEmailNode);
        }
    }
    public void writeEmail(String action, Email mail){
        showWriteEmail();
        writeEmail.setMainController(this, model, clientController, action, mail);
        if(view!="write")view="write";
    }


    public boolean checkAccount(String account){
        Pattern p = Pattern.compile("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
        Matcher m = p.matcher(account);
        return m.find();
    }

    public void loadAlert(String msg1, String msg2, String type, String action){
        new AlertController(msg1, msg2, type,action, writeEmail, ()->{
            if(!model.isWriting()) {
                model.setWriting(true);
            }
            writeEmail.setMainController(this,model,clientController,action,readEmail.getSelectedEmail());
            return null;
        }).showAndWait();
    }



}
