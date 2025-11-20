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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.function.UnaryOperator;

public class SecondaryController {

    // Injeções FXML para Clientes
    @FXML private TextField nomeField;
    @FXML private TextField telefoneField;
    @FXML private TextField clientePredioField;
    @FXML private TextField clienteNumeroField;
    @FXML private TextField clienteRuaField;
    @FXML private TextField observacoesField;
    @FXML private Button salvarButton;
    @FXML private Button excluirClienteButton;
    @FXML private Button limparClienteButton;
    @FXML private TableView<Cliente> clientesTable;
    @FXML private TableColumn<Cliente, String> colNome;
    @FXML private TableColumn<Cliente, String> colTelefone;
    @FXML private TableColumn<Cliente, String> colEndereco;
    @FXML private TableColumn<Cliente, String> colObservacoes;

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

    // Injeções FXML para Pendências Financeiras
    @FXML private TableView<Pedido> tabelaPendenciaFinanceira;
    @FXML private TableColumn<Pedido, String> colFinCliente;
    @FXML private TableColumn<Pedido, String> colFinEndereco;
    @FXML private TableColumn<Pedido, String> colFinData;
    @FXML private TableColumn<Pedido, Double> colFinValor;
    @FXML private Button btnBaixarFinanceiro;

    // Injeções FXML para Pendências de Garrafão
    @FXML private TableView<Pedido> tabelaPendenciaGarrafao;
    @FXML private TableColumn<Pedido, String> colGarCliente;
    @FXML private TableColumn<Pedido, String> colGarProduto;
    @FXML private TableColumn<Pedido, String> colGarEndereco;
    @FXML private TableColumn<Pedido, String> colGarData;
    @FXML private Button btnBaixarGarrafao;

    private ObservableList<Cliente> clientesData = FXCollections.observableArrayList();
    private ObservableList<Pedido> pendenciasFinanceiraData = FXCollections.observableArrayList();
    private ObservableList<Pedido> pendenciasGarrafaoData = FXCollections.observableArrayList();
    private Cliente clienteSelecionado = null;
    private Funcionario funcionarioSelecionado = null;
    private Produto produtoSelecionado = null;

    @FXML
    private void initialize() {
        // Configura máscara de telefone
        configurarMascaraTelefone();
        
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
                    clientePredioField.setText(newSelection.getPredioCasa());
                    clienteNumeroField.setText(newSelection.getNumero());
                    clienteRuaField.setText(newSelection.getEndereco());
                    observacoesField.setText(newSelection.getObservacoes());

                    // 2. Armazene o cliente selecionado
                    clienteSelecionado = newSelection;
                }
            }
        );

        // Carrega os clientes do banco
        loadClientesDaTabela();

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

        // Configura as colunas da tabela de pendências financeiras
        colFinCliente.setCellValueFactory(new PropertyValueFactory<>("clienteNome"));
        colFinEndereco.setCellValueFactory(new PropertyValueFactory<>("endereco"));
        colFinData.setCellValueFactory(new PropertyValueFactory<>("hora"));
        colFinValor.setCellValueFactory(new PropertyValueFactory<>("precoTotal"));
        colFinValor.setCellFactory(column -> new TableCell<Pedido, Double>() {
            @Override
            protected void updateItem(Double valor, boolean empty) {
                super.updateItem(valor, empty);
                if (empty || valor == null) {
                    setText(null);
                } else {
                    setText(String.format("R$ %.2f", valor));
                }
            }
        });

        // Configura as colunas da tabela de pendências de garrafão
        colGarCliente.setCellValueFactory(new PropertyValueFactory<>("clienteNome"));
        colGarProduto.setCellValueFactory(new PropertyValueFactory<>("produtoNome"));
        colGarEndereco.setCellValueFactory(new PropertyValueFactory<>("endereco"));
        colGarData.setCellValueFactory(new PropertyValueFactory<>("hora"));

        // Carrega as pendências do banco
        loadPendencias();
    }

    // ==================== MÉTODOS DE CLIENTES ====================

    private void loadClientesDaTabela() {
        clientesData.clear();
        String sql = "SELECT id, nome, telefone, " +
                     "(predio_casa || ', ' || numero || ', ' || endereco) as endereco_completo, " +
                     "predio_casa, numero, endereco, observacoes " +
                     "FROM Clientes";

        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Cliente cliente = new Cliente(
                    rs.getInt("id"),
                    rs.getString("nome"),
                    rs.getString("telefone"),
                    rs.getString("endereco_completo"),
                    rs.getString("predio_casa"),
                    rs.getString("numero"),
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

    private void configurarMascaraTelefone() {
        // Cria um TextFormatter que formata automaticamente
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            
            // Remove tudo que não é dígito
            String apenasNumeros = newText.replaceAll("[^0-9]", "");
            
            // Se apagou tudo, permite campo vazio
            if (apenasNumeros.isEmpty()) {
                change.setText("");
                change.setRange(0, change.getControlText().length());
                return change;
            }
            
            // Limita a 11 dígitos
            if (apenasNumeros.length() > 11) {
                return null; // Rejeita a mudança
            }
            
            // Formata o telefone
            String formatted = formatarTelefone(apenasNumeros);
            
            // Atualiza o change com o texto formatado
            change.setText(formatted);
            change.setRange(0, change.getControlText().length());
            
            // Ajusta a posição do cursor
            int newCaretPos = formatted.length();
            change.setCaretPosition(newCaretPos);
            change.setAnchor(newCaretPos);
            
            return change;
        };
        
        TextFormatter<String> textFormatter = new TextFormatter<>(filter);
        telefoneField.setTextFormatter(textFormatter);
        
        // Define o texto inicial como "(71) 9"
        telefoneField.setText("(71) 9");
        
        // Ao focar no campo vazio, preenche com o padrão (71) 9
        telefoneField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused && telefoneField.getText().isEmpty()) {
                telefoneField.setText("(71) 9");
                telefoneField.positionCaret(telefoneField.getText().length());
            }
        });
    }
    
    private String formatarTelefone(String numeros) {
        // Formata: (DD) 9 9999-9999
        StringBuilder formatted = new StringBuilder();
        
        int length = numeros.length();
        
        if (length <= 2) {
            // Apenas DDD: (71
            formatted.append("(").append(numeros);
        } else if (length == 3) {
            // DDD completo + primeiro dígito: (71) 9
            formatted.append("(").append(numeros.substring(0, 2))
                     .append(") ").append(numeros.substring(2));
        } else if (length <= 7) {
            // DDD + primeiros dígitos: (71) 9 1234
            formatted.append("(").append(numeros.substring(0, 2))
                     .append(") ").append(numeros.substring(2, 3))
                     .append(" ").append(numeros.substring(3));
        } else {
            // Formato completo: (71) 9 1234-5678
            formatted.append("(").append(numeros.substring(0, 2))
                     .append(") ").append(numeros.substring(2, 3))
                     .append(" ").append(numeros.substring(3, 7))
                     .append("-").append(numeros.substring(7));
        }
        
        return formatted.toString();
    }

    @FXML
    private void handleSalvarCliente() {
        String nome = nomeField.getText();
        String telefone = telefoneField.getText();
        String predioCasa = clientePredioField.getText();
        String numero = clienteNumeroField.getText();
        String endereco = clienteRuaField.getText();
        String observacoes = observacoesField.getText();

        if (nome.isBlank()) {
            new Alert(AlertType.ERROR, "O nome é obrigatório.").show();
            return;
        }

        if (clienteSelecionado == null) {
            // MODO 1: CRIAR NOVO (INSERT)
            String sql = "INSERT INTO Clientes (nome, telefone, endereco, predio_casa, numero, observacoes) VALUES (?, ?, ?, ?, ?, ?)";
            try (Connection conn = Database.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, nome);
                pstmt.setString(2, telefone);
                pstmt.setString(3, endereco);
                pstmt.setString(4, predioCasa);
                pstmt.setString(5, numero);
                pstmt.setString(6, observacoes);
                pstmt.executeUpdate();
                new Alert(AlertType.INFORMATION, "Cliente salvo com sucesso!").show();
            } catch (SQLException e) {
                new Alert(AlertType.ERROR, "Erro ao salvar: " + e.getMessage()).show();
            }

        } else {
            // MODO 2: ATUALIZAR EXISTENTE (UPDATE)
            String sql = "UPDATE Clientes SET nome = ?, telefone = ?, endereco = ?, predio_casa = ?, numero = ?, observacoes = ? WHERE id = ?";
            try (Connection conn = Database.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, nome);
                pstmt.setString(2, telefone);
                pstmt.setString(3, endereco);
                pstmt.setString(4, predioCasa);
                pstmt.setString(5, numero);
                pstmt.setString(6, observacoes);
                pstmt.setInt(7, clienteSelecionado.getId());
                pstmt.executeUpdate();
                new Alert(AlertType.INFORMATION, "Cliente atualizado com sucesso!").show();
            } catch (SQLException e) {
                new Alert(AlertType.ERROR, "Erro ao atualizar: " + e.getMessage()).show();
            }
        }

        // No final, recarregue a tabela e limpe o formulário
        loadClientesDaTabela();
        handleLimparCliente();
    }

    @FXML
    private void handleLimparCliente() {
        // 1. Limpe a seleção
        clienteSelecionado = null;
        clientesTable.getSelectionModel().clearSelection();

        // 2. Limpe os campos do formulário
        nomeField.clear();
        telefoneField.setText("(71) 9"); // Reseta para o padrão
        clientePredioField.clear();
        clienteNumeroField.clear();
        clienteRuaField.clear();
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
                loadClientesDaTabela();
                handleLimparCliente();
            }
        });
    }

    // ==================== MÉTODOS DE PRODUTOS ====================

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

    // ==================== MÉTODOS DE PENDÊNCIAS ====================

    private void loadPendencias() {
        pendenciasFinanceiraData.clear();
        pendenciasGarrafaoData.clear();
        
        String sql = "SELECT p.id, " +
                     "COALESCE(c.nome, p.nome_avulso) as cliente, " +
                     "CASE " +
                     "  WHEN p.cliente_id IS NOT NULL THEN " +
                     "    c.predio_casa || ', ' || c.numero || ', ' || c.endereco " +
                     "  ELSE " +
                     "    p.predio_casa_avulso || ', ' || p.numero_avulso || ', ' || p.endereco_avulso " +
                     "END as endereco_completo, " +
                     "strftime('%d/%m/%Y', p.data_hora_entregue) as data_entrega, " +
                     "p.pendencia_pagamento, " +
                     "p.pendencia_garrafao, " +
                     "pr.nome as produto, " +
                     "(pr.preco * p.quantidade) as total " +
                     "FROM Pedidos p " +
                     "LEFT JOIN Clientes c ON p.cliente_id = c.id " +
                     "JOIN Produtos pr ON p.produto_id = pr.id " +
                     "WHERE p.pendencia_pagamento = 1 OR p.pendencia_garrafao = 1 " +
                     "ORDER BY p.data_hora_entregue DESC";

        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int pendPagamento = rs.getInt("pendencia_pagamento");
                int pendGarrafao = rs.getInt("pendencia_garrafao");
                
                // Cria um Pedido temporário para exibição
                Pedido pedido = new Pedido(
                    rs.getInt("id"),
                    rs.getString("cliente"),
                    rs.getString("produto"),
                    0, // quantidade não é importante aqui
                    "", // funcionario não é importante aqui
                    "", // status não é importante aqui
                    rs.getString("data_entrega"),
                    rs.getString("endereco_completo"),
                    rs.getDouble("total"),
                    null // forma_pagamento não é importante aqui
                );
                
                // Adiciona à tabela financeira se tem pendência de pagamento
                if (pendPagamento == 1) {
                    pendenciasFinanceiraData.add(pedido);
                }
                
                // Adiciona à tabela de garrafão se tem pendência de garrafão
                if (pendGarrafao == 1) {
                    pendenciasGarrafaoData.add(pedido);
                }
            }

            tabelaPendenciaFinanceira.setItems(pendenciasFinanceiraData);
            tabelaPendenciaGarrafao.setItems(pendenciasGarrafaoData);

        } catch (SQLException e) {
            System.err.println("Erro ao carregar pendências: " + e.getMessage());
        }
    }

    @FXML
    private void handleBaixarFinanceiro() {
        Pedido pedidoSelecionado = tabelaPendenciaFinanceira.getSelectionModel().getSelectedItem();
        
        if (pedidoSelecionado == null) {
            new Alert(AlertType.WARNING, "Selecione uma pendência financeira na tabela primeiro!").show();
            return;
        }

        String sql = "UPDATE Pedidos SET pendencia_pagamento = 0 WHERE id = ?";
        
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, pedidoSelecionado.getId());
            pstmt.executeUpdate();
            
            new Alert(AlertType.INFORMATION, "Pendência financeira baixada com sucesso!").show();
            loadPendencias(); // Recarrega as tabelas
            
        } catch (SQLException e) {
            new Alert(AlertType.ERROR, "Erro ao baixar pendência: " + e.getMessage()).show();
        }
    }

    @FXML
    private void handleBaixarGarrafao() {
        Pedido pedidoSelecionado = tabelaPendenciaGarrafao.getSelectionModel().getSelectedItem();
        
        if (pedidoSelecionado == null) {
            new Alert(AlertType.WARNING, "Selecione uma pendência de garrafão na tabela primeiro!").show();
            return;
        }

        String sql = "UPDATE Pedidos SET pendencia_garrafao = 0 WHERE id = ?";
        
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, pedidoSelecionado.getId());
            pstmt.executeUpdate();
            
            new Alert(AlertType.INFORMATION, "Pendência de garrafão baixada com sucesso!").show();
            loadPendencias(); // Recarrega as tabelas
            
        } catch (SQLException e) {
            new Alert(AlertType.ERROR, "Erro ao baixar pendência: " + e.getMessage()).show();
        }
    }

    @FXML
    private void switchToPrimary() throws IOException {
        App.setRoot("primary");
    }
}