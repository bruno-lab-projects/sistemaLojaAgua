module com.distribuidora {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.distribuidora to javafx.fxml, org.junit.platform.commons;
    opens com.distribuidora.util to org.junit.platform.commons;
    exports com.distribuidora;
    exports com.distribuidora.util;
}
