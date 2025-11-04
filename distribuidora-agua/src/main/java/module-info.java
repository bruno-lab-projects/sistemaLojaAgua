module com.distribuidora {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.distribuidora to javafx.fxml;
    exports com.distribuidora;
}
