package com.example.clientprova;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.Client;
import model.Email;
import model.ServerLog;

import java.io.*;
import java.lang.reflect.Array;
import java.net.Socket;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ClientThreadHandler implements Runnable {
    Socket incoming;
    ObjectInputStream inStream = null;
    ObjectOutputStream outStream = null;
    ServerLog serverLog;
    Stage stage;
    String clientName;

    private boolean running = true;

    public ClientThreadHandler(Socket in, ServerLog serverLog, Stage stage) {
        this.incoming = in;
        this.serverLog = serverLog;
        this.stage = stage;
        this.stage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, event -> {
            stop();
            System.exit(0);
        });
    }

    @Override
    public void run() {
        try{
            try {
                openStreams(incoming);
                while(inStream.available()<=0){}
                String op=inStream.readUTF();
                this.clientName = inStream.readUTF();
                switch(op){
                    case "send":
                        try {
                            Email email = (Email) inStream.readObject();
                            ArrayList<String> receivers = email.getReceivers();
                            String errorReceivers="";
                            for(int i = 0; i<receivers.size() && errorReceivers==""; i++){
                                if(!new File("clientprova/src/main/resources/com/example/clientprova/"+receivers.get(i)).exists()){
                                    errorReceivers="ERROR: Utente "+ clientName + " non esiste!";
                                    outStream.writeUTF(errorReceivers);
                                    outStream.flush();
                                    Platform.runLater(() -> serverLog.setLastMessage("ERROR: Impossibile inviare mail " + email.getID() +" dell'utente " + clientName + ", receiver non esiste"));
                                }
                            }
                            if(errorReceivers=="") {
                                File emailSentInClient = new File("clientprova/src/main/resources/com/example/clientprova/" + clientName + "/" + email.getID() + ".txt");
                                emailSentInClient.createNewFile();
                                FileWriter fileWriter = new FileWriter(emailSentInClient);
                                fileWriter.write(new Gson().toJson(email));
                                fileWriter.close();
                                for (int i = 0; i < receivers.size(); i++) {
                                    File emailSentInReceivers = new File("clientprova/src/main/resources/com/example/clientprova/" + receivers.get(i) + "/" + email.getID() + ".txt");
                                    emailSentInReceivers.createNewFile();
                                    fileWriter = new FileWriter(emailSentInReceivers);
                                    fileWriter.write(new Gson().toJson(email));
                                    fileWriter.close();
                                }
                                outStream.writeUTF("Success: Email inviata con successo");
                                outStream.flush();
                                Platform.runLater(() -> serverLog.setLastMessage("Email id:" + email.getID() + " da [" + email.getSender() + "] a " + email.getReceivers() + " inviata!"));
                            }

                        }catch (IOException e){
                            Platform.runLater(() -> serverLog.setLastMessage("ERROR: errore comunicazione in invio mail da client " + clientName ));
                            outStream.writeUTF("ERROR: errore in invio mail");
                        }
                        break;

                    case "receive":
                        receive();
                        break;

                    case "read":
                        try {
                            Email newEmail = (Email) inStream.readObject();
                            File emailToUpdate=new File("clientprova/src/main/resources/com/example/clientprova/"+ clientName+"/"+newEmail.getID()+".txt");
                            FileWriter f;
                            f = new FileWriter(emailToUpdate, false);
                            f.write(new Gson().toJson(newEmail));
                            f.close();
                            Platform.runLater(()->serverLog.setLastMessage("Email di utente" +clientName+" con id:" + newEmail.getID()+ " segnata come letta"));
                            outStream.writeUTF("Success: Email modificata con successo");
                        }catch (IOException e) {
                            e.printStackTrace();
                            Platform.runLater(()->serverLog.setLastMessage("ERROR: Email di " + clientName +  " non segnata come letta, errore comunicazione"));
                            outStream.writeUTF("ERROR: Errore nella modifica");
                        }finally {
                            outStream.flush();
                        }

                        break;
                    case "delete":
                        try {
                            Email deletedEmail = (Email) inStream.readObject();
                            File emailToDelete = new File("clientprova/src/main/resources/com/example/clientprova/" + clientName + "/" + deletedEmail.getID() + ".txt");
                            FileWriter f;
                            f = new FileWriter(emailToDelete, false);
                            f.write(new Gson().toJson(deletedEmail));
                            f.close();
                            Platform.runLater(() -> serverLog.setLastMessage("Eliminata email " + deletedEmail.getID()));
                            outStream.writeUTF("Ok");
                            outStream.flush();
                        }catch (IOException e){
                            Platform.runLater(()->serverLog.setLastMessage("ERROR: Errore eliminazione mail client"+ clientName));
                            outStream.writeUTF("ERROR: Failed to delete the file");
                            outStream.flush();
                        }
                        break;
                }
            } catch (ClassNotFoundException e) {
                System.out.println(e.getMessage() + " " + e.getCause());
                throw new RuntimeException(e);
            } finally{
                closeStreams();
                incoming.close();
                Platform.runLater(()-> serverLog.setLastMessage("Utente " + clientName + " disconnesso"));

            }
        } catch (IOException e){
            Platform.runLater(()->serverLog.setLastMessage("ERROR: Errore chiusura stream client "+ clientName));
            e.printStackTrace();
        }
    }
    private void receive(){
        try {
                boolean firstConn = (boolean) inStream.readBoolean();
                String dateLastCheck = (String) inStream.readUTF();
            System.out.println(dateLastCheck);

            ArrayList<Email> emailList=new ArrayList<>();
                Platform.runLater(()-> serverLog.setLastMessage("Utente " + clientName + (dateLastCheck=="null"? " ha effettuato l'accesso, invio mail": " connesso, ricerca nuove mail")));

                //Cerco directory del client
                File resource = new File("clientprova/src/main/resources/com/example/clientprova/"+clientName);

                //Se non c'è directory invio mex di errore
                if (!resource.exists()) {
                    resource.mkdir();
                } else {
                    //Se c'era cerco se ci sono file interni
                    ArrayList<File> directoryListing = new ArrayList<>(Arrays.asList(resource.listFiles()));
                    if (directoryListing != null) {
                        if(!dateLastCheck.equals("null")) {
                            directoryListing.removeIf((f)-> {
                                        String fileDate = f.getName().split("_")[0];
                                        return fileDate.compareTo(dateLastCheck) <= 0;
                                    }
                            );
                        }
                        //se data non è nulla tolgo dalla lista dei file quelli con data <=alla data inviata
                        //leggo mail dal file e la inserisco in array emailList
                        BufferedReader reader;
                        StringBuilder sb;
                        for (File child : directoryListing) {
                            reader = new BufferedReader(new FileReader(child.getPath()));
                            String line = reader.readLine();
                            sb = new StringBuilder();
                            while (line != null) {
                                sb.append(line);
                                sb.append("\n");
                                line = reader.readLine();
                            }
                            reader.close();
                            String json = sb.toString();
                            //String json = "{'ID':'abc34e','data':'2023-01-05','sender':'io@gmail.com','receivers':['tu@mail.it','prova.gmail.it', 'studente@unito.it'],'subject':'oggetto','text':''}";
                            Gson gson = new Gson();
                            Email e = gson.fromJson(json, Email.class);
                            System.out.println("Mail "+ e.toReadProperty());
                            emailList.add(0, e);
                        }
                    }
                }
                //Popolo array di mail eliminate
                ArrayList<Email> clientDeletedMail = new ArrayList<>();
                emailList.removeIf(eD -> {
                    if(eD.isDeleted()) {
                        clientDeletedMail.add(eD);
                    }
                    return eD.isDeleted();
                });

                //Popolo array di mail ricevute e inviate (escluse quelle eliminate)
                ArrayList<Email> clientReceivedMail = new ArrayList<>();
                emailList.forEach(eR -> {
                    if (eR.getReceivers().contains(clientName)) {
                        clientReceivedMail.add(eR);
                    }
                });


            //invio mail inviate ed eliminate solo se è la prima connessione
                if(firstConn) {
                    ArrayList<Email> clientSentMail = new ArrayList<>();
                    emailList.forEach(eS -> {
                        if (eS.getSender().equals(clientName)) {
                            clientSentMail.add(eS);
                        }
                    });
                    outStream.writeObject(clientDeletedMail);
                    Platform.runLater(()-> serverLog.setLastMessage("Utente " + clientName + (clientDeletedMail.size()>0? " riceve mail eliminate: " + printMailArray(clientDeletedMail): " nessuna mail eliminata")));
                    outStream.writeObject(clientSentMail);
                    Platform.runLater(()-> serverLog.setLastMessage("Utente " + clientName + (clientSentMail.size()>0? " riceve mail inviate: " + printMailArray(clientSentMail): " nessuna mail inviata")));
                }
            outStream.writeObject(clientReceivedMail);
            Platform.runLater(()-> serverLog.setLastMessage("Utente " + clientName + (clientReceivedMail.size()>0? " riceve mail in arrivo: " + printMailArray(clientReceivedMail): " nessuna mail ricevuta")));


            outStream.flush();

            } catch (IOException e) {
                System.out.println(e.getMessage()+e.getCause());
                Platform.runLater(()-> serverLog.setLastMessage("ERROR: Utente " + clientName + " errore di comunicazione"));
            //e.printStackTrace();
            }
        }

    public String printMailArray(ArrayList<Email> mailArray) {
            String print = "{\n";
            for (Email e : mailArray) {
                print += "\t" + e.toString()+"\n";
            }
            print += "}";
            return print;
        }

    private void openStreams(Socket socket) throws IOException {
        System.out.println("Server Connesso");
        inStream = new ObjectInputStream(socket.getInputStream());
        outStream = new ObjectOutputStream(socket.getOutputStream());
        outStream.flush();
    }
    public void stop() {
        running = false;
    }

    private void closeStreams() throws IOException {
            if(inStream != null) {
                inStream.close();
            }

            if(outStream != null) {
                outStream.close();
            }

    }
}

