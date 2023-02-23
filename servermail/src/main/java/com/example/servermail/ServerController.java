package com.example.servermail;

import javafx.fxml.FXML;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import model.ServerLog;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ServerController {

    @FXML
    public TextFlow textFlow;
    private ServerLog serverLog;

    public void setMainController(ServerLog serverLog){
        this.serverLog=serverLog;
        serverLog.getLastMessage().addListener(((observableValue, oldV, newV) ->{
            addLog(newV);
        }));
    }

    public void addLog(String msg){
        Text log= new Text(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) +"\t| " + "  " + msg+"\n");
        if(msg.contains("ERROR")){
            log.setFill(Color.web("#cf3434"));
        }else{
            log.setFill(Color.WHITE);
        }
        textFlow.getChildren().add(log);

    }


}
