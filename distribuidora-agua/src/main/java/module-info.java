module com.distribuidora {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.distribuidora to javafx.fxml;
    exports com.distribuidora;
}
