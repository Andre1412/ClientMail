package com.example.clientprova;

import com.google.gson.Gson;
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


    private boolean running = true;

    public ClientThreadHandler(Socket in, ServerLog serverLog) {
        this.incoming = in;
        this.serverLog = serverLog;
    }

    @Override
    public void run() {
        try{
            try {
                openStreams(incoming);
                System.out.println("inizia a servire");
                while(inStream.available()<=0){}
                String op=inStream.readUTF();
                System.out.println("OP: "+op);
                String clientName;
                switch(op){
                    case "send":
                        System.out.println("Send");
                        clientName = inStream.readUTF();
                        Email email = (Email) inStream.readObject();
                        System.out.println(email);
                        ArrayList<String> receivers = email.getReceivers();
                        File emailSentInClient = new File("clientprova/src/main/resources/com/example/clientprova/" + clientName + "/" + email.getID() + ".txt");
                        System.out.println(emailSentInClient);
                        emailSentInClient.createNewFile();
                        System.out.println("File creato" + emailSentInClient);
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
                        System.out.println("Email id:" + email.getID() + " da [" + email.getSender() + "] a "+ email.getReceivers() + " inviata!");
                        break;
                    case "receive":
                        receive();
                        break;

                    case "read":
                        clientName = inStream.readUTF();
                        Email newEmail = (Email) inStream.readObject();
                        System.out.println("Letta mail: "+ newEmail);
                        File emailToUpdate=new File("clientprova/src/main/resources/com/example/clientprova/"+ clientName+"/"+newEmail.getID()+".txt");
                        FileWriter f;
                        try {
                            f = new FileWriter(emailToUpdate, false);
                            f.write(new Gson().toJson(newEmail));
                            f.close();
                            outStream.writeUTF("Success: Email modificata con successo");
                        }catch (IOException e) {
                                e.printStackTrace();
                                outStream.writeUTF("Error: Errore nella modifica");
                        }finally {
                            outStream.flush();
                        }

                        break;
                    case "delete":
                        System.out.println("Leggo dato...");

                        clientName=inStream.readUTF();
                        String id=inStream.readUTF();
                        System.out.println("Letto...");
                        File emailToDelete=new File("clientprova/src/main/resources/com/example/clientprova/"+ clientName+"/"+id+".txt");
                        System.out.println(emailToDelete.exists());

                        if (emailToDelete.delete()) {
                            outStream.writeUTF("Ok");
                            outStream.flush();
                            System.out.println("Eliminata email " + id);
                        } else {
                            System.out.println("Failed to delete the file."+ emailToDelete);
                            outStream.writeUTF("Failed to delete the file");
                            outStream.flush();
                            File resource = new File("clientprova/src/main/resources/com/example/clientprova/"+clientName);

                        }
                        break;
                }
            } catch (ClassNotFoundException e) {
                System.out.println(e.getMessage() + " " + e.getCause());
                throw new RuntimeException(e);
            } finally{
                closeStreams();
                incoming.close();
            }
        } catch (IOException e){
            e.printStackTrace();
        }



    }
    private void receive(){
        System.out.println("before try");
            try {
                String clientName = inStream.readUTF();
                String dateLastCheck = (String) inStream.readUTF();
                ArrayList<Email> emailList=new ArrayList<>();

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
                        //TODO: la sincronizzazione non permettere di vedere email nuove
                        //messo iterator perché altrimenti dà currentModificationException
                        //se data non è nulla tolgo dalla lista dei file quelli con data <=alla data inviata
                        /*if(!dateLastCheck.equals("null")) {
                            System.out.println("Prima   -------- "+directoryListing.size());
                            Iterator<File> iterator = directoryListing.iterator();
                            while (iterator.hasNext()) {
                                File f = iterator.next();
                                String fileDate = f.getName().split("_")[0];
                                if(fileDate.compareTo(dateLastCheck) <= 0){
                                    System.out.println("dateLastCheck: " + dateLastCheck + "   fileDate: " + fileDate);
                                    iterator.remove();
                                }
                            }
                            System.out.println("Dopo ---------- "+directoryListing.size());
                        }*/

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
                            emailList.add(0, e);
                        }
                    }
                }
                //Popolo array di mail ricevute e inviate
                ArrayList<Email> clientReceivedMail = new ArrayList<>();
                emailList.forEach(eR -> {
                    if (eR.getReceivers().contains(clientName)) {
                        clientReceivedMail.add(eR);
                    }
                });
                //invio mail inviate solo se è la prima connessione
                if(dateLastCheck.equals("null")) {
                    ArrayList<Email> clientSentMail = new ArrayList<>();
                    emailList.forEach(eS -> {
                        if (eS.getSender().equals(clientName)) {
                            clientSentMail.add(eS);
                        }
                    });

                    outStream.writeObject(clientSentMail);
                }

                outStream.writeObject(clientReceivedMail);
                outStream.flush();
                System.out.println("Utente " + clientName + " ha effettuato l'accesso");

            } catch (IOException e) {
                System.out.println(e.getMessage()+e.getCause());
                //e.printStackTrace();
            }
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

    private void closeStreams() {
        try {
            if(inStream != null) {
                inStream.close();
            }

            if(outStream != null) {
                outStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

