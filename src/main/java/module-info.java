module com.example.web_browser {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    opens com.example.web_browser to javafx.fxml;
    exports com.example.web_browser;
}