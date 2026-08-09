package com.distribuidora;

import com.distribuidora.util.AlertUtils;
import com.distribuidora.util.AppLogger;
import com.distribuidora.util.StartupErrorDialog;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) {
        try {
            // Carrega o ícone personalizado
            Image icon = new Image(getClass().getResourceAsStream("/images/icon.png"));
            stage.getIcons().add(icon);
            
            scene = new Scene(loadFXML("primary"), 640, 480);
            stage.setScene(scene);
            stage.setMinWidth(1000);
            stage.setMinHeight(650);
            stage.setMaximized(true);
            stage.setTitle("Distribuidora de Agua");
            stage.show();
        } catch (IOException e) {
            AlertUtils.mostrarErro(
                "Erro Crítico",
                "Não foi possível carregar a interface do sistema.\n\nDetalhes: " + e.getMessage()
            );
            System.exit(1);
        }
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        AppLogger.initialize(Database.getDatabaseDirectory().resolve("logs"));

        try {
            Database.initialize();
        } catch (SQLException | RuntimeException e) {
            AppLogger.get(App.class).log(Level.SEVERE, "Falha ao inicializar o banco de dados", e);

            StartupErrorDialog.mostrar(
                "Não foi possível iniciar o sistema",
                "Ocorreu um erro ao abrir o banco de dados e o programa será encerrado.\n\n"
                    + "Tente fechar outras janelas do sistema e abrir novamente.\n"
                    + "Se o erro continuar, envie o arquivo de log ao suporte:\n\n"
                    + Database.getDatabaseDirectory().resolve("logs"));

            System.exit(1);
        }

        launch();
    }

}