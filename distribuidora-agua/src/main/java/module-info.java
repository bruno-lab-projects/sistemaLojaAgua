module com.distribuidora {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
    requires java.sql;

    opens com.distribuidora to javafx.fxml;
    exports com.distribuidora;
}
