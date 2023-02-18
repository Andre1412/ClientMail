package com.example.clientprova;

public class ServerResponse{
    private String msg;
    private String status;

    public ServerResponse(String status, String msg){
        this.msg=msg;
        this.status=status;
    }

    public void setMsg(String msg){
        this.msg=msg;
    }
    public void setStatus(String st){
        this.status=st;
    }
    public String getMsg(){
        return msg;
    }
    public String getStatus(){
        return status;
    }
}