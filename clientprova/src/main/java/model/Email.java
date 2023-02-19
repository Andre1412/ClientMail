package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Email implements Serializable, Comparable<Email>{
    private String ID;
    private String dataSpedizione;
    private String sender;
    private ArrayList<String> receivers;
    private String text;
    private String subject;
    private boolean toRead;

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
    public Email(String ID, String dataSpedizione, String sender, List<String> receivers, String subject, String text, boolean toRead) {
        this.ID=ID;
        this.dataSpedizione=dataSpedizione;
        this.sender = sender;
        this.subject = subject;
        this.text = text;
        this.toRead=toRead;
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

    public boolean isToRead() {
        return toRead;
    }

    public void setToRead(boolean toRead){
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
        return this.sender+ "        "+
                String.join("  -  ", List.of(this.subject,  this.text.length()>60?this.text.replace("\n","")
                        .substring(0, 30) + "...": this.text.replace("\n","")));
    }
}

