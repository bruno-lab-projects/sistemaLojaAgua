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
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;

public class PrimaryController {

    // Componentes da aba Clientes
    @FXML private TextField nomeField;
    @FXML private TextField telefoneField;
    @FXML private TextField enderecoField;
    @FXML private TextField observacoesField;
    @FXML private Button salvarButton;
    @FXML private TableView<Cliente> clientesTable;
    @FXML private TableColumn<Cliente, String> colNome;
    @FXML private TableColumn<Cliente, String> colTelefone;
    @FXML private TableColumn<Cliente, String> colEndereco;
    @FXML private TableColumn<Cliente, String> colObservacoes;

    // Componentes da aba Produtos
    @FXML private TextField produtoPrecoField;
    @FXML private Button salvarPrecoButton;
    @FXML private TableView<Produto> produtosTable;
    @FXML private TableColumn<Produto, String> colProdutoNome;
    @FXML private TableColumn<Produto, Double> colProdutoPreco;

    // Componentes da aba Funcionários
    @FXML private TextField funcionarioNomeField;
    @FXML private Button salvarFuncionarioButton;
    @FXML private TableView<Funcionario> funcionariosTable;
    @FXML private TableColumn<Funcionario, String> colFuncionarioNome;

    private ObservableList<Cliente> clientesData = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        // Configura as colunas da tabela de clientes
        colNome.setCellValueFactory(cellData -> cellData.getValue().nomeProperty());
        colTelefone.setCellValueFactory(cellData -> cellData.getValue().telefoneProperty());
        colEndereco.setCellValueFactory(cellData -> cellData.getValue().enderecoProperty());
        colObservacoes.setCellValueFactory(cellData -> cellData.getValue().observacoesProperty());

        // Carrega os clientes do banco
        carregarClientes();

        // Configura as colunas da tabela de produtos
        colProdutoNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colProdutoPreco.setCellValueFactory(new PropertyValueFactory<>("preco"));

        // Adiciona listener para saber qual produto foi clicado
        produtosTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    // Quando o usuário clicar em um produto,
                    // preenche o campo de texto com o preço atual
                    produtoPrecoField.setText(String.valueOf(newSelection.getPreco()));
                }
            }
        );

        // Carrega os produtos do banco
        loadProdutosDaTabela();

        // Configura as colunas da tabela de funcionários
        colFuncionarioNome.setCellValueFactory(new PropertyValueFactory<>("nome"));

        // Carrega os funcionários do banco
        loadFuncionariosDaTabela();
    }

    private void carregarClientes() {
        clientesData.clear();
        String sql = "SELECT id, nome, telefone, endereco, observacoes FROM Clientes";

        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Cliente cliente = new Cliente(
                    rs.getInt("id"),
                    rs.getString("nome"),
                    rs.getString("telefone"),
                    rs.getString("endereco"),
                    rs.getString("observacoes")
                );
                clientesData.add(cliente);
            }

            // Atualiza a tabela com os dados
            clientesTable.setItems(clientesData);

        } catch (SQLException e) {
            System.err.println("Erro ao carregar clientes: " + e.getMessage());
        }
    }

    @FXML
    private void handleSalvarCliente() {
        // 1. Pega os valores dos campos
        String nome = nomeField.getText();
        String telefone = telefoneField.getText();
        String endereco = enderecoField.getText();
        String observacoes = observacoesField.getText();

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
        String sql = "INSERT INTO Clientes (nome, telefone, endereco, observacoes) VALUES (?, ?, ?, ?)";

        // 4 e 5. Conecta ao banco e executa o INSERT
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, nome);
            pstmt.setString(2, telefone);
            pstmt.setString(3, endereco);
            pstmt.setString(4, observacoes);
            pstmt.executeUpdate();

            // 6. Mostra mensagem de sucesso
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Sucesso");
            alert.setHeaderText(null);
            alert.setContentText("Cliente salvo com sucesso!");
            alert.showAndWait();

            // Recarrega a tabela
            carregarClientes();

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
        observacoesField.clear();
    }

    @FXML
    private void handleSalvarPreco() {
        // 1. Pegue o produto que está selecionado na tabela
        Produto produtoSelecionado = produtosTable.getSelectionModel().getSelectedItem();
        // 2. Pegue o novo preço do campo de texto
        String precoStr = produtoPrecoField.getText();

        // 3. Verificações de erro
        if (produtoSelecionado == null) {
            new Alert(Alert.AlertType.ERROR, "Por favor, selecione um produto na tabela primeiro.").show();
            return;
        }
        double novoPreco;
        try {
            novoPreco = Double.parseDouble(precoStr);
        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Preço inválido. Use apenas números (ex: 19.50).").show();
            return;
        }

        // 4. Crie o SQL de UPDATE:
        String sql = "UPDATE Produtos SET preco = ? WHERE id = ?";

        // 5. Use 'Database.connect()' e 'PreparedStatement' para salvar o novo preço.
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, novoPreco);
            pstmt.setInt(2, produtoSelecionado.getId());
            pstmt.executeUpdate();

            new Alert(Alert.AlertType.INFORMATION, "Preço atualizado com sucesso!").show();

        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Erro ao atualizar o preço: " + e.getMessage()).show();
        }

        // 6. Limpe o campo e recarregue a tabela
        produtoPrecoField.clear();
        loadProdutosDaTabela();
    }

    private void loadProdutosDaTabela() {
        // Este método é quase IDÊNTICO ao 'carregarClientes()'.

        ObservableList<Produto> listaProdutos = FXCollections.observableArrayList();
        String sql = "SELECT * FROM Produtos ORDER BY nome";

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
            new Alert(Alert.AlertType.ERROR, "O nome é obrigatório.").show();
            return;
        }

        String sql = "INSERT INTO Funcionarios (nome) VALUES (?)";
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nome);
            pstmt.executeUpdate();
            new Alert(Alert.AlertType.INFORMATION, "Funcionário salvo!").show();

        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Erro ao salvar: " + e.getMessage()).show();
        }

        funcionarioNomeField.clear();
        loadFuncionariosDaTabela();
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
    private void switchToSecondary() throws IOException {
        App.setRoot("secondary");
    }
}
