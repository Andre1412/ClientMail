package model;

import javafx.beans.property.SimpleBooleanProperty;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Email implements Serializable, Comparable<Email>{
    private String ID;
    private String dataSpedizione;
    private String sender;
    private ArrayList<String> receivers;
    private String text;
    private String subject;
    private boolean toRead;
    private boolean deleted;

    /**
     * @return stringa composta dagli indirizzi e-mail del mittente più destinatari
     */


    /**
     * Costruttore della classe.
     *
     * @param sender     email del mittente
     * @param receivers  emails dei destinatari
     * @param subject    oggetto della mail
     * @param text       testo della mail
     **/
    public Email(String ID, String dataSpedizione, String sender, List<String> receivers, String subject, String text, boolean toRead, boolean deleted) {
        this.ID=ID;
        this.dataSpedizione=dataSpedizione;
        this.sender = sender;
        this.subject = subject;
        this.text = text;
        this.toRead=toRead;
        this.deleted=deleted;
        if(receivers.size()==1 && receivers.contains("")) {
            this.receivers = new ArrayList<>();
        }
        else{ this.receivers = new ArrayList<>(receivers);}

    }
    public int compareTo(Email mail){
        return this.ID.compareTo(mail.getID());
    }
    public void setID(String ID) {
        this.ID = ID;
    }

    public String getID() {
        return ID;
    }

    public void setDeleted(boolean deleted) {
        this.deleted=deleted;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public boolean deletedProperty() {
        return deleted;
    }

    public boolean toReadProperty() {
        return toRead;
    }

    public void setToReadProperty(boolean toRead){
        this.toRead = toRead;
    }

    public void setDataSpedizione(String dataSpedizione) {
        this.dataSpedizione = dataSpedizione;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setReceivers(ArrayList<String> receivers) {
        this.receivers = receivers;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSender() {
        return sender;
    }

    public ArrayList<String> getReceivers() {
        return receivers;
    }

    public String getSubject() {
        return subject;
    }

    public String getText() {
        return text;
    }

    public String getDataSpedizione() {
        return dataSpedizione;
    }

    /**
     * @return      stringa composta dagli indirizzi e-mail del mittente più destinatari
     */
    @Override
    public String toString() {
        return this.sender+ " "+
                String.join("  -  ", List.of(this.subject,  this.text.length()>10?this.text.replace("\n","")
                        .substring(0, 10) + "...": this.text.replace("\n","")));
    }

    @Override
    public boolean equals(Object obj) {
        if(obj!=null && obj.getClass()==this.getClass()){
            Email mail= (Email)obj;
            return mail.getID().equals(this.ID);
        }
        return false;
    }

}

