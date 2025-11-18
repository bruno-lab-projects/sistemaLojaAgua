package com.distribuidora;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class SecondaryController {

    // Injeções FXML para Produtos
    @FXML private TextField produtoNomeField;
    @FXML private TextField produtoPrecoField;
    @FXML private Button salvarProdutoButton;
    @FXML private Button excluirProdutoButton;
    @FXML private Button limparProdutoButton;
    @FXML private TableView<Produto> produtosTable;
    @FXML private TableColumn<Produto, String> colProdutoNome;
    @FXML private TableColumn<Produto, Double> colProdutoPreco;

    // Injeções FXML para Funcionários
    @FXML private TextField funcionarioNomeField;
    @FXML private Button salvarFuncionarioButton;
    @FXML private Button excluirFuncionarioButton;
    @FXML private Button limparFuncionarioButton;
    @FXML private TableView<Funcionario> funcionariosTable;
    @FXML private TableColumn<Funcionario, String> colFuncionarioNome;

    private Funcionario funcionarioSelecionado = null;
    private Produto produtoSelecionado = null;

    @FXML
    private void initialize() {
        // Configura as colunas da tabela de produtos
        colProdutoNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colProdutoPreco.setCellValueFactory(new PropertyValueFactory<>("preco"));

        // Adiciona listener para selecionar produto
        produtosTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> {
                if (newVal != null) {
                    produtoSelecionado = newVal;
                    produtoNomeField.setText(newVal.getNome());
                    produtoPrecoField.setText(String.valueOf(newVal.getPreco()));
                }
            }
        );

        // Carrega os produtos do banco
        loadProdutosDaTabela();

        // Configura as colunas da tabela de funcionários
        colFuncionarioNome.setCellValueFactory(new PropertyValueFactory<>("nome"));

        // Adiciona listener para selecionar funcionário
        funcionariosTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> {
                if (newVal != null) {
                    funcionarioSelecionado = newVal;
                    funcionarioNomeField.setText(newVal.getNome());
                }
            }
        );

        // Carrega os funcionários do banco
        loadFuncionariosDaTabela();
    }

    @FXML
    private void handleSalvarProduto() {
        String nome = produtoNomeField.getText();
        String precoStr = produtoPrecoField.getText();

        // Validações
        if (nome.isBlank()) {
            new Alert(AlertType.ERROR, "O nome é obrigatório.").show();
            return;
        }

        double preco;
        try {
            preco = Double.parseDouble(precoStr);
        } catch (NumberFormatException e) {
            new Alert(AlertType.ERROR, "Preço inválido. Use apenas números (ex: 19.50).").show();
            return;
        }

        if (produtoSelecionado == null) {
            // MODO 1: CRIAR NOVO (INSERT)
            String sql = "INSERT INTO Produtos (nome, preco) VALUES (?, ?)";
            try (Connection conn = Database.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, nome);
                pstmt.setDouble(2, preco);
                pstmt.executeUpdate();
                new Alert(AlertType.INFORMATION, "Produto salvo com sucesso!").show();
            } catch (SQLException e) {
                new Alert(AlertType.ERROR, "Erro ao salvar: " + e.getMessage()).show();
            }

        } else {
            // MODO 2: ATUALIZAR EXISTENTE (UPDATE)
            String sql = "UPDATE Produtos SET nome = ?, preco = ? WHERE id = ?";
            try (Connection conn = Database.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, nome);
                pstmt.setDouble(2, preco);
                pstmt.setInt(3, produtoSelecionado.getId());
                pstmt.executeUpdate();
                new Alert(AlertType.INFORMATION, "Produto atualizado com sucesso!").show();
            } catch (SQLException e) {
                new Alert(AlertType.ERROR, "Erro ao atualizar: " + e.getMessage()).show();
            }
        }

        // No final, recarregue a tabela e limpe o formulário
        loadProdutosDaTabela();
        handleLimparProduto();
    }

    @FXML
    private void handleLimparProduto() {
        produtoSelecionado = null;
        produtosTable.getSelectionModel().clearSelection();
        produtoNomeField.clear();
        produtoPrecoField.clear();
    }

    @FXML
    private void handleExcluirProduto() {
        if (produtoSelecionado == null) {
            new Alert(AlertType.ERROR, "Selecione um produto na tabela para excluir.").show();
            return;
        }

        // Crie um Alerta de Confirmação
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Exclusão");
        alert.setHeaderText("Excluir " + produtoSelecionado.getNome() + "?");
        alert.setContentText("Tem certeza? Esta ação não pode ser desfeita.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Usuário confirmou
                String sql = "DELETE FROM Produtos WHERE id = ?";
                try (Connection conn = Database.connect();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, produtoSelecionado.getId());
                    pstmt.executeUpdate();
                    new Alert(AlertType.INFORMATION, "Produto excluído.").show();
                } catch (SQLException e) {
                    new Alert(AlertType.ERROR, "Erro ao excluir: " + e.getMessage()).show();
                }

                // Recarregue e limpe
                loadProdutosDaTabela();
                handleLimparProduto();
            }
        });
    }

    private void loadProdutosDaTabela() {
        ObservableList<Produto> listaProdutos = FXCollections.observableArrayList();
        String sql = "SELECT * FROM Produtos ORDER BY preco ASC";

        try (Connection conn = Database.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                listaProdutos.add(new Produto(
                    rs.getInt("id"),
                    rs.getString("nome"),
                    rs.getDouble("preco")
                ));
            }
            produtosTable.setItems(listaProdutos);

        } catch (SQLException e) {
            System.out.println("Erro ao carregar produtos: " + e.getMessage());
        }
    }

    @FXML
    private void handleSalvarFuncionario() {
        String nome = funcionarioNomeField.getText();
        if (nome.isBlank()) {
            new Alert(AlertType.ERROR, "O nome é obrigatório.").show();
            return;
        }

        if (funcionarioSelecionado == null) {
            // MODO 1: CRIAR NOVO (INSERT)
            String sql = "INSERT INTO Funcionarios (nome) VALUES (?)";
            try (Connection conn = Database.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, nome);
                pstmt.executeUpdate();
                new Alert(AlertType.INFORMATION, "Funcionário salvo com sucesso!").show();
            } catch (SQLException e) {
                new Alert(AlertType.ERROR, "Erro ao salvar: " + e.getMessage()).show();
            }

        } else {
            // MODO 2: ATUALIZAR EXISTENTE (UPDATE)
            String sql = "UPDATE Funcionarios SET nome = ? WHERE id = ?";
            try (Connection conn = Database.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, nome);
                pstmt.setInt(2, funcionarioSelecionado.getId());
                pstmt.executeUpdate();
                new Alert(AlertType.INFORMATION, "Funcionário atualizado com sucesso!").show();
            } catch (SQLException e) {
                new Alert(AlertType.ERROR, "Erro ao atualizar: " + e.getMessage()).show();
            }
        }

        // No final, recarregue a tabela e limpe o formulário
        loadFuncionariosDaTabela();
        handleLimparFuncionario();
    }

    @FXML
    private void handleLimparFuncionario() {
        funcionarioSelecionado = null;
        funcionariosTable.getSelectionModel().clearSelection();
        funcionarioNomeField.clear();
    }

    @FXML
    private void handleExcluirFuncionario() {
        if (funcionarioSelecionado == null) {
            new Alert(AlertType.ERROR, "Selecione um funcionário na tabela para excluir.").show();
            return;
        }

        // Crie um Alerta de Confirmação
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Exclusão");
        alert.setHeaderText("Excluir " + funcionarioSelecionado.getNome() + "?");
        alert.setContentText("Tem certeza? Esta ação não pode ser desfeita.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Usuário confirmou
                String sql = "DELETE FROM Funcionarios WHERE id = ?";
                try (Connection conn = Database.connect();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, funcionarioSelecionado.getId());
                    pstmt.executeUpdate();
                    new Alert(AlertType.INFORMATION, "Funcionário excluído.").show();
                } catch (SQLException e) {
                    new Alert(AlertType.ERROR, "Erro ao excluir: " + e.getMessage()).show();
                }

                // Recarregue e limpe
                loadFuncionariosDaTabela();
                handleLimparFuncionario();
            }
        });
    }

    private void loadFuncionariosDaTabela() {
        ObservableList<Funcionario> lista = FXCollections.observableArrayList();
        String sql = "SELECT * FROM Funcionarios ORDER BY nome";
        try (Connection conn = Database.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(new Funcionario(rs.getInt("id"), rs.getString("nome")));
            }
            funcionariosTable.setItems(lista);
        } catch (SQLException e) {
            System.out.println("Erro ao carregar funcionários: " + e.getMessage());
        }
    }

    @FXML
    private void switchToPrimary() throws IOException {
        App.setRoot("primary");
    }
}