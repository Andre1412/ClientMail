module com.example.clientprova {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;


    opens com.example.clientprova to javafx.fxml;
    opens model to com.google.gson;
    exports com.example.clientprova;




}