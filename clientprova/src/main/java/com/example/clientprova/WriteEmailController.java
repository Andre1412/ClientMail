package com.example.clientprova;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Client;
import model.Email;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WriteEmailController {
    @FXML
    public Label lblsenderAccount;
    @FXML
    public TextField lblTo;
    @FXML
    public TextField lblSubject;
    @FXML
    public TextArea txtEmail;

    private Email mail;
    private Client model;
    private ClientController clientController;
    private Stage stage;

    public void setMainController( Client model, ClientController clientController, Stage stage){
        this.model = model;
        this.clientController = clientController;
        this.stage=stage;
        lblsenderAccount.textProperty().bind(model.emailAddressProperty());
        lblTo.setPromptText("Invia a: ");
        lblSubject.setPromptText("Oggetto");

        lblTo.textProperty().addListener(((observableValue, oldV, newV) ->{
            if(!model.isWriting() && !newV.trim().equals(""))model.setWriting(true);
            if(newV.trim().equals("")){
                lblTo.setStyle("-fx-border-color: none");
            }

        }));
        lblSubject.textProperty().addListener(((observableValue, oldV, newV) ->{ if(!model.isWriting() && !newV.trim().equals(""))model.setWriting(true);}));
        txtEmail.textProperty().addListener(((observableValue, oldV, newV) ->{ if(!model.isWriting() && !newV.trim().equals(""))model.setWriting(true);}));
    }


    public void viewWriteEmail(String action, Email mail){
        this.mail = mail;
        setwriteEmail(action);
    }


    @FXML
    public void sendEmail(ActionEvent actionEvent) {
        String pattern = "ddMMyyyyHHmmss";
        Date newDate= new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        String date=simpleDateFormat.format(newDate);

        String uniqueID = date + "_" + UUID.randomUUID();
        if(!txtEmail.getText().equals("")){
            ArrayList<String> receivers = getReceivers();

            if (receivers.size() > 0) {
                Pattern patternMail = Pattern.compile("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$", Pattern.CASE_INSENSITIVE);
                Matcher matcher;
                String errorReceiver="";
                for(String account:receivers) {
                     matcher = patternMail.matcher(account);
                     if(!matcher.find()) errorReceiver+=(!errorReceiver.equals("") ? ", " : " ") + account;
                }

                if(errorReceiver.equals("")){
                    DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                    String strDate = dateFormat.format(newDate);

                    lblTo.setStyle("-fx-border-color: none;");
                        Email send = new Email(uniqueID, strDate, lblsenderAccount.getText(), receivers, lblSubject.getText(), txtEmail.getText(), true,false);
                        clientController.sendEmail(send,response->{
                            if(response.getStatus().equals("OK")){
                                Platform.runLater(()-> {
                                    clearWriteEmail();
                                    Platform.runLater(() -> new AlertController(this.stage,"Successo", "Mail inviata", "INFO", ()->null).showAndWait());

                                });
                            }else {
                                Platform.runLater(() -> new AlertController(null,"ERROR: Qualcosa è andato storto", response.getMsg(), "ERROR", ()->null).showAndWait());
                                if(response.getMsg().contains("non esiste")) lblTo.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                            }

                        });
                }else {
                    lblTo.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                    new AlertController(this.stage,"Errore!","Receiver" + errorReceiver + " errato/i","ERROR",()->null ).showAndWait();
                }
            } else {
                lblTo.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                new AlertController(this.stage,"Errore!","Aggiungi almeno un destinatario","ERROR",()->null ).showAndWait();
            }
        } else {
            new AlertController(this.stage,"Errore!","Non puoi inviare una mail vuota","ERROR",()->null ).showAndWait();
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

