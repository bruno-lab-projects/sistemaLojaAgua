package com.distribuidora.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe FormatUtils.
 * Verifica a corretude dos métodos de formatação de moeda, nomes e telefones.
 */
class FormatUtilsTest {

    // ==================== TESTES DE FORMATAÇÃO DE MOEDA ====================

    @Test
    void testFormatarMoeda_ValorSimples() {
        // Testa se 10.0 é formatado corretamente no padrão brasileiro
        String resultado = FormatUtils.formatarMoeda(10.0);
        // Nota: NumberFormat usa espaço não quebrável (U+00A0) entre R$ e o valor
        assertEquals("R$\u00A010,00", resultado, "10.0 deve ser formatado como 'R$ 10,00'");
    }

    @Test
    void testFormatarMoeda_ValorZero() {
        // Testa se 0.0 é formatado corretamente
        String resultado = FormatUtils.formatarMoeda(0.0);
        assertEquals("R$\u00A00,00", resultado, "0.0 deve ser formatado como 'R$ 0,00'");
    }

    @Test
    void testFormatarMoeda_ValorComMilhar() {
        // Testa se valores com milhares usam ponto como separador
        String resultado = FormatUtils.formatarMoeda(1000.50);
        assertEquals("R$\u00A01.000,50", resultado, "1000.50 deve ser formatado como 'R$ 1.000,50'");
    }

    @Test
    void testFormatarMoeda_ValorNegativo() {
        // Testa formatação de valor negativo
        String resultado = FormatUtils.formatarMoeda(-50.99);
        assertEquals("-R$\u00A050,99", resultado, "Valores negativos devem ter o sinal antes do símbolo da moeda");
    }

    @Test
    void testFormatarMoeda_ValorGrande() {
        // Testa formatação de valores grandes com múltiplos separadores de milhar
        String resultado = FormatUtils.formatarMoeda(1234567.89);
        assertEquals("R$\u00A01.234.567,89", resultado, "Valores grandes devem ter separadores de milhar corretos");
    }

    // ==================== TESTES DE CAPITALIZAÇÃO DE NOME ====================

    @Test
    void testCapitalizarNome_NomeMinusculo() {
        // Testa capitalização de nome todo em minúsculas
        String resultado = FormatUtils.capitalizarNome("joao silva");
        assertEquals("Joao Silva", resultado, "'joao silva' deve virar 'Joao Silva'");
    }

    @Test
    void testCapitalizarNome_NomeMaiusculo() {
        // Testa capitalização de nome todo em maiúsculas
        String resultado = FormatUtils.capitalizarNome("MARIA SOUZA");
        assertEquals("Maria Souza", resultado, "'MARIA SOUZA' deve virar 'Maria Souza'");
    }

    @Test
    void testCapitalizarNome_NomeMisto() {
        // Testa capitalização de nome com capitalização mista
        String resultado = FormatUtils.capitalizarNome("cArLos EdUaRdo");
        assertEquals("Carlos Eduardo", resultado, "Nomes mistos devem ser normalizados corretamente");
    }

    @Test
    void testCapitalizarNome_NomeComEspacosExtras() {
        // Testa se remove espaços extras
        String resultado = FormatUtils.capitalizarNome("  ana   maria  ");
        assertEquals("Ana Maria", resultado, "Deve remover espaços extras e capitalizar corretamente");
    }

    @Test
    void testCapitalizarNome_NomeNull() {
        // Testa comportamento com entrada null
        String resultado = FormatUtils.capitalizarNome(null);
        assertNull(resultado, "null deve retornar null");
    }

    @Test
    void testCapitalizarNome_NomeVazio() {
        // Testa comportamento com string vazia
        String resultado = FormatUtils.capitalizarNome("");
        assertEquals("", resultado, "String vazia deve retornar vazia");
    }

    @Test
    void testCapitalizarNome_NomeApenasBrancos() {
        // Testa comportamento com string contendo apenas espaços
        String resultado = FormatUtils.capitalizarNome("   ");
        assertEquals("   ", resultado, "String com apenas espaços deve ser preservada");
    }

    @Test
    void testCapitalizarNome_NomeUnico() {
        // Testa capitalização de nome único (sem sobrenome)
        String resultado = FormatUtils.capitalizarNome("pedro");
        assertEquals("Pedro", resultado, "Nome único deve ser capitalizado corretamente");
    }

    // ==================== TESTES DE FORMATAÇÃO DE TELEFONE ====================

    @Test
    void testFormatarTelefone_NumeroCompleto() {
        // Testa formatação de número completo (11 dígitos)
        String resultado = FormatUtils.formatarTelefone("71912345678");
        assertEquals("(71) 9 1234-5678", resultado, 
            "Número completo deve ser formatado como '(71) 9 1234-5678'");
    }

    @Test
    void testFormatarTelefone_ApenasDDD() {
        // Testa formatação com apenas DDD
        String resultado = FormatUtils.formatarTelefone("71");
        assertEquals("(71", resultado, "Apenas DDD deve começar a formatação: '(71'");
    }

    @Test
    void testFormatarTelefone_DDDMaisUmDigito() {
        // Testa formatação com DDD + primeiro dígito
        String resultado = FormatUtils.formatarTelefone("719");
        assertEquals("(71) 9", resultado, "DDD + 1 dígito deve formatar como '(71) 9'");
    }

    @Test
    void testFormatarTelefone_DDDMaisQuatroDigitos() {
        // Testa formatação parcial (DDD + 4 dígitos)
        String resultado = FormatUtils.formatarTelefone("7191234");
        assertEquals("(71) 9 1234", resultado, 
            "DDD + 5 dígitos deve formatar como '(71) 9 1234'");
    }

    @Test
    void testFormatarTelefone_NumeroNull() {
        // Testa comportamento com entrada null
        String resultado = FormatUtils.formatarTelefone(null);
        assertNull(resultado, "null deve retornar null");
    }

    @Test
    void testFormatarTelefone_NumeroVazio() {
        // Testa comportamento com string vazia
        String resultado = FormatUtils.formatarTelefone("");
        assertEquals("", resultado, "String vazia deve retornar vazia");
    }

    @Test
    void testFormatarTelefone_UmDigito() {
        // Testa formatação com apenas 1 dígito
        String resultado = FormatUtils.formatarTelefone("7");
        assertEquals("(7", resultado, "Um dígito deve começar a formatação do DDD");
    }

    @Test
    void testFormatarTelefone_NumeroComOitoDigitos() {
        // Testa formatação com 8 dígitos (início do hífen)
        String resultado = FormatUtils.formatarTelefone("71912345");
        assertEquals("(71) 9 1234-5", resultado, 
            "8 dígitos devem formatar com início do hífen: '(71) 9 1234-5'");
    }

    // ==================== TESTES DE NORMALIZAÇÃO DE ENTRADA ====================

    @Test
    void testNormalizarTelefoneParaPersistencia_ValorVazio() {
        String resultado = FormatUtils.normalizarTelefoneParaPersistencia("");
        assertEquals("", resultado, "Campo de telefone vazio deve ser persistido como vazio");
    }

    @Test
    void testNormalizarTelefoneParaPersistencia_PadraoInicial() {
        String resultado = FormatUtils.normalizarTelefoneParaPersistencia("(71) 9");
        assertEquals("", resultado, "Máscara inicial não deve ser salva como telefone");
    }

    @Test
    void testNormalizarTelefoneParaPersistencia_NumeroCompleto() {
        String resultado = FormatUtils.normalizarTelefoneParaPersistencia("71912345678");
        assertEquals("(71) 9 1234-5678", resultado, "Telefone completo deve ser normalizado no formato padrão");
    }

    @Test
    void testNormalizarTelefoneParaPersistencia_Incompleto() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> FormatUtils.normalizarTelefoneParaPersistencia("7191234")
        );
        assertEquals("O número de telefone informado está incompleto.", ex.getMessage());
    }

    @Test
    void testSomenteDigitos_TextoComMascara() {
        String resultado = FormatUtils.somenteDigitos("(71) 9 1234-5678");
        assertEquals("71912345678", resultado, "Deve extrair apenas os dígitos do telefone");
    }

    @Test
    void testParsePreco_ComVirgula() {
        double resultado = FormatUtils.parsePreco("19,50");
        assertEquals(19.50, resultado, 0.0001, "Deve aceitar vírgula como separador decimal");
    }

    @Test
    void testParsePreco_ComPonto() {
        double resultado = FormatUtils.parsePreco("19.50");
        assertEquals(19.50, resultado, 0.0001, "Deve aceitar ponto como separador decimal");
    }

    @Test
    void testParsePreco_Invalido() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> FormatUtils.parsePreco("abc")
        );
        assertEquals("Preço inválido. Use apenas números (ex: 19,50).", ex.getMessage());
    }
}
