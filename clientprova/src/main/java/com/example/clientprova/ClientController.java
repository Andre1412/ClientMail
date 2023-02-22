package com.example.clientprova;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import model.Client;
import model.Email;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

public class ClientController {

    private final ExecutorService threadPool;
    private MainController mainController;
    Client client;
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private BooleanProperty serverStatus;

    boolean firstConn=true;

    Socket socket;
    ObjectOutputStream outputStream = null;
    ObjectInputStream inputStream = null;

    public ClientController(Client client, MainController mainController) {
        threadPool = Executors.newFixedThreadPool(10);
        this.client = client;
        this.serverStatus = new SimpleBooleanProperty(false);
        this.mainController = mainController;
    }

    public void communicate(String host, int port){
        scheduler.scheduleAtFixedRate(() ->{
            tryCommunication(host, port);
        },0, 5, TimeUnit.SECONDS);
    }



    private void tryCommunication(String host, int port){
        try {
            System.out.println("connesso");
            connectToServer(host, port);
            outputStream.writeUTF("receive");
            outputStream.writeUTF(client.getEmailAddress());
            outputStream.writeBoolean(firstConn);
            outputStream.writeUTF(client.getLastEmailFormattedDate());
            outputStream.flush();
            //Se è la prima connessione aspetto anche mail inviate
            if(firstConn) {
                //Attendo mail eliminate
                ArrayList<Email> deletedEmails = (ArrayList<Email>)inputStream.readObject();
                ArrayList<Email> sentEmails = (ArrayList<Email>) inputStream.readObject();
                System.out.println("Eliminate: "+deletedEmails);
                System.out.println("Inviate: "+sentEmails);
                Platform.runLater(()->{
                        client.setSentContent(sentEmails);
                        client.setDeletedContent(deletedEmails);});
            }
            //Attendo mail in arrivo
            ArrayList<Email> inboxEmails = (ArrayList<Email>)inputStream.readObject();

            System.out.println("Ricevute: "+inboxEmails);
            Platform.runLater(()->
                client.setInboxContent(inboxEmails));

            if(firstConn)firstConn=false;
            System.out.println("fine");
        } catch (IOException e) {
            if(serverStatus.getValue()){
                Platform.runLater(()->new AlertController(mainController.stage,"Qualcosa è andato storto", "Il server si è spento","ERROR", mainController.writeEmail, ()->null).showAndWait());
            }
            this.serverStatus.setValue(false);
        } catch (ClassNotFoundException e) {
            System.out.println("Errori nella lettura dei dati");
        }
        finally {
            closeConnections();
        }
    }

    private void closeConnections() {
        if (socket != null) {
            try {
                inputStream.close();
                outputStream.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void connectToServer(String host, int port) throws IOException {
        socket = new Socket(host, port);
        socket.setSoTimeout(3000);
        outputStream = new ObjectOutputStream(socket.getOutputStream());
        outputStream.flush();
        inputStream = new ObjectInputStream(socket.getInputStream());
        this.serverStatus.setValue(true);
    }

    public interface ResponseFunction {
        void run(ServerResponse response);
    }
    public void sendEmail(Email send, ResponseFunction response) {
        threadPool.execute(()->{
        try {

            this.connectToServer("localhost", 8085);
            outputStream.writeUTF("send");
            outputStream.writeUTF(client.getEmailAddress());
            outputStream.writeObject(send);
            outputStream.flush();
            String feedback=inputStream.readUTF();
            ServerResponse res=new ServerResponse(feedback.contains("ERROR")?"ERROR": "OK", feedback);
            if(res.getStatus()=="OK"){
                ArrayList<Email> sendArray=new ArrayList<>();
                sendArray.add(send);
                client.setSentContent(sendArray);
            }
            response.run(res);

//            return feedback;
        } catch (IOException e) {
            this.serverStatus.setValue(false);
            response.run(new ServerResponse("ERROR","Errore, il server è spento"));
        }finally{
            closeConnections();
        }

        });
    }

    public void setToRead(Email email, ResponseFunction response){
        threadPool.execute(()-> {
            try {
                this.connectToServer("localhost", 8085);
                outputStream.writeUTF("read");
                outputStream.writeUTF(client.getEmailAddress());
                outputStream.writeObject(email);
                outputStream.flush();
                if (inputStream.readUTF().contains("ERROR")) {
                    System.out.println("Errore nella modifica");
                } else System.out.println("Modificato con successo!");
            } catch (IOException e) {
                response.run(new ServerResponse("ERROR","Errore, il server è spento"));
                this.serverStatus.setValue(false);
            } finally {
                System.out.println("Chiudo");
                closeConnections();
            }
        });
    }
    public void permanentlyDelete(Email email, ResponseFunction response){
        threadPool.execute(()->{
            try{
                this.connectToServer("localhost", 8085);
                outputStream.writeUTF("permanentDelete");
                outputStream.writeUTF(client.getEmailAddress());
                outputStream.writeUTF(email.getID());
                outputStream.flush();
                String feedback=inputStream.readUTF();

                ServerResponse res=new ServerResponse(feedback.contains("ERROR")? "ERROR":"OK", feedback);
                response.run(res);
            }catch (IOException e){
                System.out.println("Errore comunicazione: "+ e.getMessage());
                this.serverStatus.setValue(false);
                response.run(new ServerResponse("ERROR", "Errore di comunicazione"));
            }finally{
                System.out.println("Chiudo");
                closeConnections();
            }
        });

    }
    public void deleteEmail(Email email, ResponseFunction response){
        threadPool.execute(()->{
            try{
                this.connectToServer("localhost", 8085);
                //this.serverStatus.setValue(true);
                outputStream.writeUTF("delete");
                outputStream.writeUTF(client.getEmailAddress());
                outputStream.writeObject(new Email(email.getID(),email.getDataSpedizione(),email.getSender(),email.getReceivers(),email.getSubject(),email.getText(),email.toReadProperty(),true));
                outputStream.flush();
                String feedback=inputStream.readUTF();

                ServerResponse res=new ServerResponse(feedback.contains("ERROR")? "ERROR":"OK", feedback);
                response.run(res);
            }catch (IOException e){
                System.out.println("Errore comunicazione: "+ e.getMessage());
                this.serverStatus.setValue(false);
                response.run(new ServerResponse("ERROR", "Errore di comunicazione"));
            }finally{
                System.out.println("Chiudo");
                closeConnections();
            }
        });
    }

    public BooleanProperty serverStatusProperty() {
        return serverStatus;
    }


    public void closeThreadpool(){
        scheduler.shutdownNow();
        threadPool.shutdownNow();
    }

}
