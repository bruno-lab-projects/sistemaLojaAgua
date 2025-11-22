package com.distribuidora;

import com.distribuidora.util.AlertUtils;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

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
        try {
            Database.initialize();
        } catch (SQLException e) {
            System.err.println("ERRO CRÍTICO: Falha ao inicializar o banco de dados.");
            System.err.println("Detalhes: " + e.getMessage());
            System.err.println("\nO aplicativo será encerrado.");
            e.printStackTrace();
            System.exit(1);
        }
        
        launch();
    }

}