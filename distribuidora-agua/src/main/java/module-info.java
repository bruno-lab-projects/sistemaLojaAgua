module com.distribuidora {
    requires transitive javafx.controls;
    requires transitive javafx.graphics;
    requires transitive javafx.base;
    requires transitive java.sql;
    requires java.logging;
    requires java.desktop;
    requires javafx.fxml;
    // HttpClient (baixar version.txt e o JAR novo) e Preferences ("lembrar
    // depois"). Ambos são do próprio JDK, mas num projeto modularizado
    // precisam ser declarados: `mvn javafx:run` roda no module path.
    requires java.net.http;
    requires java.prefs;

    opens com.distribuidora to javafx.fxml, org.junit.platform.commons;
    opens com.distribuidora.util to org.junit.platform.commons;
    opens com.distribuidora.update to org.junit.platform.commons;
    exports com.distribuidora;
    exports com.distribuidora.util;
    exports com.distribuidora.update;
}
