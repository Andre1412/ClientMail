package com.example.clientprova;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import model.Client;
import model.Email;

import java.util.ArrayList;

public class ReadEmailController {
    @FXML
    public VBox borderListEmail;
    @FXML
    public SplitPane PaneListEmail;
    @FXML
    public Label username;
    @FXML
    public ListView<Email> listEmail;
    @FXML
    public BorderPane borderTextEmail;
    @FXML
    public Label lblFrom;
    @FXML
    public Label lblTo;
    @FXML
    public Label lblSubject;
    @FXML
    public Label lblData;
    @FXML
    public TextArea txtEmail;
    @FXML
    public Button btnInoltra;
    @FXML
    public Circle status;

    SimpleBooleanProperty server_status;

    MainController mainController;
    ClientController clientController;

    private Client model;
    private Email selectedEmail;

    private int cellHeight=70;


    public void setMainController(MainController m, Client model, ClientController clientController){
        this.mainController = m;
        this.model = model;
        this.clientController = clientController;

        selectedEmail = null;
        //TODO: risolvere binding

        model.getCurrentEmails().addListener((ListChangeListener<Email>)(value)->{
            System.out.println("Listener----------------" + value.getList());
            listEmail.itemsProperty().setValue((ObservableList<Email>) value.getList());
            /*if(value.getList().size()>listEmail.getItems().size()){
                int i=0;
                for(Email e:value.getList()){
                    if(e.isToRead()){
                        listEmail.getItems().get(i).setID("toRead");
                    }
                    System.out.println("listener:-----------"+listEmail.getItems().get(i).getID() );
                    i++;
                }

            }*/
            System.out.println(listEmail.itemsProperty().toString());
        });

        //listEmail.itemsProperty().bind(model.getInboxContent());
        listEmail.setOnMouseClicked(this::showSelectedEmail);
        username.textProperty().bind(model.emailAddressProperty());
        PaneListEmail.getItems().remove(borderTextEmail);
        //setting status of server
        server_status=new SimpleBooleanProperty(false);
        server_status.bind(clientController.serverStatusProperty());
        server_status.addListener((observable, oldValue, newValue) -> {
            System.out.println(oldValue.toString() + newValue.toString());
            if (newValue.booleanValue())
                status.setFill(Color.LIME);
            else
                status.setFill(Color.RED);
        });
    }
    public Email getSelectedEmail(){
        return selectedEmail;
    }

    protected void showSelectedEmail(MouseEvent mouseEvent) {
        if(listEmail.getSelectionModel().getSelectedItem()!=null) {
            if (selectedEmail == null) {
                PaneListEmail.getItems().add(borderTextEmail);
            }
            Email email = listEmail.getSelectionModel().getSelectedItem();
            if(email.isToRead()){
                email.setToRead(false);
                clientController.setToRead(email);
            }
            model.setNewEmails();
            selectedEmail = email;
            updateDetailView(email);
        }
    }

    protected void updateDetailView(Email email) {
        if(email != null) {
            lblFrom.setText(email.getSender());
            System.out.println(String.join(", ", email.getReceivers()));
            lblTo.setText(( !email.getReceivers().isEmpty()?"A: ":"") + String.join(", ", email.getReceivers()));
            lblSubject.setText(email.getSubject());
            lblData.setText(email.getDataSpedizione());
            txtEmail.setText(email.getText());
        }
    }

    public void changeView(String newVue){
        if(newVue=="incoming" && model.getView()!="incoming"){
            if(!PaneListEmail.getItems().contains(borderListEmail)){
                PaneListEmail.getItems().add(borderListEmail);
            }
        }else if(newVue=="sent" && model.getView()!="sent"){
            if(!PaneListEmail.getItems().contains(borderListEmail)){
                PaneListEmail.getItems().add(borderListEmail);
            }
        }
        Platform.runLater(()-> {
                    model.setCurrentEmails();
                });
        model.setView(newVue);
        selectedEmail=null;
        PaneListEmail.getItems().remove(borderTextEmail);
    }


    @FXML
    protected void onDeleteButtonClick() {
        clientController.deleteEmail(selectedEmail.getID(),response->{

            if(response.getStatus()=="ERROR"){
                Platform.runLater(()->mainController.loadAlert("Qualcosa è andato storto",response.getMsg(),"ERROR","" ));
            }else {
                Platform.runLater(()-> {
                    if(model.getView()=="incoming")
                        model.removeInboxContent(selectedEmail);
                    else
                        model.removeSentContent(selectedEmail);

                    selectedEmail = null;
                    PaneListEmail.getItems().remove(borderTextEmail);
                });
            }
        });

    }

    @FXML
    protected void forwardEmail(){
        mainController.showWriteEmail();
        alertWriting("forward");
    }

    @FXML
    public void onReplyButton() {
        mainController.showWriteEmail();
        alertWriting("reply");

    }

    @FXML
    public void replyAllEmail() {
        mainController.showWriteEmail();
        alertWriting("replyAll");

    }

    public void alertWriting(String action){
      if(model.isWriting()){
          mainController.loadAlert("C'è una bozza in attesa","Vuoi sovrascriverla?","ALERT", action);
      }else{
          mainController.writeEmail(action,selectedEmail);
          model.setWriting(true);
      }
    }

}
