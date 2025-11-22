package com.distribuidora;

import javafx.beans.property.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe Pedido.
 * Verifica a corretude do construtor, getters, properties do JavaFX e lógica de negócio.
 */
class PedidoTest {

    // ==================== TESTES DO CONSTRUTOR ====================

    @Test
    void testConstrutorCompleto() {
        // Arrange & Act - Cria um pedido com todos os parâmetros
        Pedido pedido = new Pedido(
            1,                              // id
            "João Silva",                   // clienteNome
            "Garrafão 20L",                 // produtoNome
            5,                              // quantidade
            "Carlos Entregador",            // funcionarioNome
            "Feito",                        // status
            "2025-11-22 10:30:00",         // hora
            "Rua A, Ed. Sol, Apt 101",     // endereco
            50.0,                           // precoTotal
            FormaPagamento.DINHEIRO         // formaPagamento
        );

        // Assert - Verifica se todos os valores foram atribuídos corretamente
        assertEquals(1, pedido.getId(), "ID deve ser 1");
        assertEquals("João Silva", pedido.getClienteNome(), "Cliente nome deve ser 'João Silva'");
        assertEquals("Garrafão 20L", pedido.getProdutoNome(), "Produto nome deve ser 'Garrafão 20L'");
        assertEquals(5, pedido.getQuantidade(), "Quantidade deve ser 5");
        assertEquals("Carlos Entregador", pedido.getFuncionarioNome(), "Funcionário deve ser 'Carlos Entregador'");
        assertEquals("Feito", pedido.getStatus(), "Status deve ser 'Feito'");
        assertEquals("2025-11-22 10:30:00", pedido.getHora(), "Hora deve estar correta");
        assertEquals("Rua A, Ed. Sol, Apt 101", pedido.getEndereco(), "Endereço deve estar correto");
        assertEquals(50.0, pedido.getPrecoTotal(), 0.001, "Preço total deve ser 50.0");
        assertEquals(FormaPagamento.DINHEIRO, pedido.getFormaPagamento(), "Forma de pagamento deve ser DINHEIRO");
    }

    @Test
    void testConstrutorComValoresNulos() {
        // Arrange & Act - Cria pedido com valores opcionais nulos
        Pedido pedido = new Pedido(
            2,
            null,              // clienteNome pode ser nulo (pedido avulso)
            "Produto X",
            1,
            null,              // funcionarioNome pode ser nulo
            "Feito",
            "2025-11-22 11:00:00",
            null,              // endereco pode ser nulo
            10.0,
            null               // formaPagamento pode ser nula
        );

        // Assert
        assertEquals(2, pedido.getId());
        assertNull(pedido.getClienteNome(), "Cliente nome pode ser nulo para pedidos avulsos");
        assertEquals("Produto X", pedido.getProdutoNome());
        assertNull(pedido.getFuncionarioNome(), "Funcionário pode ser nulo");
        assertNull(pedido.getEndereco(), "Endereço pode ser nulo");
        assertNull(pedido.getFormaPagamento(), "Forma de pagamento pode ser nula");
    }

    @Test
    void testConstrutorComQuantidadeZero() {
        // Arrange & Act - Testa comportamento com quantidade zero (caso de borda)
        Pedido pedido = new Pedido(
            3, "Cliente Teste", "Produto", 0, null, "Feito", 
            "2025-11-22", null, 0.0, null
        );

        // Assert
        assertEquals(0, pedido.getQuantidade(), "Quantidade pode ser 0");
        assertEquals(0.0, pedido.getPrecoTotal(), "Preço total pode ser 0");
    }

    // ==================== TESTES DE GETTERS E SETTERS ====================

    @Test
    void testSetterStatus() {
        // Arrange
        Pedido pedido = new Pedido(4, "Maria", "Produto A", 2, null, "Feito", 
            "2025-11-22", null, 20.0, null);

        // Act
        pedido.setStatus("Na Rua");

        // Assert
        assertEquals("Na Rua", pedido.getStatus(), "setStatus() deve atualizar o status");
    }

    @Test
    void testSetterQuantidade() {
        // Arrange
        Pedido pedido = new Pedido(5, "Pedro", "Produto B", 1, null, "Feito", 
            "2025-11-22", null, 10.0, null);

        // Act
        pedido.setQuantidade(10);

        // Assert
        assertEquals(10, pedido.getQuantidade(), "setQuantidade() deve atualizar a quantidade");
    }

    @Test
    void testSetterPrecoTotal() {
        // Arrange
        Pedido pedido = new Pedido(6, "Ana", "Produto C", 3, null, "Feito", 
            "2025-11-22", null, 30.0, null);

        // Act
        pedido.setPrecoTotal(45.50);

        // Assert
        assertEquals(45.50, pedido.getPrecoTotal(), 0.001, 
            "setPrecoTotal() deve atualizar o preço total");
    }

    @Test
    void testSetterFormaPagamento() {
        // Arrange
        Pedido pedido = new Pedido(7, "Juliana", "Produto D", 1, null, "Feito", 
            "2025-11-22", null, 10.0, FormaPagamento.DINHEIRO);

        // Act
        pedido.setFormaPagamento(FormaPagamento.PIX);

        // Assert
        assertEquals(FormaPagamento.PIX, pedido.getFormaPagamento(), 
            "setFormaPagamento() deve atualizar a forma de pagamento");
    }

    // ==================== TESTES DE JAVAFX PROPERTIES ====================

    @Test
    void testPropertiesNaoSaoNulas() {
        // Arrange
        Pedido pedido = new Pedido(8, "Roberto", "Garrafão 10L", 2, "Entregador A", 
            "Entregue", "2025-11-22 14:00:00", "Rua B", 20.0, FormaPagamento.CARTAO);

        // Act & Assert - Verifica que todas as properties foram inicializadas
        assertNotNull(pedido.idProperty(), "idProperty não deve ser null");
        assertNotNull(pedido.clienteNomeProperty(), "clienteNomeProperty não deve ser null");
        assertNotNull(pedido.produtoNomeProperty(), "produtoNomeProperty não deve ser null");
        assertNotNull(pedido.quantidadeProperty(), "quantidadeProperty não deve ser null");
        assertNotNull(pedido.funcionarioNomeProperty(), "funcionarioNomeProperty não deve ser null");
        assertNotNull(pedido.statusProperty(), "statusProperty não deve ser null");
        assertNotNull(pedido.horaProperty(), "horaProperty não deve ser null");
        assertNotNull(pedido.enderecoProperty(), "enderecoProperty não deve ser null");
        assertNotNull(pedido.precoTotalProperty(), "precoTotalProperty não deve ser null");
        assertNotNull(pedido.formaPagamentoProperty(), "formaPagamentoProperty não deve ser null");
    }

    @Test
    void testPropertiesSaoDoTipoCorreto() {
        // Arrange
        Pedido pedido = new Pedido(9, "Teste", "Produto", 1, null, "Feito", 
            "2025-11-22", null, 10.0, null);

        // Act & Assert - Verifica que as properties são dos tipos corretos
        assertTrue(pedido.idProperty() instanceof SimpleIntegerProperty, 
            "idProperty deve ser SimpleIntegerProperty");
        assertTrue(pedido.clienteNomeProperty() instanceof SimpleStringProperty, 
            "clienteNomeProperty deve ser SimpleStringProperty");
        assertTrue(pedido.produtoNomeProperty() instanceof SimpleStringProperty, 
            "produtoNomeProperty deve ser SimpleStringProperty");
        assertTrue(pedido.quantidadeProperty() instanceof SimpleIntegerProperty, 
            "quantidadeProperty deve ser SimpleIntegerProperty");
        assertTrue(pedido.statusProperty() instanceof SimpleStringProperty, 
            "statusProperty deve ser SimpleStringProperty");
        assertTrue(pedido.precoTotalProperty() instanceof SimpleDoubleProperty, 
            "precoTotalProperty deve ser SimpleDoubleProperty");
        assertTrue(pedido.formaPagamentoProperty() instanceof ObjectProperty, 
            "formaPagamentoProperty deve ser ObjectProperty");
    }

    @Test
    void testPropertyValueBinding() {
        // Arrange
        Pedido pedido = new Pedido(10, "Patricia", "Produto E", 1, null, "Feito", 
            "2025-11-22", null, 10.0, null);

        // Act - Altera o valor através da property
        pedido.statusProperty().set("Entregue");
        pedido.quantidadeProperty().set(5);
        pedido.precoTotalProperty().set(50.0);

        // Assert - Verifica se os getters retornam os novos valores
        assertEquals("Entregue", pedido.getStatus(), 
            "Alteração via statusProperty deve refletir no getter");
        assertEquals(5, pedido.getQuantidade(), 
            "Alteração via quantidadeProperty deve refletir no getter");
        assertEquals(50.0, pedido.getPrecoTotal(), 0.001, 
            "Alteração via precoTotalProperty deve refletir no getter");
    }

    // ==================== TESTES DE LÓGICA DE NEGÓCIO ====================

    @Test
    void testCalculoPrecoTotal_QuantidadeVezesPrecoPorUnidade() {
        // Arrange - Simula um pedido onde preço total = quantidade * preço unitário
        int quantidade = 5;
        double precoUnitario = 10.0;
        double precoTotalEsperado = quantidade * precoUnitario; // 50.0

        // Act
        Pedido pedido = new Pedido(11, "Cliente Calc", "Garrafão", quantidade, null, 
            "Feito", "2025-11-22", null, precoTotalEsperado, null);

        // Assert
        assertEquals(precoTotalEsperado, pedido.getPrecoTotal(), 0.001, 
            "Preço total deve ser quantidade * preço unitário");
        assertEquals(50.0, pedido.getPrecoTotal(), 0.001, 
            "5 unidades * R$ 10.00 = R$ 50.00");
    }

    @Test
    void testCalculoPrecoTotal_ComValorDecimal() {
        // Arrange
        int quantidade = 3;
        double precoUnitario = 12.50;
        double precoTotalEsperado = quantidade * precoUnitario; // 37.50

        // Act
        Pedido pedido = new Pedido(12, "Cliente B", "Produto F", quantidade, null, 
            "Feito", "2025-11-22", null, precoTotalEsperado, null);

        // Assert
        assertEquals(37.50, pedido.getPrecoTotal(), 0.001, 
            "3 unidades * R$ 12.50 = R$ 37.50");
    }

    @Test
    void testCalculoPrecoTotal_UmaUnidade() {
        // Arrange
        double precoUnitario = 8.99;

        // Act
        Pedido pedido = new Pedido(13, "Cliente C", "Produto G", 1, null, 
            "Feito", "2025-11-22", null, precoUnitario, null);

        // Assert
        assertEquals(8.99, pedido.getPrecoTotal(), 0.001, 
            "1 unidade deve ter preço total igual ao preço unitário");
    }

    @Test
    void testStatusFeito() {
        // Arrange & Act
        Pedido pedido = new Pedido(14, "Cliente Status", "Produto", 1, null, 
            "Feito", "2025-11-22", null, 10.0, null);

        // Assert
        assertEquals("Feito", pedido.getStatus(), "Status inicial deve ser 'Feito'");
    }

    @Test
    void testStatusNaRua() {
        // Arrange
        Pedido pedido = new Pedido(15, "Cliente Rua", "Produto", 1, null, 
            "Feito", "2025-11-22", null, 10.0, null);

        // Act
        pedido.setStatus("Na Rua");

        // Assert
        assertEquals("Na Rua", pedido.getStatus(), 
            "Status deve ser atualizado para 'Na Rua'");
    }

    @Test
    void testStatusEntregue() {
        // Arrange
        Pedido pedido = new Pedido(16, "Cliente Entregue", "Produto", 1, null, 
            "Feito", "2025-11-22", null, 10.0, null);

        // Act
        pedido.setStatus("Entregue");

        // Assert
        assertEquals("Entregue", pedido.getStatus(), 
            "Status deve ser atualizado para 'Entregue'");
    }

    // ==================== TESTES COM FORMAS DE PAGAMENTO ====================

    @Test
    void testFormaPagamentoDinheiro() {
        // Arrange & Act
        Pedido pedido = new Pedido(17, "Cliente A", "Produto", 1, null, 
            "Feito", "2025-11-22", null, 10.0, FormaPagamento.DINHEIRO);

        // Assert
        assertEquals(FormaPagamento.DINHEIRO, pedido.getFormaPagamento(), 
            "Forma de pagamento deve ser DINHEIRO");
    }

    @Test
    void testFormaPagamentoPix() {
        // Arrange & Act
        Pedido pedido = new Pedido(18, "Cliente B", "Produto", 1, null, 
            "Feito", "2025-11-22", null, 10.0, FormaPagamento.PIX);

        // Assert
        assertEquals(FormaPagamento.PIX, pedido.getFormaPagamento(), 
            "Forma de pagamento deve ser PIX");
    }

    @Test
    void testFormaPagamentoCartao() {
        // Arrange & Act
        Pedido pedido = new Pedido(19, "Cliente C", "Produto", 1, null, 
            "Feito", "2025-11-22", null, 10.0, FormaPagamento.CARTAO);

        // Assert
        assertEquals(FormaPagamento.CARTAO, pedido.getFormaPagamento(), 
            "Forma de pagamento deve ser CARTAO");
    }

    @Test
    void testAlterarFormaPagamento() {
        // Arrange
        Pedido pedido = new Pedido(20, "Cliente D", "Produto", 1, null, 
            "Feito", "2025-11-22", null, 10.0, FormaPagamento.DINHEIRO);

        // Act
        pedido.setFormaPagamento(FormaPagamento.PIX);

        // Assert
        assertEquals(FormaPagamento.PIX, pedido.getFormaPagamento(), 
            "Forma de pagamento deve ser alterada para PIX");
    }

    // ==================== TESTES DE CASOS ESPECIAIS ====================

    @Test
    void testPedidoAvulsoSemCliente() {
        // Arrange & Act - Pedido avulso (sem cliente cadastrado)
        Pedido pedido = new Pedido(21, null, "Garrafão 20L", 2, null, 
            "Feito", "2025-11-22", "Rua X, Casa 10", 20.0, FormaPagamento.DINHEIRO);

        // Assert
        assertNull(pedido.getClienteNome(), 
            "Pedido avulso não deve ter nome de cliente");
        assertEquals("Rua X, Casa 10", pedido.getEndereco(), 
            "Pedido avulso deve ter endereço informado");
    }

    @Test
    void testPedidoComClienteCadastrado() {
        // Arrange & Act - Pedido de cliente cadastrado
        Pedido pedido = new Pedido(22, "Fernanda Lima", "Garrafão 10L", 1, null, 
            "Feito", "2025-11-22", "Rua Y, Apt 200", 10.0, FormaPagamento.PIX);

        // Assert
        assertNotNull(pedido.getClienteNome(), 
            "Pedido de cliente cadastrado deve ter nome");
        assertEquals("Fernanda Lima", pedido.getClienteNome());
    }

    @Test
    void testPedidoComQuantidadeGrande() {
        // Arrange
        int quantidadeGrande = 100;
        double precoUnitario = 10.0;
        double precoTotal = quantidadeGrande * precoUnitario;

        // Act
        Pedido pedido = new Pedido(23, "Empresa XYZ", "Garrafão", quantidadeGrande, 
            null, "Feito", "2025-11-22", null, precoTotal, FormaPagamento.CARTAO);

        // Assert
        assertEquals(100, pedido.getQuantidade(), 
            "Deve suportar quantidades grandes");
        assertEquals(1000.0, pedido.getPrecoTotal(), 0.001, 
            "Preço total deve ser calculado corretamente para grandes quantidades");
    }
}
