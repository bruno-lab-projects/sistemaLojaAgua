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
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.function.UnaryOperator;

public class PrimaryController {

    // Componentes da aba Clientes
    @FXML private TextField nomeField;
    @FXML private TextField telefoneField;
    @FXML private TextField enderecoField;
    @FXML private TextField predioCasaField;
    @FXML private TextField blocoNumeroField;
    @FXML private TextField observacoesField;
    @FXML private Button salvarButton;
    @FXML private Button limparClienteButton;

    // Componentes da aba Pedidos
    @FXML private ComboBox<Cliente> pedidoClienteCombo;
    @FXML private ComboBox<Produto> pedidoProdutoCombo;
    @FXML private Spinner<Integer> pedidoQtdSpinner;
    @FXML private TextField pedidoNomeAvulsoField;
    @FXML private TextField pedidoEnderecoAvulsoField;
    @FXML private Button criarPedidoButton;
    @FXML private DatePicker pedidoDatePicker;
    @FXML private TabPane statusTabPane;

    // Labels de título com contagem
    @FXML private Label lblTituloPendentes;
    @FXML private Label lblTituloSairam;
    @FXML private Label lblTituloEntregues;

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

    @FXML
    private void initialize() {
        // Configura máscara de telefone
        configurarMascaraTelefone();
        
        // Carrega clientes para o ComboBox de pedidos
        carregarClientes();
        
        // Carrega produtos para o ComboBox de pedidos
        carregarProdutosParaCombo();


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
        ObservableList<Cliente> listaClientes = FXCollections.observableArrayList();
        String sql = "SELECT id, nome, telefone, endereco, predio_casa, numero, observacoes FROM Clientes";

        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Cliente cliente = new Cliente(
                    rs.getInt("id"),
                    rs.getString("nome"),
                    rs.getString("telefone"),
                    rs.getString("endereco"),
                    rs.getString("predio_casa"),
                    rs.getString("numero"),
                    rs.getString("observacoes")
                );
                listaClientes.add(cliente);
            }

            // Atualiza o ComboBox de pedidos com a lista atualizada
            pedidoClienteCombo.setItems(listaClientes);

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
        String endereco = enderecoField.getText();
        String predioCasa = predioCasaField.getText();
        String blocoNumero = blocoNumeroField.getText();
        String observacoes = observacoesField.getText();

        if (nome.isBlank()) {
            new Alert(AlertType.ERROR, "O nome é obrigatório.").show();
            return;
        }

        // Cria novo cliente (INSERT)
        String sql = "INSERT INTO Clientes (nome, telefone, endereco, predio_casa, numero, observacoes) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nome);
            pstmt.setString(2, telefone);
            pstmt.setString(3, endereco);
            pstmt.setString(4, predioCasa);
            pstmt.setString(5, blocoNumero);
            pstmt.setString(6, observacoes);
            pstmt.executeUpdate();
            new Alert(AlertType.INFORMATION, "Cliente salvo com sucesso!").show();
        } catch (SQLException e) {
            new Alert(AlertType.ERROR, "Erro ao salvar: " + e.getMessage()).show();
        }

        // Recarrega lista de clientes e limpa o formulário
        carregarClientes();
        handleLimparCliente();
    }

    @FXML
    private void handleLimparCliente() {
        // Limpa os campos do formulário
        nomeField.clear();
        telefoneField.setText("(71) 9"); // Reseta para o padrão
        enderecoField.clear();
        predioCasaField.clear();
        blocoNumeroField.clear();
        observacoesField.clear();
    }

    private void carregarProdutosParaCombo() {
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
            pedidoProdutoCombo.setItems(listaProdutos);

            // Define o produto padrão no ComboBox
            setProdutoPadrao(listaProdutos);

        } catch (SQLException e) {
            System.out.println("Erro ao carregar produtos: " + e.getMessage());
        }
    }

    private void setProdutoPadrao(ObservableList<Produto> listaProdutos) {
        // Define o produto mais barato (primeiro da lista) como padrão
        if (!listaProdutos.isEmpty()) {
            pedidoProdutoCombo.setValue(listaProdutos.get(0));
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
        carregarProdutosParaCombo(); // Recarrega produtos e define o padrão
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

            // Atualiza os Labels com as contagens
            lblTituloPendentes.setText(String.format("PENDENTES (%d)", pedidosFeitos.size()));
            lblTituloSairam.setText(String.format("SAÍRAM PARA ENTREGA (%d)", pedidosNaRua.size()));
            lblTituloEntregues.setText(String.format("ENTREGUES (%d)", pedidosEntregues.size()));

        } catch (SQLException e) {
            System.out.println("Erro ao carregar pedidos: " + e.getMessage());
        }
    }

    @FXML
    private void switchToSecondary() throws IOException {
        // Cria o diálogo de senha
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Acesso Restrito");
        dialog.setHeaderText("Área Administrativa");
        dialog.setContentText("Digite a senha de administrador:");

        // Mostra o diálogo e espera a resposta
        dialog.showAndWait().ifPresent(senha -> {
            // Verifica se a senha está correta
            if (senha.equals("admin123")) {
                // Senha correta - troca para a tela secundária
                try {
                    App.setRoot("secondary");
                } catch (IOException e) {
                    new Alert(AlertType.ERROR, "Erro ao carregar tela: " + e.getMessage()).show();
                }
            } else {
                // Senha incorreta - mostra erro
                new Alert(AlertType.ERROR, "Senha incorreta!").show();
            }
        });
        // Se o usuário cancelar (ifPresent não executa), não faz nada
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

        // Diálogo de confirmação
        Alert confirmacao = new Alert(AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar Cancelamento");
        confirmacao.setHeaderText("Tem certeza que deseja cancelar este pedido?");
        confirmacao.setContentText("Cliente: " + pedidoSelecionado.getClienteNome() + "\n" +
                                   "Produto: " + pedidoSelecionado.getProdutoNome() + "\n" +
                                   "Quantidade: " + pedidoSelecionado.getQuantidade());
        
        // Aguarda a resposta do usuário
        if (confirmacao.showAndWait().get() != ButtonType.OK) {
            return; // Usuário cancelou a operação
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
