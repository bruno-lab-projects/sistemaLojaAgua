package com.distribuidora;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
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
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TabPane;
import javafx.scene.control.cell.PropertyValueFactory;

public class PrimaryController {

    // Componentes da aba Clientes
    @FXML private TextField nomeField;
    @FXML private TextField telefoneField;
    @FXML private TextField enderecoField;
    @FXML private TextField observacoesField;
    @FXML private Button salvarButton;
    @FXML private Button excluirClienteButton;
    @FXML private Button limparClienteButton;
    @FXML private TableView<Cliente> clientesTable;
    @FXML private TableColumn<Cliente, String> colNome;
    @FXML private TableColumn<Cliente, String> colTelefone;
    @FXML private TableColumn<Cliente, String> colEndereco;
    @FXML private TableColumn<Cliente, String> colObservacoes;

    // Componentes da aba Produtos
    @FXML private TextField produtoNomeField;
    @FXML private TextField produtoPrecoField;
    @FXML private Button salvarProdutoButton;
    @FXML private Button excluirProdutoButton;
    @FXML private Button limparProdutoButton;
    @FXML private TableView<Produto> produtosTable;
    @FXML private TableColumn<Produto, String> colProdutoNome;
    @FXML private TableColumn<Produto, Double> colProdutoPreco;

    // Componentes da aba Funcionários
    @FXML private TextField funcionarioNomeField;
    @FXML private Button salvarFuncionarioButton;
    @FXML private Button excluirFuncionarioButton;
    @FXML private Button limparFuncionarioButton;
    @FXML private TableView<Funcionario> funcionariosTable;
    @FXML private TableColumn<Funcionario, String> colFuncionarioNome;

    // Componentes da aba Pedidos
    @FXML private ComboBox<Cliente> pedidoClienteCombo;
    @FXML private ComboBox<Produto> pedidoProdutoCombo;
    @FXML private Spinner<Integer> pedidoQtdSpinner;
    @FXML private TextField pedidoNomeAvulsoField;
    @FXML private TextField pedidoEnderecoAvulsoField;
    @FXML private Button criarPedidoButton;
    @FXML private DatePicker pedidoDatePicker;
    @FXML private TabPane statusTabPane;

    // Botões de Ação
    @FXML private Button marcarSaiuButton;
    @FXML private Button marcarEntregueButton;
    @FXML private Button cancelarPedidoButton;

    // Tabela 1: Pedidos Feitos
    @FXML private TableView<Pedido> tablePedidosFeitos;
    @FXML private TableColumn<Pedido, String> colFeitosCliente;
    @FXML private TableColumn<Pedido, String> colFeitosEndereco;
    @FXML private TableColumn<Pedido, String> colFeitosProduto;
    @FXML private TableColumn<Pedido, Integer> colFeitosQtd;
    @FXML private TableColumn<Pedido, String> colFeitosHora;
    @FXML private TableColumn<Pedido, Double> colFeitosTotal;
    @FXML private TableColumn<Pedido, String> colFeitosFuncionario;

    // Tabela 2: Na Rua
    @FXML private TableView<Pedido> tablePedidosNaRua;
    @FXML private TableColumn<Pedido, String> colNaRuaCliente;
    @FXML private TableColumn<Pedido, String> colNaRuaEndereco;
    @FXML private TableColumn<Pedido, String> colNaRuaProduto;
    @FXML private TableColumn<Pedido, Integer> colNaRuaQtd;
    @FXML private TableColumn<Pedido, String> colNaRuaHora;
    @FXML private TableColumn<Pedido, Double> colNaRuaTotal;
    @FXML private TableColumn<Pedido, String> colNaRuaFuncionario;

    // Tabela 3: Entregues
    @FXML private TableView<Pedido> tablePedidosEntregues;
    @FXML private TableColumn<Pedido, String> colEntreguesCliente;
    @FXML private TableColumn<Pedido, String> colEntreguesEndereco;
    @FXML private TableColumn<Pedido, String> colEntreguesProduto;
    @FXML private TableColumn<Pedido, Integer> colEntreguesQtd;
    @FXML private TableColumn<Pedido, String> colEntreguesHora;
    @FXML private TableColumn<Pedido, Double> colEntreguesTotal;
    @FXML private TableColumn<Pedido, String> colEntreguesFuncionario;

    private ObservableList<Cliente> clientesData = FXCollections.observableArrayList();
    private Cliente clienteSelecionado = null;
    private Funcionario funcionarioSelecionado = null;
    private Produto produtoSelecionado = null;

    @FXML
    private void initialize() {
        // Configura as colunas da tabela de clientes
        colNome.setCellValueFactory(cellData -> cellData.getValue().nomeProperty());
        colTelefone.setCellValueFactory(cellData -> cellData.getValue().telefoneProperty());
        colEndereco.setCellValueFactory(cellData -> cellData.getValue().enderecoProperty());
        colObservacoes.setCellValueFactory(cellData -> cellData.getValue().observacoesProperty());

        // Adiciona listener para a tabela de clientes
        clientesTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    // Se o usuário clicou em um cliente:
                    // 1. Preencha o formulário com os dados
                    nomeField.setText(newSelection.getNome());
                    telefoneField.setText(newSelection.getTelefone());
                    enderecoField.setText(newSelection.getEndereco());
                    observacoesField.setText(newSelection.getObservacoes());

                    // 2. Armazene o cliente selecionado
                    clienteSelecionado = newSelection;
                }
            }
        );

        // Carrega os clientes do banco
        carregarClientes();

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

        // Popula os ComboBoxes da aba Pedidos com as listas das outras tabelas
        pedidoClienteCombo.setItems(clientesTable.getItems());
        pedidoProdutoCombo.setItems(produtosTable.getItems());

        // Configura o Spinner de quantidade com valores de 1 a 99, padrão 1
        SpinnerValueFactory<Integer> valueFactory =
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 99, 1); // (min, max, padrão)
        pedidoQtdSpinner.setValueFactory(valueFactory);

        // Adiciona listener para preencher campos avulsos automaticamente
        pedidoClienteCombo.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    // Se o usuário ESCOLHEU um cliente:
                    // 1. Preencha os campos avulsos com os dados do cliente
                    pedidoNomeAvulsoField.setText(newSelection.getNome());
                    pedidoEnderecoAvulsoField.setText(newSelection.getEndereco());
                    // 2. Desabilite os campos avulsos
                    pedidoNomeAvulsoField.setDisable(true);
                    pedidoEnderecoAvulsoField.setDisable(true);
                } else {
                    // Se o usuário LIMPOU o ComboBox:
                    // 1. Limpe os campos avulsos
                    pedidoNomeAvulsoField.clear();
                    pedidoEnderecoAvulsoField.clear();
                    // 2. Re-abilite os campos
                    pedidoNomeAvulsoField.setDisable(false);
                    pedidoEnderecoAvulsoField.setDisable(false);
                }
            }
        );

        // Configura o DatePicker com a data de hoje
        pedidoDatePicker.setValue(LocalDate.now());
        
        // Adiciona listener para carregar pedidos quando a data mudar
        pedidoDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadPedidosPorData();
            }
        });

        // Configura as colunas da tabela de pedidos FEITOS
        colFeitosCliente.setCellValueFactory(new PropertyValueFactory<>("clienteNome"));
        colFeitosEndereco.setCellValueFactory(new PropertyValueFactory<>("endereco"));
        colFeitosProduto.setCellValueFactory(new PropertyValueFactory<>("produtoNome"));
        colFeitosQtd.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
        colFeitosHora.setCellValueFactory(new PropertyValueFactory<>("hora"));
        colFeitosTotal.setCellValueFactory(new PropertyValueFactory<>("precoTotal"));
        colFeitosFuncionario.setCellValueFactory(new PropertyValueFactory<>("funcionarioNome"));

        // Configura as colunas da tabela de pedidos NA RUA
        colNaRuaCliente.setCellValueFactory(new PropertyValueFactory<>("clienteNome"));
        colNaRuaEndereco.setCellValueFactory(new PropertyValueFactory<>("endereco"));
        colNaRuaProduto.setCellValueFactory(new PropertyValueFactory<>("produtoNome"));
        colNaRuaQtd.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
        colNaRuaHora.setCellValueFactory(new PropertyValueFactory<>("hora"));
        colNaRuaTotal.setCellValueFactory(new PropertyValueFactory<>("precoTotal"));
        colNaRuaFuncionario.setCellValueFactory(new PropertyValueFactory<>("funcionarioNome"));

        // Configura as colunas da tabela de pedidos ENTREGUES
        colEntreguesCliente.setCellValueFactory(new PropertyValueFactory<>("clienteNome"));
        colEntreguesEndereco.setCellValueFactory(new PropertyValueFactory<>("endereco"));
        colEntreguesProduto.setCellValueFactory(new PropertyValueFactory<>("produtoNome"));
        colEntreguesQtd.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
        colEntreguesHora.setCellValueFactory(new PropertyValueFactory<>("hora"));
        colEntreguesTotal.setCellValueFactory(new PropertyValueFactory<>("precoTotal"));
        colEntreguesFuncionario.setCellValueFactory(new PropertyValueFactory<>("funcionarioNome"));

        // Carrega os pedidos do banco
        loadPedidosPorData();
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

            // Atualiza o ComboBox de pedidos com a lista atualizada
            pedidoClienteCombo.setItems(clientesData);

        } catch (SQLException e) {
            System.err.println("Erro ao carregar clientes: " + e.getMessage());
        }
    }

    @FXML
    private void handleSalvarCliente() {
        String nome = nomeField.getText();
        String telefone = telefoneField.getText();
        String endereco = enderecoField.getText();
        String observacoes = observacoesField.getText();

        if (nome.isBlank()) {
            new Alert(AlertType.ERROR, "O nome é obrigatório.").show();
            return;
        }

        if (clienteSelecionado == null) {
            // MODO 1: CRIAR NOVO (INSERT)
            String sql = "INSERT INTO Clientes (nome, telefone, endereco, observacoes) VALUES (?, ?, ?, ?)";
            try (Connection conn = Database.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, nome);
                pstmt.setString(2, telefone);
                pstmt.setString(3, endereco);
                pstmt.setString(4, observacoes);
                pstmt.executeUpdate();
                new Alert(AlertType.INFORMATION, "Cliente salvo com sucesso!").show();
            } catch (SQLException e) {
                new Alert(AlertType.ERROR, "Erro ao salvar: " + e.getMessage()).show();
            }

        } else {
            // MODO 2: ATUALIZAR EXISTENTE (UPDATE)
            String sql = "UPDATE Clientes SET nome = ?, telefone = ?, endereco = ?, observacoes = ? WHERE id = ?";
            try (Connection conn = Database.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, nome);
                pstmt.setString(2, telefone);
                pstmt.setString(3, endereco);
                pstmt.setString(4, observacoes);
                pstmt.setInt(5, clienteSelecionado.getId());
                pstmt.executeUpdate();
                new Alert(AlertType.INFORMATION, "Cliente atualizado com sucesso!").show();
            } catch (SQLException e) {
                new Alert(AlertType.ERROR, "Erro ao atualizar: " + e.getMessage()).show();
            }
        }

        // No final, recarregue a tabela e limpe o formulário
        carregarClientes();
        handleLimparCliente();
    }

    @FXML
    private void handleLimparCliente() {
        // 1. Limpe a seleção
        clienteSelecionado = null;
        clientesTable.getSelectionModel().clearSelection();

        // 2. Limpe os campos do formulário
        nomeField.clear();
        telefoneField.clear();
        enderecoField.clear();
        observacoesField.clear();
    }

    @FXML
    private void handleExcluirCliente() {
        if (clienteSelecionado == null) {
            new Alert(AlertType.ERROR, "Selecione um cliente na tabela para excluir.").show();
            return;
        }

        // Crie um Alerta de Confirmação
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Exclusão");
        alert.setHeaderText("Excluir " + clienteSelecionado.getNome() + "?");
        alert.setContentText("Tem certeza? Esta ação não pode ser desfeita.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Usuário confirmou
                String sql = "DELETE FROM Clientes WHERE id = ?";
                try (Connection conn = Database.connect();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, clienteSelecionado.getId());
                    pstmt.executeUpdate();
                    new Alert(AlertType.INFORMATION, "Cliente excluído.").show();
                } catch (SQLException e) {
                    new Alert(AlertType.ERROR, "Erro ao excluir: " + e.getMessage()).show();
                }

                // Recarregue e limpe
                carregarClientes();
                handleLimparCliente();
            }
        });
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
        // Este método é quase IDÊNTICO ao 'carregarClientes()'.

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

            // Atualiza o ComboBox de pedidos com a lista atualizada
            pedidoProdutoCombo.setItems(listaProdutos);

            // Define o produto padrão no ComboBox
            setProdutoPadrao();

        } catch (SQLException e) {
            System.out.println("Erro ao carregar produtos: " + e.getMessage());
        }
    }

    private void setProdutoPadrao() {
        Produto produtoPadrao = null;

        // Procura pelo produto com ID = 2 (Água Maiorca original, mesmo que tenha mudado de nome)
        for (Produto p : produtosTable.getItems()) {
            if (p.getId() == 2) {
                produtoPadrao = p;
                break;
            }
        }

        // Fallback: se não encontrar o ID 2, pega o primeiro produto da lista (mais barato)
        if (produtoPadrao == null && !produtosTable.getItems().isEmpty()) {
            produtoPadrao = produtosTable.getItems().get(0);
        }

        // Define o valor no ComboBox
        pedidoProdutoCombo.setValue(produtoPadrao);
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
    private void handleCriarPedido() {
        // 1. Pegue todos os valores
        Cliente cliente = pedidoClienteCombo.getValue();
        Produto produto = pedidoProdutoCombo.getValue();
        int quantidade = pedidoQtdSpinner.getValue();
        String nomeAvulso = pedidoNomeAvulsoField.getText();
        String enderecoAvulso = pedidoEnderecoAvulsoField.getText();

        // 2. Validações
        if (produto == null) {
            new Alert(AlertType.ERROR, "Selecione um Produto.").show();
            return;
        }

        // 3. Prepare o SQL (sem funcionario_id)
        String sql = "INSERT INTO Pedidos (cliente_id, produto_id, status, data_hora, quantidade, nome_avulso, endereco_avulso) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        String dataAgora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String statusInicial = "Feito";

        // 4. Conecte e execute com lógica condicional
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Lógica do Cliente (se for cadastrado ou avulso)
            if (cliente != null) {
                pstmt.setInt(1, cliente.getId());
                pstmt.setNull(6, java.sql.Types.VARCHAR); // nome_avulso
                pstmt.setNull(7, java.sql.Types.VARCHAR); // endereco_avulso
            } else {
                pstmt.setNull(1, java.sql.Types.INTEGER); // cliente_id
                pstmt.setString(6, nomeAvulso);
                pstmt.setString(7, enderecoAvulso);
            }

            // Resto dos dados
            pstmt.setInt(2, produto.getId());
            pstmt.setString(3, statusInicial);
            pstmt.setString(4, dataAgora);
            pstmt.setInt(5, quantidade);

            pstmt.executeUpdate();
            new Alert(AlertType.INFORMATION, "Pedido criado com sucesso!").show();

        } catch (SQLException e) {
            new Alert(AlertType.ERROR, "Erro ao criar pedido: " + e.getMessage()).show();
        }

        // 5. Limpe os campos
        pedidoClienteCombo.setValue(null);
        setProdutoPadrao();
        pedidoNomeAvulsoField.clear();
        pedidoEnderecoAvulsoField.clear();
        pedidoQtdSpinner.getValueFactory().setValue(1); // Reseta para o padrão

        // 6. Recarregue a tabela
        loadPedidosPorData();
    }

    private void loadPedidosPorData() {
        // Obtém a data selecionada no DatePicker
        LocalDate dataSelecionada = pedidoDatePicker.getValue();
        if (dataSelecionada == null) {
            return; // Não faz nada se não houver data selecionada
        }
        
        String dataStr = dataSelecionada.toString(); // formato YYYY-MM-DD

        String sql = "SELECT p.id, " +
                     "COALESCE(c.nome, p.nome_avulso) as cliente, " +
                     "pr.nome as produto, p.quantidade, " +
                     "COALESCE(f.nome, 'Aguardando') as funcionario, " +
                     "p.status, strftime('%H:%M', p.data_hora) as hora, " +
                     "COALESCE(c.endereco, p.endereco_avulso) as endereco, " +
                     "(pr.preco * p.quantidade) as total " +
                     "FROM Pedidos p " +
                     "LEFT JOIN Clientes c ON p.cliente_id = c.id " +
                     "JOIN Produtos pr ON p.produto_id = pr.id " +
                     "LEFT JOIN Funcionarios f ON p.funcionario_id = f.id " +
                     "WHERE date(p.data_hora) = ? " + // Filtra pela data
                     "ORDER BY p.data_hora DESC";

        ObservableList<Pedido> pedidosFeitos = FXCollections.observableArrayList();
        ObservableList<Pedido> pedidosNaRua = FXCollections.observableArrayList();
        ObservableList<Pedido> pedidosEntregues = FXCollections.observableArrayList();

        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, dataStr);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Pedido pedido = new Pedido(
                    rs.getInt("id"),
                    rs.getString("cliente"),
                    rs.getString("produto"),
                    rs.getInt("quantidade"),
                    rs.getString("funcionario"),
                    rs.getString("status"),
                    rs.getString("hora"),
                    rs.getString("endereco"),
                    rs.getDouble("total")
                );
                
                // Separa os pedidos por status em diferentes tabelas
                String status = rs.getString("status");
                switch (status) {
                    case "Feito":
                        pedidosFeitos.add(pedido);
                        break;
                    case "Saiu p/ Entrega":
                        pedidosNaRua.add(pedido);
                        break;
                    case "Entregue":
                        pedidosEntregues.add(pedido);
                        break;
                    // Cancelado não aparece em nenhuma tabela
                }
            }
            
            tablePedidosFeitos.setItems(pedidosFeitos);
            tablePedidosNaRua.setItems(pedidosNaRua);
            tablePedidosEntregues.setItems(pedidosEntregues);

        } catch (SQLException e) {
            System.out.println("Erro ao carregar pedidos: " + e.getMessage());
        }
    }

    @FXML
    private void switchToSecondary() throws IOException {
        App.setRoot("secondary");
    }

    @FXML
    private void handleMarcarSaiu() {
        // Pega o pedido selecionado na tabela FEITOS
        Pedido pedidoSelecionado = tablePedidosFeitos.getSelectionModel().getSelectedItem();
        
        if (pedidoSelecionado == null) {
            new Alert(AlertType.WARNING, "Selecione um pedido na tabela 'Feitos' primeiro!").show();
            return;
        }

        // Carrega a lista de funcionários disponíveis
        ObservableList<Funcionario> funcionarios = FXCollections.observableArrayList();
        String sqlFunc = "SELECT id, nome FROM Funcionarios";
        
        try (Connection conn = Database.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sqlFunc)) {
            
            while (rs.next()) {
                funcionarios.add(new Funcionario(rs.getInt("id"), rs.getString("nome")));
            }
        } catch (SQLException e) {
            new Alert(AlertType.ERROR, "Erro ao carregar funcionários: " + e.getMessage()).show();
            return;
        }

        if (funcionarios.isEmpty()) {
            new Alert(AlertType.WARNING, "Nenhum funcionário cadastrado!").show();
            return;
        }

        // Abre um ChoiceDialog para escolher o funcionário
        ChoiceDialog<Funcionario> dialog = new ChoiceDialog<>(funcionarios.get(0), funcionarios);
        dialog.setTitle("Escolher Funcionário");
        dialog.setHeaderText("Marcar pedido como 'Saiu p/ Entrega'");
        dialog.setContentText("Escolha o funcionário:");
        
        dialog.showAndWait().ifPresent(funcionarioEscolhido -> {
            // Atualiza o pedido no banco
            String sqlUpdate = "UPDATE Pedidos SET status = ?, funcionario_id = ?, data_hora_saiu = ? WHERE id = ?";
            String dataHoraAgora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            try (Connection conn = Database.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sqlUpdate)) {
                
                pstmt.setString(1, "Saiu p/ Entrega");
                pstmt.setInt(2, funcionarioEscolhido.getId());
                pstmt.setString(3, dataHoraAgora);
                pstmt.setInt(4, pedidoSelecionado.getId());
                
                pstmt.executeUpdate();
                
                // Recarrega as tabelas
                loadPedidosPorData();
                
            } catch (SQLException e) {
                new Alert(AlertType.ERROR, "Erro ao atualizar pedido: " + e.getMessage()).show();
            }
        });
    }

    @FXML
    private void handleMarcarEntregue() {
        // Pega o pedido selecionado na tabela NA RUA
        Pedido pedidoSelecionado = tablePedidosNaRua.getSelectionModel().getSelectedItem();
        
        if (pedidoSelecionado == null) {
            new Alert(AlertType.WARNING, "Selecione um pedido na tabela 'Na Rua' primeiro!").show();
            return;
        }

        // Atualiza o pedido no banco
        String sqlUpdate = "UPDATE Pedidos SET status = ?, data_hora_entregue = ? WHERE id = ?";
        String dataHoraAgora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sqlUpdate)) {
            
            pstmt.setString(1, "Entregue");
            pstmt.setString(2, dataHoraAgora);
            pstmt.setInt(3, pedidoSelecionado.getId());
            
            pstmt.executeUpdate();
            new Alert(AlertType.INFORMATION, "Pedido marcado como 'Entregue'!").show();
            
            // Recarrega as tabelas
            loadPedidosPorData();
            
        } catch (SQLException e) {
            new Alert(AlertType.ERROR, "Erro ao atualizar pedido: " + e.getMessage()).show();
        }
    }

    @FXML
    private void handleCancelarPedido() {
        // Pega o pedido selecionado de qualquer uma das 3 tabelas
        Pedido pedidoSelecionado = tablePedidosFeitos.getSelectionModel().getSelectedItem();
        
        if (pedidoSelecionado == null) {
            pedidoSelecionado = tablePedidosNaRua.getSelectionModel().getSelectedItem();
        }
        
        if (pedidoSelecionado == null) {
            pedidoSelecionado = tablePedidosEntregues.getSelectionModel().getSelectedItem();
        }
        
        if (pedidoSelecionado == null) {
            new Alert(AlertType.WARNING, "Selecione um pedido em qualquer uma das tabelas primeiro!").show();
            return;
        }

        // Atualiza o pedido no banco
        String sqlUpdate = "UPDATE Pedidos SET status = ? WHERE id = ?";
        
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sqlUpdate)) {
            
            pstmt.setString(1, "Cancelado");
            pstmt.setInt(2, pedidoSelecionado.getId());
            
            pstmt.executeUpdate();
            new Alert(AlertType.INFORMATION, "Pedido cancelado!").show();
            
            // Recarrega as tabelas
            loadPedidosPorData();
            
        } catch (SQLException e) {
            new Alert(AlertType.ERROR, "Erro ao cancelar pedido: " + e.getMessage()).show();
        }
    }
}
