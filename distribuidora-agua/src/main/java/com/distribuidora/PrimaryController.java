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
import javafx.collections.transformation.FilteredList;
import javafx.util.StringConverter;
import javafx.beans.property.SimpleStringProperty;
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
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Dialog;
import javafx.scene.control.CheckBox;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import java.util.function.UnaryOperator;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.control.TableRow;

public class PrimaryController {

    // Variável de controle para modo de edição
    private Pedido pedidoEmEdicao = null;
    
    // Variável para armazenar o pedido sendo arrastado
    private Pedido pedidoSendoArrastado = null;
    
    // Lista mestra de clientes para autocomplete
    private ObservableList<Cliente> clientesMestra = FXCollections.observableArrayList();
    
    // Flag para evitar loop infinito no autocomplete
    private boolean ignorarMudancaTexto = false;

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
    @FXML private ComboBox<FormaPagamento> pedidoPagamentoCombo;
    @FXML private Spinner<Integer> pedidoQtdSpinner;
    @FXML private TextField pedidoNomeAvulsoField;
    @FXML private TextField pedidoAvulsoTipoField;   // (Casa/Prédio)
    @FXML private TextField pedidoAvulsoNumeroField; // (Número)
    @FXML private TextField pedidoAvulsoRuaField;    // (Logradouro)
    @FXML private Button criarPedidoButton;
    @FXML private Button limparPedidoButton;
    @FXML private Button editarPedidoButton;
    @FXML private DatePicker pedidoDatePicker;

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
    @FXML private TableColumn<Pedido, String> colFeitosTotal;
    @FXML private TableColumn<Pedido, String> colFeitosPagamento;

    // Tabela 2: Na Rua
    @FXML private TableView<Pedido> tablePedidosNaRua;
    @FXML private TableColumn<Pedido, String> colNaRuaCliente;
    @FXML private TableColumn<Pedido, String> colNaRuaEndereco;
    @FXML private TableColumn<Pedido, String> colNaRuaProduto;
    @FXML private TableColumn<Pedido, Integer> colNaRuaQtd;
    @FXML private TableColumn<Pedido, String> colNaRuaHora;
    @FXML private TableColumn<Pedido, String> colNaRuaTotal;
    @FXML private TableColumn<Pedido, String> colNaRuaFuncionario;

    // Tabela 3: Entregues
    @FXML private TableView<Pedido> tablePedidosEntregues;
    @FXML private TableColumn<Pedido, String> colEntreguesCliente;
    @FXML private TableColumn<Pedido, String> colEntreguesEndereco;
    @FXML private TableColumn<Pedido, String> colEntreguesProduto;
    @FXML private TableColumn<Pedido, Integer> colEntreguesQtd;
    @FXML private TableColumn<Pedido, String> colEntreguesHora;
    @FXML private TableColumn<Pedido, String> colEntreguesTotal;
    @FXML private TableColumn<Pedido, String> colEntreguesFuncionario;

    @FXML
    private void initialize() {
        // Configura máscara de telefone
        configurarMascaraTelefone();
        
        // Configura autocomplete para o ComboBox de clientes
        configurarAutocompleteClientes();
        
        // Carrega produtos para o ComboBox de pedidos
        carregarProdutosParaCombo();

        // Popula o ComboBox de Forma de Pagamento
        pedidoPagamentoCombo.getItems().setAll(FormaPagamento.values());

        // Configura o Spinner de quantidade com valores de 1 a 99, padrão 1
        SpinnerValueFactory<Integer> valueFactory =
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 99, 1); // (min, max, padrão)
        pedidoQtdSpinner.setValueFactory(valueFactory);

        // Configura o DatePicker com a data de hoje
        if (pedidoDatePicker != null) {
            pedidoDatePicker.setValue(LocalDate.now());
            
            // Adiciona listener para carregar pedidos quando a data mudar
            pedidoDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    loadPedidosPorData();
                }
            });
        }

        // Configura as colunas da tabela de pedidos FEITOS
        colFeitosCliente.setCellValueFactory(new PropertyValueFactory<>("clienteNome"));
        colFeitosEndereco.setCellValueFactory(new PropertyValueFactory<>("endereco"));
        colFeitosProduto.setCellValueFactory(new PropertyValueFactory<>("produtoNome"));
        colFeitosQtd.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
        colFeitosHora.setCellValueFactory(new PropertyValueFactory<>("hora"));
        colFeitosTotal.setCellValueFactory(cellData -> {
            double valor = cellData.getValue().getPrecoTotal();
            return new SimpleStringProperty(String.format("R$ %.2f", valor));
        });
        colFeitosPagamento.setCellValueFactory(cellData -> {
            // Converte o Enum para String para exibir na tabela
            var pgto = cellData.getValue().getFormaPagamento();
            return new SimpleStringProperty(pgto != null ? pgto.toString() : "-");
        });

        // Configura as colunas da tabela de pedidos NA RUA
        colNaRuaCliente.setCellValueFactory(new PropertyValueFactory<>("clienteNome"));
        colNaRuaEndereco.setCellValueFactory(new PropertyValueFactory<>("endereco"));
        colNaRuaProduto.setCellValueFactory(new PropertyValueFactory<>("produtoNome"));
        colNaRuaQtd.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
        colNaRuaHora.setCellValueFactory(new PropertyValueFactory<>("hora"));
        colNaRuaTotal.setCellValueFactory(cellData -> {
            double valor = cellData.getValue().getPrecoTotal();
            return new SimpleStringProperty(String.format("R$ %.2f", valor));
        });
        colNaRuaFuncionario.setCellValueFactory(new PropertyValueFactory<>("funcionarioNome"));

        // Configura as colunas da tabela de pedidos ENTREGUES
        colEntreguesCliente.setCellValueFactory(new PropertyValueFactory<>("clienteNome"));
        colEntreguesEndereco.setCellValueFactory(new PropertyValueFactory<>("endereco"));
        colEntreguesProduto.setCellValueFactory(new PropertyValueFactory<>("produtoNome"));
        colEntreguesQtd.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
        colEntreguesHora.setCellValueFactory(new PropertyValueFactory<>("hora"));
        colEntreguesTotal.setCellValueFactory(cellData -> {
            double valor = cellData.getValue().getPrecoTotal();
            return new SimpleStringProperty(String.format("R$ %.2f", valor));
        });
        colEntreguesFuncionario.setCellValueFactory(new PropertyValueFactory<>("funcionarioNome"));

        // Carrega os pedidos do banco
        loadPedidosPorData();
        
        // Configura Drag-and-Drop entre as tabelas
        configurarDragAndDrop();
    }

    /**
     * Configura o sistema de Drag-and-Drop entre as tabelas.
     * Suporta 4 cenários:
     * 1. Feitos -> Na Rua (Marcar Saiu)
     * 2. Na Rua -> Entregues (Marcar Entregue)
     * 3. Feitos -> Entregues (Pulo - Funcionário + Entrega)
     * 4. Na Rua -> Feitos (Retorno - Resetar pedido)
     */
    private void configurarDragAndDrop() {
        // ========== CONFIGURAÇÃO DAS ORIGENS (Row Factories) ==========
        
        // Configura tablePedidosFeitos como ORIGEM
        tablePedidosFeitos.setRowFactory(tv -> {
            TableRow<Pedido> row = new TableRow<>();
            
            row.setOnDragDetected(event -> {
                if (!row.isEmpty()) {
                    Pedido pedido = row.getItem();
                    pedidoSendoArrastado = pedido;
                    
                    Dragboard dragboard = row.startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent content = new ClipboardContent();
                    content.putString("FEITOS:" + pedido.getId());
                    dragboard.setContent(content);
                    
                    event.consume();
                }
            });
            
            row.setOnDragDone(event -> {
                pedidoSendoArrastado = null;
                event.consume();
            });
            
            return row;
        });
        
        // Configura tablePedidosNaRua como ORIGEM
        tablePedidosNaRua.setRowFactory(tv -> {
            TableRow<Pedido> row = new TableRow<>();
            
            row.setOnDragDetected(event -> {
                if (!row.isEmpty()) {
                    Pedido pedido = row.getItem();
                    pedidoSendoArrastado = pedido;
                    
                    Dragboard dragboard = row.startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent content = new ClipboardContent();
                    content.putString("NARUA:" + pedido.getId());
                    dragboard.setContent(content);
                    
                    event.consume();
                }
            });
            
            row.setOnDragDone(event -> {
                pedidoSendoArrastado = null;
                event.consume();
            });
            
            return row;
        });
        
        // ========== CONFIGURAÇÃO DOS DESTINOS ==========
        
        // ===== tablePedidosFeitos como DESTINO (CENÁRIO 4: Na Rua -> Feitos) =====
        tablePedidosFeitos.setOnDragOver(event -> {
            if (event.getGestureSource() != tablePedidosFeitos && event.getDragboard().hasString()) {
                String content = event.getDragboard().getString();
                // Só aceita se vier de "Na Rua"
                if (content.startsWith("NARUA:")) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }
            }
            event.consume();
        });
        
        tablePedidosFeitos.setOnDragDropped(event -> {
            Dragboard dragboard = event.getDragboard();
            boolean success = false;
            
            if (dragboard.hasString() && pedidoSendoArrastado != null) {
                String content = dragboard.getString();
                
                // CENÁRIO 4: Na Rua -> Feitos (Retorno/Reset)
                if (content.startsWith("NARUA:")) {
                    success = handleRetornoParaFeitos(pedidoSendoArrastado);
                }
            }
            
            event.setDropCompleted(success);
            event.consume();
        });
        
        tablePedidosFeitos.setOnDragEntered(event -> {
            if (event.getGestureSource() != tablePedidosFeitos && event.getDragboard().hasString()) {
                String content = event.getDragboard().getString();
                if (content.startsWith("NARUA:")) {
                    tablePedidosFeitos.setStyle("-fx-border-color: #FF9800; -fx-border-width: 2px;");
                }
            }
            event.consume();
        });
        
        tablePedidosFeitos.setOnDragExited(event -> {
            tablePedidosFeitos.setStyle("");
            event.consume();
        });
        
        // ===== tablePedidosNaRua como DESTINO (CENÁRIO 1: Feitos -> Na Rua) =====
        tablePedidosNaRua.setOnDragOver(event -> {
            if (event.getGestureSource() != tablePedidosNaRua && event.getDragboard().hasString()) {
                String content = event.getDragboard().getString();
                // Só aceita se vier de "Feitos"
                if (content.startsWith("FEITOS:")) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }
            }
            event.consume();
        });
        
        tablePedidosNaRua.setOnDragDropped(event -> {
            Dragboard dragboard = event.getDragboard();
            boolean success = false;
            
            if (dragboard.hasString() && pedidoSendoArrastado != null) {
                String content = dragboard.getString();
                
                // CENÁRIO 1: Feitos -> Na Rua
                if (content.startsWith("FEITOS:")) {
                    tablePedidosFeitos.getSelectionModel().select(pedidoSendoArrastado);
                    handleMarcarSaiu();
                    success = true;
                }
            }
            
            event.setDropCompleted(success);
            event.consume();
        });
        
        tablePedidosNaRua.setOnDragEntered(event -> {
            if (event.getGestureSource() != tablePedidosNaRua && event.getDragboard().hasString()) {
                String content = event.getDragboard().getString();
                if (content.startsWith("FEITOS:")) {
                    tablePedidosNaRua.setStyle("-fx-border-color: #4CAF50; -fx-border-width: 2px;");
                }
            }
            event.consume();
        });
        
        tablePedidosNaRua.setOnDragExited(event -> {
            tablePedidosNaRua.setStyle("");
            event.consume();
        });
        
        // ===== tablePedidosEntregues como DESTINO (CENÁRIOS 2 e 3) =====
        tablePedidosEntregues.setOnDragOver(event -> {
            if (event.getGestureSource() != tablePedidosEntregues && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });
        
        tablePedidosEntregues.setOnDragDropped(event -> {
            Dragboard dragboard = event.getDragboard();
            boolean success = false;
            
            if (dragboard.hasString() && pedidoSendoArrastado != null) {
                String content = dragboard.getString();
                
                // CENÁRIO 2: Na Rua -> Entregues
                if (content.startsWith("NARUA:")) {
                    tablePedidosNaRua.getSelectionModel().select(pedidoSendoArrastado);
                    handleMarcarEntregue();
                    success = true;
                }
                // CENÁRIO 3: Feitos -> Entregues (Pulo)
                else if (content.startsWith("FEITOS:")) {
                    success = handlePuloParaEntregues(pedidoSendoArrastado);
                }
            }
            
            event.setDropCompleted(success);
            event.consume();
        });
        
        tablePedidosEntregues.setOnDragEntered(event -> {
            if (event.getGestureSource() != tablePedidosEntregues && event.getDragboard().hasString()) {
                tablePedidosEntregues.setStyle("-fx-border-color: #2196F3; -fx-border-width: 2px;");
            }
            event.consume();
        });
        
        tablePedidosEntregues.setOnDragExited(event -> {
            tablePedidosEntregues.setStyle("");
            event.consume();
        });
    }
    
    /**
     * CENÁRIO 3: Pulo de Feitos -> Entregues
     * Abre diálogo de funcionário, depois diálogo de pagamento/garrafão,
     * e atualiza o pedido diretamente para Entregue.
     */
    private boolean handlePuloParaEntregues(Pedido pedido) {
        // 1. Carrega funcionários
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
            return false;
        }

        if (funcionarios.isEmpty()) {
            new Alert(AlertType.WARNING, "Nenhum funcionário cadastrado!").show();
            return false;
        }

        // 2. Escolhe o funcionário
        ChoiceDialog<Funcionario> dialogFunc = new ChoiceDialog<>(funcionarios.get(0), funcionarios);
        dialogFunc.setTitle("Escolher Funcionário");
        dialogFunc.setHeaderText("Pulo para Entregue");
        dialogFunc.setContentText("Escolha o funcionário que fez a entrega:");
        
        final Funcionario[] funcionarioEscolhido = {null};
        dialogFunc.showAndWait().ifPresent(func -> funcionarioEscolhido[0] = func);
        
        if (funcionarioEscolhido[0] == null) {
            return false; // Usuário cancelou
        }
        
        // 3. Abre dialog de pagamento/garrafão
        Dialog<ButtonType> dialogEntrega = new Dialog<>();
        dialogEntrega.setTitle("Confirmar Entrega");
        dialogEntrega.setHeaderText("Marcar como Entregue");
        
        CheckBox chkPagou = new CheckBox("Pagamento Recebido?");
        chkPagou.setSelected(true);
        
        CheckBox chkGarrafao = new CheckBox("Garrafão Devolvido?");
        chkGarrafao.setSelected(true);
        
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));
        vbox.getChildren().addAll(chkPagou, chkGarrafao);
        
        dialogEntrega.getDialogPane().setContent(vbox);
        dialogEntrega.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        final boolean[] confirmed = {false};
        dialogEntrega.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                confirmed[0] = true;
            }
        });
        
        if (!confirmed[0]) {
            return false; // Usuário cancelou
        }
        
        // 4. Atualiza o pedido no banco
        int pendenciaPagamento = chkPagou.isSelected() ? 0 : 1;
        int pendenciaGarrafao = chkGarrafao.isSelected() ? 0 : 1;
        
        String sqlUpdate = "UPDATE Pedidos SET status = ?, funcionario_id = ?, " +
                           "data_hora_saiu = ?, data_hora_entregue = ?, " +
                           "pendencia_pagamento = ?, pendencia_garrafao = ? WHERE id = ?";
        String dataHoraAgora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sqlUpdate)) {
            
            pstmt.setString(1, "Entregue");
            pstmt.setInt(2, funcionarioEscolhido[0].getId());
            pstmt.setString(3, dataHoraAgora);
            pstmt.setString(4, dataHoraAgora);
            pstmt.setInt(5, pendenciaPagamento);
            pstmt.setInt(6, pendenciaGarrafao);
            pstmt.setInt(7, pedido.getId());
            
            pstmt.executeUpdate();
            
            // Recarrega as tabelas
            loadPedidosPorData();
            
            new Alert(AlertType.INFORMATION, "Pedido marcado como Entregue!").show();
            return true;
            
        } catch (SQLException e) {
            new Alert(AlertType.ERROR, "Erro ao atualizar pedido: " + e.getMessage()).show();
            return false;
        }
    }
    
    /**
     * CENÁRIO 4: Retorno de Na Rua -> Feitos
     * Reseta o pedido para o status inicial, removendo funcionário e datas.
     */
    private boolean handleRetornoParaFeitos(Pedido pedido) {
        // Confirma a ação
        Alert confirmacao = new Alert(AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar Retorno");
        confirmacao.setHeaderText("Retornar pedido para Pendentes");
        confirmacao.setContentText("Isso irá resetar o pedido, removendo funcionário e horário de saída. Confirmar?");
        
        final boolean[] confirmed = {false};
        confirmacao.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                confirmed[0] = true;
            }
        });
        
        if (!confirmed[0]) {
            return false;
        }
        
        // Reseta o pedido no banco
        String sqlUpdate = "UPDATE Pedidos SET status = ?, funcionario_id = NULL, data_hora_saiu = NULL WHERE id = ?";
        
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sqlUpdate)) {
            
            pstmt.setString(1, "Feito");
            pstmt.setInt(2, pedido.getId());
            
            pstmt.executeUpdate();
            
            // Recarrega as tabelas
            loadPedidosPorData();
            
            new Alert(AlertType.INFORMATION, "Pedido retornado para Pendentes!").show();
            return true;
            
        } catch (SQLException e) {
            new Alert(AlertType.ERROR, "Erro ao atualizar pedido: " + e.getMessage()).show();
            return false;
        }
    }

    /**
     * Configura o sistema de autocomplete para o ComboBox de clientes.
     * Permite pesquisa/filtro em tempo real enquanto o usuário digita.
     */
    private void configurarAutocompleteClientes() {
        // 1. Carrega a lista mestra do banco de dados
        carregarClientesDoBanco();
        
        // 2. Cria a lista filtrada baseada na lista mestra
        FilteredList<Cliente> listaFiltrada = new FilteredList<>(clientesMestra, p -> true);
        pedidoClienteCombo.setItems(listaFiltrada);
        
        // 3. Configura como o Cliente aparece no texto (StringConverter)
        pedidoClienteCombo.setConverter(new StringConverter<Cliente>() {
            @Override
            public String toString(Cliente cliente) {
                return (cliente != null) ? cliente.getNome() : "";
            }
            
            @Override
            public Cliente fromString(String string) {
                if (string == null || string.isEmpty()) {
                    return null;
                }
                return clientesMestra.stream()
                        .filter(c -> c.getNome().equalsIgnoreCase(string))
                        .findFirst().orElse(null);
            }
        });
        
        // 4. Listener de seleção - preenche campos quando cliente é selecionado
        pedidoClienteCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (ignorarMudancaTexto) {
                return; // Ignora se estamos em processo de atualização
            }
            
            if (newVal != null) {
                // Cliente selecionado - preenche campos
                pedidoNomeAvulsoField.setText(newVal.getNome());
                pedidoAvulsoTipoField.setText(newVal.getPredioCasa());
                pedidoAvulsoNumeroField.setText(newVal.getNumero());
                pedidoAvulsoRuaField.setText(newVal.getEndereco());
                
                pedidoNomeAvulsoField.setDisable(true);
                pedidoAvulsoTipoField.setDisable(true);
                pedidoAvulsoNumeroField.setDisable(true);
                pedidoAvulsoRuaField.setDisable(true);
            } else {
                // Seleção limpa - habilita campos
                pedidoNomeAvulsoField.clear();
                pedidoAvulsoTipoField.clear();
                pedidoAvulsoNumeroField.clear();
                pedidoAvulsoRuaField.clear();
                
                pedidoNomeAvulsoField.setDisable(false);
                pedidoAvulsoTipoField.setDisable(false);
                pedidoAvulsoNumeroField.setDisable(false);
                pedidoAvulsoRuaField.setDisable(false);
            }
        });
        
        // 5. Listener de texto - filtra a lista enquanto usuário digita
        pedidoClienteCombo.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            if (ignorarMudancaTexto) {
                return; // Ignora se for mudança programática
            }
            
            final Cliente currentValue = pedidoClienteCombo.getValue();
            
            // Se o texto for apagado completamente
            if (newText == null || newText.isEmpty()) {
                listaFiltrada.setPredicate(c -> true);
                
                // Só limpa o valor se já não estiver null
                if (currentValue != null) {
                    ignorarMudancaTexto = true;
                    pedidoClienteCombo.setValue(null);
                    ignorarMudancaTexto = false;
                }
                return;
            }
            
            // Se o texto for igual ao nome do cliente atual, não faz nada
            if (currentValue != null && currentValue.getNome().equals(newText)) {
                return;
            }
            
            // Limpa seleção se o texto não corresponder mais ao cliente selecionado
            if (currentValue != null && !currentValue.getNome().equalsIgnoreCase(newText)) {
                ignorarMudancaTexto = true;
                pedidoClienteCombo.setValue(null);
                ignorarMudancaTexto = false;
            }
            
            // Filtra a lista baseado no texto digitado
            final String lowerText = newText.toLowerCase();
            listaFiltrada.setPredicate(c -> c.getNome().toLowerCase().contains(lowerText));
            
            // Se houver resultados, abre o dropdown
            if (!listaFiltrada.isEmpty()) {
                if (!pedidoClienteCombo.isShowing()) {
                    pedidoClienteCombo.show();
                }
            }
        });
        
        // 6. Listener para fechar dropdown ao selecionar
        pedidoClienteCombo.setOnAction(event -> {
            Cliente selecionado = pedidoClienteCombo.getValue();
            if (selecionado != null) {
                ignorarMudancaTexto = true;
                pedidoClienteCombo.getEditor().setText(selecionado.getNome());
                ignorarMudancaTexto = false;
                pedidoClienteCombo.hide();
            }
        });
    }
    
    /**
     * Carrega os clientes do banco de dados para a lista mestra.
     * Este método é chamado apenas uma vez na inicialização e quando há atualizações.
     */
    private void carregarClientesDoBanco() {
        clientesMestra.clear();
        String sql = "SELECT id, nome, telefone, endereco, predio_casa, numero, observacoes FROM Clientes ORDER BY nome ASC";

        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                // Monta o endereço completo para exibição no ComboBox
                String enderecoCompleto = rs.getString("predio_casa") + ", " + 
                                         rs.getString("numero") + ", " + 
                                         rs.getString("endereco");
                
                Cliente cliente = new Cliente(
                    rs.getInt("id"),
                    rs.getString("nome"),
                    rs.getString("telefone"),
                    enderecoCompleto,                          // Endereço completo
                    rs.getString("predio_casa"),
                    rs.getString("numero"),
                    rs.getString("observacoes"),
                    rs.getString("endereco")                   // Apenas a rua
                );
                clientesMestra.add(cliente);
            }

        } catch (SQLException e) {
            System.err.println("Erro ao carregar clientes: " + e.getMessage());
        }
    }

    /**
     * Método utilitário que capitaliza cada palavra de um nome.
     * Exemplo: "BRUNO SANTOS" ou "bruno santos" → "Bruno Santos"
     */
    private String capitalizarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            return nome;
        }
        
        String[] palavras = nome.trim().toLowerCase().split("\\s+");
        StringBuilder resultado = new StringBuilder();
        
        for (String palavra : palavras) {
            if (!palavra.isEmpty()) {
                // Capitaliza a primeira letra e mantém o resto em minúsculo
                resultado.append(Character.toUpperCase(palavra.charAt(0)))
                         .append(palavra.substring(1))
                         .append(" ");
            }
        }
        
        return resultado.toString().trim();
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
        String nome = capitalizarNome(nomeField.getText());
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
        carregarClientesDoBanco();
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
            setProdutoPadrao();

        } catch (SQLException e) {
            System.out.println("Erro ao carregar produtos: " + e.getMessage());
        }
    }

    private void setProdutoPadrao() {
        // Versão sem parâmetros - pega a lista atual do ComboBox
        ObservableList<Produto> listaProdutos = pedidoProdutoCombo.getItems();
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
        String tipoAvulso = pedidoAvulsoTipoField.getText();
        String numeroAvulso = pedidoAvulsoNumeroField.getText();
        String ruaAvulso = pedidoAvulsoRuaField.getText();
        FormaPagamento pagamento = pedidoPagamentoCombo.getValue();

        // 2. Validações
        if (produto == null) {
            new Alert(AlertType.ERROR, "Selecione um Produto.").show();
            return;
        }

        if (pedidoEmEdicao == null) {
            // ========== MODO: CRIAR NOVO PEDIDO ==========
            String sql = "INSERT INTO Pedidos (cliente_id, produto_id, status, data_hora, quantidade, nome_avulso, endereco_avulso, predio_casa_avulso, numero_avulso, forma_pagamento) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            String dataAgora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String statusInicial = "Feito";

            try (Connection conn = Database.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                // Lógica do Cliente (se for cadastrado ou avulso)
                if (cliente != null) {
                    pstmt.setInt(1, cliente.getId());
                    pstmt.setNull(6, java.sql.Types.VARCHAR); // nome_avulso
                    pstmt.setNull(7, java.sql.Types.VARCHAR); // endereco_avulso
                    pstmt.setNull(8, java.sql.Types.VARCHAR); // predio_casa_avulso
                    pstmt.setNull(9, java.sql.Types.VARCHAR); // numero_avulso
                } else {
                    pstmt.setNull(1, java.sql.Types.INTEGER); // cliente_id
                    pstmt.setString(6, capitalizarNome(nomeAvulso));
                    pstmt.setString(7, ruaAvulso);
                    pstmt.setString(8, tipoAvulso);
                    pstmt.setString(9, numeroAvulso);
                }

                // Resto dos dados
                pstmt.setInt(2, produto.getId());
                pstmt.setString(3, statusInicial);
                pstmt.setString(4, dataAgora);
                pstmt.setInt(5, quantidade);

                // Forma de pagamento
                if (pagamento != null) {
                    pstmt.setString(10, pagamento.name());
                } else {
                    pstmt.setNull(10, java.sql.Types.VARCHAR);
                }

                pstmt.executeUpdate();
                new Alert(AlertType.INFORMATION, "Pedido criado com sucesso!").show();

            } catch (SQLException e) {
                new Alert(AlertType.ERROR, "Erro ao criar pedido: " + e.getMessage()).show();
            }

        } else {
            // ========== MODO: EDITAR PEDIDO EXISTENTE ==========
            String sql = "UPDATE Pedidos SET cliente_id = ?, produto_id = ?, quantidade = ?, " +
                         "nome_avulso = ?, endereco_avulso = ?, predio_casa_avulso = ?, numero_avulso = ?, " +
                         "forma_pagamento = ? WHERE id = ?";

            try (Connection conn = Database.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                // Lógica do Cliente (se for cadastrado ou avulso)
                if (cliente != null) {
                    pstmt.setInt(1, cliente.getId());
                    pstmt.setNull(4, java.sql.Types.VARCHAR); // nome_avulso
                    pstmt.setNull(5, java.sql.Types.VARCHAR); // endereco_avulso
                    pstmt.setNull(6, java.sql.Types.VARCHAR); // predio_casa_avulso
                    pstmt.setNull(7, java.sql.Types.VARCHAR); // numero_avulso
                } else {
                    pstmt.setNull(1, java.sql.Types.INTEGER); // cliente_id
                    pstmt.setString(4, capitalizarNome(nomeAvulso));
                    pstmt.setString(5, ruaAvulso);
                    pstmt.setString(6, tipoAvulso);
                    pstmt.setString(7, numeroAvulso);
                }

                // Resto dos dados
                pstmt.setInt(2, produto.getId());
                pstmt.setInt(3, quantidade);

                // Forma de pagamento
                if (pagamento != null) {
                    pstmt.setString(8, pagamento.name());
                } else {
                    pstmt.setNull(8, java.sql.Types.VARCHAR);
                }

                pstmt.setInt(9, pedidoEmEdicao.getId());

                pstmt.executeUpdate();
                new Alert(AlertType.INFORMATION, "Pedido atualizado com sucesso!").show();

            } catch (SQLException e) {
                new Alert(AlertType.ERROR, "Erro ao atualizar pedido: " + e.getMessage()).show();
            }
        }

        // Sai do modo de edição e reseta o botão
        pedidoEmEdicao = null;
        criarPedidoButton.setText("Criar Pedido");
        criarPedidoButton.setStyle("");

        // Limpe os campos
        pedidoClienteCombo.setValue(null);
        carregarProdutosParaCombo(); // Recarrega produtos e define o padrão
        pedidoNomeAvulsoField.clear();
        pedidoAvulsoTipoField.clear();
        pedidoAvulsoNumeroField.clear();
        pedidoAvulsoRuaField.clear();
        pedidoQtdSpinner.getValueFactory().setValue(1); // Reseta para o padrão
        pedidoPagamentoCombo.setValue(null);

        // Recarregue a tabela
        loadPedidosPorData();
    }

    @FXML
    private void handleLimparPedido() {
        // Cancela o modo de edição se estiver ativo
        pedidoEmEdicao = null;
        criarPedidoButton.setText("Criar Pedido");
        criarPedidoButton.setStyle("");

        // 1. Resete o Cliente (isso vai disparar o listener que limpa/habilita os campos avulsos automaticamente!)
        pedidoClienteCombo.setValue(null);

        // 2. Por segurança, garanta a limpeza e habilitação dos campos avulsos explicitamente:
        pedidoNomeAvulsoField.clear();
        pedidoAvulsoTipoField.clear();
        pedidoAvulsoNumeroField.clear();
        pedidoAvulsoRuaField.clear();
        pedidoNomeAvulsoField.setDisable(false);
        pedidoAvulsoTipoField.setDisable(false);
        pedidoAvulsoNumeroField.setDisable(false);
        pedidoAvulsoRuaField.setDisable(false);

        // 3. Resete o Produto para o padrão (use o helper que criamos)
        setProdutoPadrao();

        // 4. Resete a Quantidade para 1
        pedidoQtdSpinner.getValueFactory().setValue(1);

        // 5. Limpe a Forma de Pagamento
        pedidoPagamentoCombo.setValue(null);
    }

    @FXML
    private void handleEditarPedido() {
        // Tenta pegar o pedido selecionado de qualquer uma das 3 tabelas
        Pedido pedido = tablePedidosFeitos.getSelectionModel().getSelectedItem();
        if (pedido == null) pedido = tablePedidosNaRua.getSelectionModel().getSelectedItem();
        if (pedido == null) pedido = tablePedidosEntregues.getSelectionModel().getSelectedItem();

        if (pedido == null) {
            new Alert(AlertType.ERROR, "Selecione um pedido para editar.").show();
            return;
        }

        // Entra no "Modo de Edição"
        pedidoEmEdicao = pedido;
        criarPedidoButton.setText("Salvar Alterações");
        criarPedidoButton.setStyle("-fx-base: #ffd700;"); // Muda cor para amarelo

        // PREENCHE O FORMULÁRIO COM OS DADOS DO PEDIDO
        pedidoQtdSpinner.getValueFactory().setValue(pedido.getQuantidade());

        // Preenche Forma de Pagamento
        if (pedido.getFormaPagamento() != null) {
            pedidoPagamentoCombo.setValue(pedido.getFormaPagamento());
        } else {
            pedidoPagamentoCombo.setValue(null);
        }

        // Preenche Produto - busca o produto na lista do combo pelo nome
        for (Produto p : pedidoProdutoCombo.getItems()) {
            if (p.getNome().equals(pedido.getProdutoNome())) {
                pedidoProdutoCombo.setValue(p);
                break;
            }
        }

        // Lógica para Cliente ou Avulso
        // Primeiro, verifica se é um pedido com cliente cadastrado
        boolean isClienteCadastrado = false;
        for (Cliente c : pedidoClienteCombo.getItems()) {
            if (c.getNome().equals(pedido.getClienteNome())) {
                pedidoClienteCombo.setValue(c);
                isClienteCadastrado = true;
                break;
            }
        }

        // Se não encontrou cliente cadastrado, é avulso
        if (!isClienteCadastrado) {
            pedidoClienteCombo.setValue(null);
            pedidoNomeAvulsoField.setText(pedido.getClienteNome());
            
            // Extrai endereço do formato "Prédio/Casa, Número, Rua"
            String endereco = pedido.getEndereco();
            if (endereco != null && !endereco.isEmpty()) {
                String[] partes = endereco.split(", ", 3);
                if (partes.length >= 3) {
                    pedidoAvulsoTipoField.setText(partes[0]);
                    pedidoAvulsoNumeroField.setText(partes[1]);
                    pedidoAvulsoRuaField.setText(partes[2]);
                }
            }
        }
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
                     "CASE " +
                     "  WHEN p.cliente_id IS NOT NULL THEN " +
                     "    c.predio_casa || ', ' || c.numero || ', ' || c.endereco " +
                     "  ELSE " +
                     "    p.predio_casa_avulso || ', ' || p.numero_avulso || ', ' || p.endereco_avulso " +
                     "END as endereco_completo, " +
                     "(pr.preco * p.quantidade) as total, " +
                     "p.forma_pagamento " +
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
                // Recupera e converte a forma de pagamento
                String formaPagamentoStr = rs.getString("forma_pagamento");
                FormaPagamento formaPagamento = null;
                if (formaPagamentoStr != null && !formaPagamentoStr.isEmpty()) {
                    try {
                        formaPagamento = FormaPagamento.valueOf(formaPagamentoStr);
                    } catch (IllegalArgumentException e) {
                        // Se o valor não for válido, mantém null
                    }
                }
                
                Pedido pedido = new Pedido(
                    rs.getInt("id"),
                    rs.getString("cliente"),
                    rs.getString("produto"),
                    rs.getInt("quantidade"),
                    rs.getString("funcionario"),
                    rs.getString("status"),
                    rs.getString("hora"),
                    rs.getString("endereco_completo"),
                    rs.getDouble("total"),
                    formaPagamento
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
            new Alert(AlertType.WARNING, "Selecione um pedido na tabela 'Pendentes' primeiro!").show();
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
            new Alert(AlertType.WARNING, "Selecione um pedido na tabela 'Saíram para entrega' primeiro!").show();
            return;
        }

        // Cria o Dialog de confirmação
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Confirmar Entrega");
        dialog.setHeaderText("Marcar pedido como 'Entregue'");
        
        // Cria os CheckBoxes
        CheckBox chkPagou = new CheckBox("Pagamento Recebido?");
        chkPagou.setSelected(true); // Marcado por padrão
        
        CheckBox chkGarrafao = new CheckBox("Garrafão Devolvido?");
        chkGarrafao.setSelected(true); // Marcado por padrão
        
        // Cria o VBox com os CheckBoxes
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));
        vbox.getChildren().addAll(chkPagou, chkGarrafao);
        
        dialog.getDialogPane().setContent(vbox);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Mostra o dialog e espera a resposta
        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Verifica os CheckBoxes para determinar as pendências
                int pendenciaPagamento = chkPagou.isSelected() ? 0 : 1;
                int pendenciaGarrafao = chkGarrafao.isSelected() ? 0 : 1;
                
                // Atualiza o pedido no banco
                String sqlUpdate = "UPDATE Pedidos SET status = ?, data_hora_entregue = ?, pendencia_pagamento = ?, pendencia_garrafao = ? WHERE id = ?";
                String dataHoraAgora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                
                try (Connection conn = Database.connect();
                     PreparedStatement pstmt = conn.prepareStatement(sqlUpdate)) {
                    
                    pstmt.setString(1, "Entregue");
                    pstmt.setString(2, dataHoraAgora);
                    pstmt.setInt(3, pendenciaPagamento);
                    pstmt.setInt(4, pendenciaGarrafao);
                    pstmt.setInt(5, pedidoSelecionado.getId());
                    
                    pstmt.executeUpdate();
                    new Alert(AlertType.INFORMATION, "Pedido marcado como 'Entregue'!").show();
                    
                    // Recarrega as tabelas
                    loadPedidosPorData();
                    
                } catch (SQLException e) {
                    new Alert(AlertType.ERROR, "Erro ao atualizar pedido: " + e.getMessage()).show();
                }
            }
        });
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
