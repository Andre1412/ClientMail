module com.example.servermail {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;


    opens com.example.servermail to javafx.fxml;
    opens model to com.google.gson;
    exports com.example.servermail;
}