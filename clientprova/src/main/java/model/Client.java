package model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.net.Socket;
import java.util.*;



public class Client {

    Socket socket = null;
    ObjectOutputStream outputStream = null;
    ObjectInputStream inputStream = null;

    private final ObservableList<Email> inboxContent;
    private final ObservableList<Email> sentContent;
    private final ObservableList<Email> deletedContent;
    private final ObservableList<Email> currentEmails;
    private final StringProperty emailAddress;
    private SimpleStringProperty view;
    private boolean isWriting;
    private SimpleIntegerProperty newEmails;

    final int MAX_ATTEMPTS = 5;


    @Override
    public String toString() {
        return "Client{" +
                "socket=" + socket +
                ", outputStream=" + outputStream +
                ", inputStream=" + inputStream +
                ", inboxContent=" + inboxContent +
                ", sentContent=" + sentContent +
                ", emailAddress=" + emailAddress +
                ", MAX_ATTEMPTS=" + MAX_ATTEMPTS +
                '}';
    }

    /**
     * Costruttore della classe.
     *
     * @param emailAddress   indirizzo email
     *
     */

    public Client(String emailAddress) {
        this.inboxContent = FXCollections.observableArrayList(new ArrayList<>());
        this.sentContent = FXCollections.observableArrayList(new ArrayList<>());
        this.deletedContent = FXCollections.observableArrayList(new ArrayList<>());
        this.currentEmails = FXCollections.observableArrayList(new ArrayList<>());
        currentEmails.setAll(inboxContent);
        this.emailAddress = new SimpleStringProperty(emailAddress);
        isWriting=false;
        newEmails=new SimpleIntegerProperty(0);
        view=new SimpleStringProperty("incoming");
    }

    public void setView(String view) {
        this.view.set(view);
    }


    public String getView() {
        return view.get();
    }

    public boolean isWriting() {
        return isWriting;
    }

    public void setWriting(boolean writing) {
        isWriting=writing;
    }

    public boolean isNewEmails() {
        return newEmails.getValue()==0;
    }

    public void setNewEmails() {
        int newEmails=0;
        for(Email e: inboxContent){
            if(e.toReadProperty()){
                newEmails++;
            }
        }
        this.newEmails.setValue(newEmails);
    }

    public void permanentlyDelete(Email mail){
        deletedContent.remove(mail);
        setCurrentEmails();
    }

    public SimpleIntegerProperty newEmailsProperty() {
        return newEmails;
    }

    public SimpleStringProperty getViewProperty() {
        return view;
    }

    //set inbox to the list of emails passed as parameter
    public void setInboxContent(ArrayList<Email> inbox) {
        if(inbox.size()>0) {
            Collections.sort(inbox, Collections.reverseOrder());

            this.inboxContent.addAll(0, inbox);
            setNewEmails();
            if(view.getValue().equals("incoming"))
                setCurrentEmails();
        }
    }

    //set sent to the list of emails passed as parameter
    public void setSentContent(ArrayList<Email> sent) {
        if(sent.size()>0) {
            Collections.sort(sent, Collections.reverseOrder());
            int i=0;
            for (Email e : sent) {
                if(e.getReceivers().contains(e.getSender())){
                    sent.set(i,new Email(e.getID(),e.getDataSpedizione(),e.getSender(),e.getReceivers(),e.getSubject(),e.getText(),false,e.isDeleted()));
                }else {
                    e.setToReadProperty(false);
                }
                i++;

            }
            this.sentContent.addAll(0, sent);
            if(view.getValue().equals("sent"))
                setCurrentEmails();
        }

    }
    public void setDeletedContent(ArrayList<Email> deleted){
        if(deleted.size()>0) {
            Collections.sort(deleted, Collections.reverseOrder());
            for (Email e : deleted) {
                sentContent.remove(e);
                inboxContent.remove(e);
                if(e.toReadProperty()){
                    setNewEmails();
                }
            }
            this.deletedContent.addAll(0,deleted);
            setCurrentEmails();
        }
    }

    public String getEmailAddress() {
        return emailAddress.get();
    }

    public StringProperty emailAddressProperty() {
        return emailAddress;
    }

    public ObservableList<Email> getDeletedContent() {
        return deletedContent;
    }


    public void setCurrentEmails(){
        currentEmails.clear();
        currentEmails.addAll(view.getValue().equals("incoming")? inboxContent: view.getValue().equals("sent")?sentContent: deletedContent);

    }
    public ObservableList<Email> getCurrentEmails(){
        return currentEmails;
    }
    public String getLastEmailFormattedDate(){
        return inboxContent.size()>0?inboxContent.get(0).getID().split("_")[0]:"null";
    }

    public void searchEmail(String text) {
        ArrayList<Email> emails = new ArrayList<>();
        for (Email e : (getView().equals("incoming")?inboxContent: getView().equals("sent")?sentContent:deletedContent)) {
            if (e.getSubject().toLowerCase().contains(text.toLowerCase()) || e.getText().toLowerCase().contains(text.toLowerCase())) {
                emails.add(e);
            }
        }
        currentEmails.setAll(emails);
    }
}
