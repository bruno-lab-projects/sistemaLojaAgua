package com.distribuidora;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.ComboBox;
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

    // Componentes da aba Pedidos
    @FXML private ComboBox<Cliente> pedidoClienteCombo;
    @FXML private ComboBox<Produto> pedidoProdutoCombo;
    @FXML private ComboBox<Funcionario> pedidoFuncionarioCombo;
    @FXML private TextField pedidoQtdField;
    @FXML private Button criarPedidoButton;
    @FXML private TableView<Pedido> pedidosTable;
    @FXML private TableColumn<Pedido, String> colPedidoCliente;
    @FXML private TableColumn<Pedido, String> colPedidoProduto;
    @FXML private TableColumn<Pedido, Integer> colPedidoQtd;
    @FXML private TableColumn<Pedido, String> colPedidoFuncionario;
    @FXML private TableColumn<Pedido, String> colPedidoStatus;
    @FXML private TableColumn<Pedido, String> colPedidoData;
    @FXML private TableColumn<Pedido, Double> colPedidoTotal;

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

        // Popula os ComboBoxes da aba Pedidos com as listas das outras tabelas
        pedidoClienteCombo.setItems(clientesTable.getItems());
        pedidoProdutoCombo.setItems(produtosTable.getItems());
        pedidoFuncionarioCombo.setItems(funcionariosTable.getItems());

        // Configura as colunas da tabela de pedidos
        colPedidoCliente.setCellValueFactory(new PropertyValueFactory<>("clienteNome"));
        colPedidoProduto.setCellValueFactory(new PropertyValueFactory<>("produtoNome"));
        colPedidoQtd.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
        colPedidoFuncionario.setCellValueFactory(new PropertyValueFactory<>("funcionarioNome"));
        colPedidoStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colPedidoData.setCellValueFactory(new PropertyValueFactory<>("dataHora"));
        colPedidoTotal.setCellValueFactory(new PropertyValueFactory<>("precoTotal"));

        // Carrega os pedidos do banco
        loadPedidosDaTabela();
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
    private void handleCriarPedido() {
        // 1. Pegue os objetos selecionados nos ComboBoxes
        Cliente cliente = pedidoClienteCombo.getValue();
        Produto produto = pedidoProdutoCombo.getValue();
        Funcionario func = pedidoFuncionarioCombo.getValue();
        String qtdStr = pedidoQtdField.getText();

        // 2. Valide os dados
        if (cliente == null) {
            new Alert(AlertType.ERROR, "Por favor, selecione um cliente.").show();
            return;
        }
        if (produto == null) {
            new Alert(AlertType.ERROR, "Por favor, selecione um produto.").show();
            return;
        }
        if (func == null) {
            new Alert(AlertType.ERROR, "Por favor, selecione um funcionário.").show();
            return;
        }

        int quantidade;
        try {
            quantidade = Integer.parseInt(qtdStr);
            if (quantidade <= 0) {
                new Alert(AlertType.ERROR, "Quantidade deve ser maior que zero.").show();
                return;
            }
        } catch (NumberFormatException e) {
            new Alert(AlertType.ERROR, "Quantidade inválida. Use apenas números inteiros.").show();
            return;
        }

        // 3. Prepare os dados para salvar
        String dataAgora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String statusInicial = "Feito";

        String sql = "INSERT INTO Pedidos (cliente_id, funcionario_id, produto_id, status, data_hora, quantidade) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        // 4. Salve no banco
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, cliente.getId());
            pstmt.setInt(2, func.getId());
            pstmt.setInt(3, produto.getId());
            pstmt.setString(4, statusInicial);
            pstmt.setString(5, dataAgora);
            pstmt.setInt(6, quantidade);
            pstmt.executeUpdate();

            // 5. Mostre mensagem de sucesso
            new Alert(AlertType.INFORMATION, "Pedido criado com sucesso!").show();

            // Limpe os campos
            pedidoClienteCombo.setValue(null);
            pedidoProdutoCombo.setValue(null);
            pedidoFuncionarioCombo.setValue(null);
            pedidoQtdField.clear();

        } catch (SQLException e) {
            new Alert(AlertType.ERROR, "Erro ao criar pedido: " + e.getMessage()).show();
        }

        // 6. Recarregue a tabela de pedidos
        loadPedidosDaTabela();
    }

    private void loadPedidosDaTabela() {
        // SQL com JOINs para pegar os nomes de todas as tabelas relacionadas
        String sql = "SELECT p.id, c.nome as cliente, pr.nome as produto, p.quantidade, " +
                     "f.nome as funcionario, p.status, p.data_hora, (pr.preco * p.quantidade) as total " +
                     "FROM Pedidos p " +
                     "JOIN Clientes c ON p.cliente_id = c.id " +
                     "JOIN Produtos pr ON p.produto_id = pr.id " +
                     "JOIN Funcionarios f ON p.funcionario_id = f.id " +
                     "ORDER BY p.data_hora DESC";

        ObservableList<Pedido> listaPedidos = FXCollections.observableArrayList();

        try (Connection conn = Database.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Pedido pedido = new Pedido(
                    rs.getInt("id"),
                    rs.getString("cliente"),
                    rs.getString("produto"),
                    rs.getInt("quantidade"),
                    rs.getString("funcionario"),
                    rs.getString("status"),
                    rs.getString("data_hora"),
                    rs.getDouble("total")
                );
                listaPedidos.add(pedido);
            }

            pedidosTable.setItems(listaPedidos);

        } catch (SQLException e) {
            System.out.println("Erro ao carregar pedidos: " + e.getMessage());
        }
    }

    @FXML
    private void switchToSecondary() throws IOException {
        App.setRoot("secondary");
    }
}
