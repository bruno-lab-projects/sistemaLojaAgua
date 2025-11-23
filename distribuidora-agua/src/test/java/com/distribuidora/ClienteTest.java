package com.distribuidora;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe Cliente.
 * Verifica a corretude do construtor, getters e properties do JavaFX.
 */
class ClienteTest {

    // ==================== TESTES DO CONSTRUTOR ====================

    @Test
    void testConstrutorCompleto() {
        // Arrange & Act - Cria um cliente com todos os parâmetros
        Cliente cliente = new Cliente(
            1,                          // id
            "João Silva",               // nome
            "(71) 9 1234-5678",        // telefone
            "Rua A, Ed. Sol, Apt 101", // endereco (completo)
            "Ed. Sol",                  // predioCasa
            "Apt 101",                  // numero
            "Cliente VIP",              // observacoes
            "Rua A"                     // enderecoRua (apenas logradouro)
        );

        // Assert - Verifica se todos os valores foram atribuídos corretamente
        assertEquals(1, cliente.getId(), "ID deve ser 1");
        assertEquals("João Silva", cliente.getNome(), "Nome deve ser 'João Silva'");
        assertEquals("(71) 9 1234-5678", cliente.getTelefone(), "Telefone deve estar formatado");
        assertEquals("Rua A, Ed. Sol, Apt 101", cliente.getEndereco(), "Endereço completo deve estar correto");
        assertEquals("Ed. Sol", cliente.getPredioCasa(), "Prédio/Casa deve ser 'Ed. Sol'");
        assertEquals("Apt 101", cliente.getNumero(), "Número deve ser 'Apt 101'");
        assertEquals("Cliente VIP", cliente.getObservacoes(), "Observações devem estar corretas");
        assertEquals("Rua A", cliente.getEnderecoRua(), "Endereço (rua) deve ser 'Rua A'");
    }

    @Test
    void testConstrutorComValoresNulos() {
        // Arrange & Act - Cria cliente com valores nulos (aceitos pelo sistema)
        Cliente cliente = new Cliente(
            2,
            "Maria Santos",
            null,      // telefone pode ser nulo
            null,      // endereco pode ser nulo
            null,      // predioCasa pode ser nulo
            null,      // numero pode ser nulo
            null,      // observacoes pode ser nulo
            null       // enderecoRua pode ser nulo
        );

        // Assert
        assertEquals(2, cliente.getId());
        assertEquals("Maria Santos", cliente.getNome());
        assertEquals("", cliente.getTelefone(), "Telefone nulo deve retornar string vazia");
        assertEquals("", cliente.getEndereco(), "Endereço nulo deve retornar string vazia");
        assertEquals("", cliente.getPredioCasa(), "Prédio/Casa nulo deve retornar string vazia");
        assertEquals("", cliente.getNumero(), "Número nulo deve retornar string vazia");
        assertEquals("", cliente.getObservacoes(), "Observações nulas devem retornar string vazia");
        assertEquals("", cliente.getEnderecoRua(), "Endereço (rua) nulo deve retornar string vazia");
    }

    @Test
    void testConstrutorComValoresVazios() {
        // Arrange & Act - Cria cliente com strings vazias
        Cliente cliente = new Cliente(
            3,
            "Pedro Costa",
            "",
            "",
            "",
            "",
            "",
            ""
        );

        // Assert
        assertEquals("", cliente.getTelefone(), "Telefone vazio deve retornar string vazia");
        assertEquals("", cliente.getEndereco(), "Endereço vazio deve retornar string vazia");
        assertEquals("", cliente.getPredioCasa(), "Prédio/Casa vazio deve retornar string vazia");
    }

    // ==================== TESTES DE GETTERS E SETTERS ====================

    @Test
    void testGetterPredioCasa() {
        // Arrange
        Cliente cliente = new Cliente(4, "Ana Lima", "(71) 9 8765-4321", 
            "Rua B", "Casa Amarela", "15", "Nenhuma", "Rua B");

        // Act & Assert
        assertEquals("Casa Amarela", cliente.getPredioCasa(), 
            "getPredioCasa() deve retornar 'Casa Amarela'");
    }

    @Test
    void testSetterPredioCasa() {
        // Arrange
        Cliente cliente = new Cliente(5, "Carlos Eduardo", null, null, "Ed. Antigo", null, null, null);

        // Act
        cliente.setPredioCasa("Ed. Novo");

        // Assert
        assertEquals("Ed. Novo", cliente.getPredioCasa(), 
            "setPredioCasa() deve atualizar o valor");
    }

    @Test
    void testSetterNumero() {
        // Arrange
        Cliente cliente = new Cliente(6, "Juliana Souza", null, null, null, "Apt 200", null, null);

        // Act
        cliente.setNumero("Apt 300");

        // Assert
        assertEquals("Apt 300", cliente.getNumero(), 
            "setNumero() deve atualizar o valor");
    }

    @Test
    void testSetterNome() {
        // Arrange
        Cliente cliente = new Cliente(7, "Roberto Silva", null, null, null, null, null, null);

        // Act
        cliente.setNome("Roberto Silva Junior");

        // Assert
        assertEquals("Roberto Silva Junior", cliente.getNome(), 
            "setNome() deve atualizar o nome do cliente");
    }

    // ==================== TESTES DE JAVAFX PROPERTIES ====================

    @Test
    void testPropertiesNaoSaoNulas() {
        // Arrange
        Cliente cliente = new Cliente(8, "Fernanda Costa", "(71) 9 5555-5555", 
            "Rua C, Ed. Lua, Apt 202", "Ed. Lua", "Apt 202", "Observação teste", "Rua C");

        // Act & Assert - Verifica que todas as properties foram inicializadas
        assertNotNull(cliente.idProperty(), "idProperty não deve ser null");
        assertNotNull(cliente.nomeProperty(), "nomeProperty não deve ser null");
        assertNotNull(cliente.telefoneProperty(), "telefoneProperty não deve ser null");
        assertNotNull(cliente.enderecoProperty(), "enderecoProperty não deve ser null");
        assertNotNull(cliente.enderecoRuaProperty(), "enderecoRuaProperty não deve ser null");
        assertNotNull(cliente.predioCasaProperty(), "predioCasaProperty não deve ser null");
        assertNotNull(cliente.numeroProperty(), "numeroProperty não deve ser null");
        assertNotNull(cliente.observacoesProperty(), "observacoesProperty não deve ser null");
    }

    @Test
    void testPropertiesSaoDoTipoCorreto() {
        // Arrange
        Cliente cliente = new Cliente(9, "Marcos Paulo", null, null, null, null, null, null);

        // Act & Assert - Verifica que as properties são dos tipos corretos
        assertTrue(cliente.idProperty() instanceof SimpleIntegerProperty, 
            "idProperty deve ser SimpleIntegerProperty");
        assertTrue(cliente.nomeProperty() instanceof SimpleStringProperty, 
            "nomeProperty deve ser SimpleStringProperty");
        assertTrue(cliente.telefoneProperty() instanceof SimpleStringProperty, 
            "telefoneProperty deve ser SimpleStringProperty");
        assertTrue(cliente.predioCasaProperty() instanceof SimpleStringProperty, 
            "predioCasaProperty deve ser SimpleStringProperty");
        assertTrue(cliente.numeroProperty() instanceof SimpleStringProperty, 
            "numeroProperty deve ser SimpleStringProperty");
    }

    @Test
    void testPropertyValueBinding() {
        // Arrange
        Cliente cliente = new Cliente(10, "Patricia Alves", null, null, "Ed. Central", null, null, null);

        // Act - Altera o valor através da property
        cliente.predioCasaProperty().set("Ed. Novo Central");

        // Assert - Verifica se o getter retorna o novo valor
        assertEquals("Ed. Novo Central", cliente.getPredioCasa(), 
            "Alteração via property deve refletir no getter");
    }

    @Test
    void testIdPropertyValue() {
        // Arrange
        Cliente cliente = new Cliente(100, "Teste ID", null, null, null, null, null, null);

        // Act & Assert
        assertEquals(100, cliente.idProperty().get(), 
            "idProperty.get() deve retornar o ID correto");
    }

    // ==================== TESTES DE CASOS ESPECIAIS ====================

    @Test
    void testClienteComEnderecoComplexo() {
        // Arrange & Act - Testa endereço com caracteres especiais
        Cliente cliente = new Cliente(
            11,
            "Amélia São José",
            "(71) 9 9999-9999",
            "Av. Principal, Ed. São João, Bl. A, Apt. 1502",
            "Ed. São João",
            "Bl. A - Apt. 1502",
            "Cliente com endereço complexo",
            "Av. Principal"
        );

        // Assert
        assertEquals("Ed. São João", cliente.getPredioCasa());
        assertEquals("Bl. A - Apt. 1502", cliente.getNumero());
        assertTrue(cliente.getEndereco().contains("Av. Principal"));
        assertTrue(cliente.getEndereco().contains("Ed. São João"));
    }

    @Test
    void testClienteComObservacoesLongas() {
        // Arrange
        String observacaoLonga = "Este é um cliente especial que sempre pede entrega " +
                                "no período da manhã e prefere garrafões de 20 litros. " +
                                "Atenção: sempre ligar antes de entregar.";
        
        // Act
        Cliente cliente = new Cliente(
            12, "Cliente Especial", null, null, null, null, observacaoLonga, null
        );

        // Assert
        assertEquals(observacaoLonga, cliente.getObservacoes(), 
            "Observações longas devem ser preservadas integralmente");
        assertTrue(cliente.getObservacoes().length() > 100, 
            "Observação deve ter mais de 100 caracteres");
    }
}
