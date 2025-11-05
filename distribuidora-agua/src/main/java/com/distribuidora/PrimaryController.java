package com.distribuidora;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class PrimaryController {

    @FXML private TextField nomeField;
    @FXML private TextField telefoneField;
    @FXML private TextField enderecoField;
    @FXML private Button salvarButton;
    @FXML private TableView clientesTable;

    @FXML
    private void handleSalvarCliente() {
        // 1. Pega os valores dos campos
        String nome = nomeField.getText();
        String telefone = telefoneField.getText();
        String endereco = enderecoField.getText();

        // 2. Verifica se o nome está vazio
        if (nome == null || nome.isBlank()) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText(null);
            alert.setContentText("O nome é obrigatório.");
            alert.showAndWait();
            return;
        }

        // 3. Define o SQL
        String sql = "INSERT INTO Clientes (nome, telefone, endereco) VALUES (?, ?, ?)";

        // 4 e 5. Conecta ao banco e executa o INSERT
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, nome);
            pstmt.setString(2, telefone);
            pstmt.setString(3, endereco);
            pstmt.executeUpdate();

            // 6. Mostra mensagem de sucesso
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Sucesso");
            alert.setHeaderText(null);
            alert.setContentText("Cliente salvo com sucesso!");
            alert.showAndWait();

        } catch (SQLException e) {
            System.err.println("Erro ao salvar cliente: " + e.getMessage());
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText(null);
            alert.setContentText("Erro ao salvar cliente: " + e.getMessage());
            alert.showAndWait();
        }

        // 7. Limpa os campos
        nomeField.clear();
        telefoneField.clear();
        enderecoField.clear();
    }

    @FXML
    private void switchToSecondary() throws IOException {
        App.setRoot("secondary");
    }
}
