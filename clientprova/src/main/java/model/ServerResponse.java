package model;

public class ServerResponse{
    private String msg;
    private String status;

    public ServerResponse(String status, String msg){
        this.msg=msg;
        this.status=status;
    }

    public String getMsg(){
        return msg;
    }
    public String getStatus(){
        return status;
    }
}