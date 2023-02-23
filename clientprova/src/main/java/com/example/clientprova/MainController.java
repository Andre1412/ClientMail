package com.example.clientprova;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.Client;
import model.Email;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainController {
    @FXML
    public BorderPane root;
    @FXML
    public TextField txtAccount;

    private ButtonTabController buttonTab;
    private ReadEmailController readEmail;
    private WriteEmailController writeEmail;

    private ClientController clientController;
    private Client model;
    private Parent readEmailNode;
    private Parent writeEmailNode;

    private Stage stage;

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

                this.clientController = new ClientController(model);

                FXMLLoader buttonTabLoader = new FXMLLoader(getClass().getResource("buttonTab.fxml"));
                root.setLeft(buttonTabLoader.load());
                this.buttonTab = buttonTabLoader.getController();
                buttonTab.setMainController(this, model);

                FXMLLoader readEmailLoader = new FXMLLoader(getClass().getResource("readEmail.fxml"));
                readEmailNode = readEmailLoader.load();
                root.setCenter(readEmailNode);
                this.readEmail = readEmailLoader.getController();
                readEmail.setMainController(this, model, clientController);

                clientController.communicate("localhost", 8085);

                FXMLLoader writeEmailLoader = new FXMLLoader(getClass().getResource("writeEmail.fxml"));
                writeEmailNode = writeEmailLoader.load();
                this.writeEmail = writeEmailLoader.getController();
                writeEmail.setMainController(model,clientController,stage);

                model.getViewProperty().addListener(((observableValue, oldV, newV) ->{
                    selectNewView(newV,oldV);
                } ));


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

    public void selectNewView(String newView, String oldV){
        //se la vista nuova è diversa dalla precedente eseguo switch grafico
        if(oldV!=newView) {
            if(oldV.equals("write")){
                if(writeEmail.lblTo.getText().length()==0 && writeEmail.txtEmail.getText().length()==0 && writeEmail.lblSubject.getText().length()==0)
                    model.setWriting(false);
                root.setCenter(readEmailNode);
            }
            if(newView.equals("write")){
                writeEmail("",null);
            }
        }
    }


    /*
     * stuff può essere l'oggetto della mail, il sender, o i receiver+sender
     * */
    public void showWriteEmail(){
        if(!root.getCenter().equals(writeEmailNode)) {
            root.setCenter(writeEmailNode);
        }
    }
    public void writeEmail(String action, Email mail){
        showWriteEmail();
        writeEmail.viewWriteEmail(action, mail);
        if(!model.getView().equals("write"))model.setView("write");
    }


    public boolean checkAccount(String account){
        Pattern p = Pattern.compile("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
        Matcher m = p.matcher(account);
        return m.find();
    }

    public void loadAlert(String msg1,String msg2, String type, String action){
        new AlertController(stage,msg1, msg2, type, ()->{
            writeEmail.viewWriteEmail(action,readEmail.getSelectedEmail());
            return null;
        }).showAndWait();
    }



}
