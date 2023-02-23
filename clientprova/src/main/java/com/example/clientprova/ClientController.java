package com.example.clientprova;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import model.Client;
import model.Email;
import model.ServerResponse;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

public class ClientController {

    private final ExecutorService threadPool;
    private Client client;
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private BooleanProperty serverStatus;

    boolean firstConn=true;

    private Socket socket;
    private ObjectOutputStream outputStream = null;
    private ObjectInputStream inputStream = null;

    public ClientController(Client client) {
        threadPool = Executors.newFixedThreadPool(10);
        this.client = client;
        this.serverStatus = new SimpleBooleanProperty(false);
    }

    public void communicate(String host, int port){
        scheduler.scheduleAtFixedRate(() ->{
            tryCommunication(host, port);
        },0, 3, TimeUnit.SECONDS);
    }



    private void tryCommunication(String host, int port){
        try {
            connectToServer(host, port);
            outputStream.writeUTF("receive");
            outputStream.writeUTF(client.getEmailAddress());
            outputStream.writeBoolean(firstConn);
            outputStream.writeUTF(client.getLastEmailFormattedDate());
            outputStream.flush();


            //Se è la prima connessione aspetto anche mail inviate ed eliminate
            if(firstConn) {
                ArrayList<Email> deletedEmails = (ArrayList<Email>)inputStream.readObject();
                ArrayList<Email> sentEmails = (ArrayList<Email>) inputStream.readObject();
                Platform.runLater(()->{
                    client.setSentContent(sentEmails);
                    client.setDeletedContent(deletedEmails);
                });
            }
            //Attendo mail in arrivo
            ArrayList<Email> inboxEmails = (ArrayList<Email>)inputStream.readObject();

            Platform.runLater(()->
                client.setInboxContent(inboxEmails));

            //Se la connessione è riuscita lo segnalo
            if(firstConn)firstConn=false;
        } catch (IOException e) {
            if(serverStatus.getValue()){
                Platform.runLater(()->new AlertController(null,"Qualcosa è andato storto", "Il server si è spento","ERROR", ()->null).showAndWait());
            }
            this.serverStatus.setValue(false);
        } catch (ClassNotFoundException e) {
            System.out.println("Errore class not found in lettura dati: " + e.getMessage());
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

    //definisco interfaccia per la funzione di callback per inviare il feedback al chiamante
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
            if(res.getStatus().equals("OK")){
                ArrayList<Email> sendArray=new ArrayList<>();
                sendArray.add(send);
                client.setSentContent(sendArray);
            }
            response.run(res);

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
                outputStream.writeObject(new Email(email.getID(),email.getDataSpedizione(),email.getSender(),email.getReceivers(),email.getSubject(),email.getText(),false,email.deletedProperty()));
                outputStream.flush();

                if (inputStream.readUTF().contains("ERROR")) {
                    response.run(new ServerResponse("ERROR","Errore, operazione non riuscita"));
                }else  response.run(new ServerResponse("OK","Operazione riuscita"));
            } catch (IOException e) {
                response.run(new ServerResponse("ERROR","Errore, il server è spento"));
                this.serverStatus.setValue(false);
            } finally {
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
                this.serverStatus.setValue(false);
                response.run(new ServerResponse("ERROR", "Errore di comunicazione"));
            }finally{
                closeConnections();
            }
        });

    }
    public void deleteEmail(Email email, ResponseFunction response){
        threadPool.execute(()->{
            try{
                this.connectToServer("localhost", 8085);
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
