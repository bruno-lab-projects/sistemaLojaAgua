module com.distribuidora {
    requires transitive javafx.controls;
    requires transitive javafx.graphics;
    requires transitive javafx.base;
    requires transitive java.sql;
    requires javafx.fxml;

    opens com.distribuidora to javafx.fxml, org.junit.platform.commons;
    opens com.distribuidora.util to org.junit.platform.commons;
    exports com.distribuidora;
    exports com.distribuidora.util;
}
