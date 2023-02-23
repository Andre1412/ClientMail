package com.example.clientprova;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import model.Client;
import model.Email;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WriteEmailController {

    @FXML
    public Label lblData;
    @FXML
    public Label lblsenderAccount;
    @FXML
    public TextField lblTo;
    @FXML
    public TextField lblSubject;
    @FXML
    public TextArea txtEmail;

    Email mail;
    Client model;
    MainController mainController;
    ClientController clientController;

    /*
    * action può assumere valore "forward", "reply", "replyall"
    * textOrSubject prende il testo della mail se action=="forward", altrimenti oggetto della mail
    * */
    public void setMainController(MainController m, Client model, ClientController clientController){
        this.mainController = m;
        this.model = model;
        this.clientController = clientController;
        lblsenderAccount.textProperty().bind(model.emailAddressProperty());
        lblTo.setPromptText("Invia a: ");
        lblSubject.setPromptText("Oggetto");

        lblTo.textProperty().addListener(((observableValue, oldV, newV) ->{
            if(!model.isWriting() && newV.trim()!="")model.setWriting(true);
            if(newV.trim()==""){
                lblTo.setStyle("-fx-border-color: none");
            }

        }));
        lblSubject.textProperty().addListener(((observableValue, oldV, newV) ->{ if(!model.isWriting() && newV.trim()!="")model.setWriting(true);}));
        txtEmail.textProperty().addListener(((observableValue, oldV, newV) ->{ if(!model.isWriting() && newV.trim()!="")model.setWriting(true);}));
    }


    public void viewWriteEmail(String action, Email mail){
        this.mail = mail;
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        String strDate = dateFormat.format(date);
        StringProperty data = new SimpleStringProperty(strDate);
        lblData.textProperty().bind(data);

        setwriteEmail(action);
    }


    @FXML
    public void sendEmail(ActionEvent actionEvent) {
        String pattern = "ddMMyyyyHHmmss";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        String date=simpleDateFormat.format(new Date());

        String uniqueID = date + "_" + UUID.randomUUID();
        if(txtEmail.getText() != ""){
            ArrayList<String> receivers = getReceivers();

            if (receivers.size() > 0) {
                Pattern patternMail = Pattern.compile("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$", Pattern.CASE_INSENSITIVE);
                Matcher matcher;
                String errorReceiver="";
                for(String account:receivers) {
                     matcher = patternMail.matcher(account);
                     if(!matcher.find()) errorReceiver+=(errorReceiver!=""? ", " : " ") + account;
                }

                if(errorReceiver==""){
                    lblTo.setStyle("-fx-border-color: none;");
                        Email send = new Email(uniqueID, lblData.getText(), lblsenderAccount.getText(), receivers, lblSubject.getText(), txtEmail.getText(), true,false);
                        clientController.sendEmail(send,response->{
                            if(response.getStatus()=="OK"){
                                Platform.runLater(()-> {
                                    clearWriteEmail();
                                    Platform.runLater(() -> new AlertController(mainController.stage,"Successo", "Mail inviata", "INFO", ()->null).showAndWait());

                                });
                            }else {
                                Platform.runLater(() -> new AlertController(mainController.stage,"ERROR: Qualcosa è andato storto", response.getMsg(), "ERROR", ()->null).showAndWait());
                                if(response.getMsg().contains("non esiste")) lblTo.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                            }

                        });
                }else {
                    lblTo.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                    new AlertController(mainController.stage,"Errore!","Receiver" + errorReceiver + " errato/i","ERROR",()->null ).showAndWait();
                }
            } else {
                lblTo.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                new AlertController(mainController.stage,"Errore!","Aggiungi almeno un destinatario","ERROR",()->null ).showAndWait();

                Alert a = new Alert(Alert.AlertType.ERROR);
            }
        } else {
            /*
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setContentText("Non puoi inviare una mail vuota");
            a.setHeaderText("Errore scrittura email");
            a.show();*/
            new AlertController(mainController.stage,"Errore!","Non puoi inviare una mail vuota","ERROR",()->null ).showAndWait();
        }
    }
    private ArrayList<String> getReceivers(){
        ArrayList<String> receivers = new ArrayList<>();
        if(lblTo.getText().trim().length()>0) {
            String[] receiv = lblTo.getText().trim().split("\\s+");
            for (int i = 0; i < receiv.length; i++) {
                receivers.add(receiv[i]);
            }
        }
        return receivers;
    }
    public void clearWriteEmail(){
        lblTo.setText("");
        lblTo.setEditable(true);
        lblSubject.setText("");
        lblSubject.setEditable(true);
        txtEmail.setText("");
        txtEmail.setEditable(true);
    }

    public void setwriteEmail(String action){
        switch(action){
            case "forward":
                clearWriteEmail();
                String txtForwarded="-----Forwarded message-----\n" +
                        "From: "+mail.getSender()+"\n"+
                        "To: "+mail.getReceivers()+ "\n"+
                        "Date: "+mail.getDataSpedizione()+"\n"+
                        "Subject: "+mail.getSubject()+"\n";

                txtEmail.setText("\n\n"+txtForwarded + mail.getText());
                txtEmail.positionCaret( 0 );
                lblSubject.setText(mail.getSubject());
                lblSubject.setEditable(false);
                lblTo.requestFocus();

                break;

            case "reply":
                clearWriteEmail();
                lblTo.setText(mail.getSender());
                lblTo.setEditable(false);
                lblSubject.setText("Re: " + mail.getSubject());
                lblSubject.setEditable(false);
                txtEmail.requestFocus();
                break;

            case "replyAll":
                clearWriteEmail();
                ArrayList<String> rec = mail.getReceivers();
                String receiver = mail.getSender();
                for(int i=0; i<rec.size(); i++){
                    if(!rec.get(i).equals(model.getEmailAddress()))
                        receiver += " " + (rec.get(i));
                }
                lblTo.setText(receiver);
                lblTo.setEditable(false);
                lblSubject.setText("Re: " + mail.getSubject());
                lblSubject.setEditable(false);
                txtEmail.requestFocus();
                break;

            case "clear":
                clearWriteEmail();
                break;
        }

    }
}

