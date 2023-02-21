package com.example.clientprova;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import model.Client;
import model.Email;

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
    @FXML
    public TextField searchTxt;

    SimpleBooleanProperty server_status;

    MainController mainController;
    ClientController clientController;

    private Client model;
    private Email selectedEmail;
    Stage stage;

    private int cellHeight=70;


    public void setMainController(MainController m, Client model, ClientController clientController, Stage stage){
        this.mainController = m;
        this.model = model;
        this.clientController = clientController;
        this.stage=stage;
        selectedEmail = null;

        model.getCurrentEmails().addListener((ListChangeListener<Email>)(value)->{
            listEmail.itemsProperty().setValue((ObservableList<Email>) value.getList());
        });

        model.getViewProperty().addListener(((observableValue, oldV, newV) -> changeView(newV) ));
        listEmail.setCellFactory((listView)->new EmailCell(model, listEmail,stage));
        listEmail.setOnMouseClicked(this::showSelectedEmail);
        username.textProperty().bind(model.emailAddressProperty());
        PaneListEmail.getItems().remove(borderTextEmail);
        //setting status of server
        server_status=new SimpleBooleanProperty(false);
        server_status.bind(clientController.serverStatusProperty());
        server_status.addListener((observable, oldValue, newValue) -> {
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
            if (selectedEmail == null || !PaneListEmail.getItems().contains(borderTextEmail)) {
                PaneListEmail.getItems().add(borderTextEmail);
            }
            Email email = listEmail.getSelectionModel().getSelectedItem();
            if(email.toReadProperty()){
                email.setToReadProperty(false);
                model.setCurrentEmails();
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
            lblTo.setText(( !email.getReceivers().isEmpty()?"A: ":"") + String.join(", ", email.getReceivers()));
            lblSubject.setText(email.getSubject());
            lblData.setText(email.getDataSpedizione());
            txtEmail.setText(email.getText());
        }
    }
    public void closePanel(){
        PaneListEmail.getItems().remove(borderTextEmail);
    }

    public void changeView(String newVue){
        if(newVue=="incoming" || newVue=="sent"){
            if(!PaneListEmail.getItems().contains(borderListEmail)){
                PaneListEmail.getItems().add(borderListEmail);
            }
            Platform.runLater(()-> model.setCurrentEmails());
        }
        if(PaneListEmail.getItems().contains(borderTextEmail)) PaneListEmail.getItems().remove(borderTextEmail);
    }


    @FXML
    protected void onDeleteButtonClick() {
        clientController.deleteEmail(selectedEmail.getID(),response->{

            if(response.getStatus()=="ERROR"){
                Platform.runLater(()->new AlertController("Qualcosa è andato storto",response.getMsg(),"ERROR", mainController.writeEmail, ()->null));
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
        alertWriting("forward");
    }

    @FXML
    public void onReplyButton() {
        alertWriting("reply");
    }

    @FXML
    public void replyAllEmail() {
        alertWriting("replyAll");
    }

    public void alertWriting(String action){
      if(model.isWriting()){
          mainController.writeEmail("",null);
          mainController.loadAlert("C'è una bozza in attesa","Vuoi sovrascriverla?","ALERT", action);
      }else{
          mainController.writeEmail(action,selectedEmail);
      }
    }

    public void searchEmail(ActionEvent actionEvent) {
        if(searchTxt.getText().isEmpty()){
            model.setCurrentEmails();
        }else{
            model.searchEmail(searchTxt.getText());
        }
    }
}
