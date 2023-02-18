package com.example.clientprova;

import javafx.fxml.FXML;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import model.ServerLog;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class ServerController {

    @FXML
    public TextFlow textFlow;
    ServerLog serverLog;

    public void setMainController(ServerLog serverLog){
        this.serverLog=serverLog;
        serverLog.getLastMessage().addListener(((observableValue, oldV, newV) ->{
            addLog(newV,"");
        }));
    }

    public void addLog(String msg, String type){
        Text log= new Text(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) +" | " + type + "  " + msg+"\n");
        if(type=="ERROR"){
            log.setFill(Color.RED);
        }else if(type=="WARN"){
            log.setFill(Color.YELLOW);
        }
        textFlow.getChildren().add(log);

    }


}
