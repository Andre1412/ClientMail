package model;

import javafx.beans.property.SimpleStringProperty;

public class ServerLog {
    public SimpleStringProperty lastMessage;

    public ServerLog(){
        lastMessage = new SimpleStringProperty("");
    }

    public void setLastMessage(String msg){
        lastMessage.setValue(msg);
    }

    public SimpleStringProperty getLastMessage(){
        return lastMessage;
    }

}
