package com.distribuidora.update;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Cobre a comparação de versões, que é o ponto onde um erro passa despercebido:
 * o programa continua abrindo normalmente, só nunca mais avisa que existe
 * versão nova (ou avisa quando não devia).
 */
class AppVersionTest {

    @Test
    void versaoMaiorNoUltimoSegmentoEhMaisNova() {
        assertTrue(AppVersion.ehMaisNova("1.2.1", "1.2.0"));
    }

    @Test
    void dezEhMaiorQueNoveMesmoSendoMenorComoTexto() {
        // "1.10.0".compareTo("1.9.0") é negativo. Comparar como número é o que
        // impede a loja de ficar presa na 1.9.0 para sempre.
        assertTrue(AppVersion.ehMaisNova("1.10.0", "1.9.0"));
        assertTrue(AppVersion.comparar("1.10.0", "1.9.0") > 0);
    }

    @Test
    void versoesIguaisNaoSaoMaisNovas() {
        assertFalse(AppVersion.ehMaisNova("1.2.0", "1.2.0"));
        assertEquals(0, AppVersion.comparar("1.2.0", "1.2.0"));
    }

    @Test
    void versaoRemotaMaisAntigaNaoEhMaisNova() {
        assertFalse(AppVersion.ehMaisNova("1.1.9", "1.2.0"));
    }

    @Test
    void segmentoAusenteContaComoZero() {
        assertEquals(0, AppVersion.comparar("1.2", "1.2.0"));
        assertFalse(AppVersion.ehMaisNova("1.2", "1.2.0"));
        assertTrue(AppVersion.ehMaisNova("1.2.1", "1.2"));
    }

    @Test
    void versaoDeDesenvolvimentoNuncaDisparaAviso() {
        // Rodando pela IDE (1.1-SNAPSHOT) ou num build de teste do workflow
        // (0.0.0-dev.7) o aviso não pode aparecer.
        assertFalse(AppVersion.ehMaisNova("1.2.0", "1.1-SNAPSHOT"));
        assertFalse(AppVersion.ehMaisNova("1.2.0", "0.0.0-dev.7"));
        assertFalse(AppVersion.ehMaisNova("1.3.0-SNAPSHOT", "1.2.0"));
    }

    @Test
    void valorMalformadoNaoQuebraENaoAvisa() {
        assertFalse(AppVersion.ehMaisNova(null, "1.2.0"));
        assertFalse(AppVersion.ehMaisNova("1.2.0", null));
        assertFalse(AppVersion.ehMaisNova("", "1.2.0"));
        assertFalse(AppVersion.ehMaisNova("   ", "1.2.0"));
        assertFalse(AppVersion.ehMaisNova("<html>404</html>", "1.2.0"));
        assertFalse(AppVersion.ehMaisNova("v1.2.0", "1.2.0"));
        assertFalse(AppVersion.ehMaisNova("1.2.0", AppVersion.DESCONHECIDA));
    }

    @Test
    void formatoValidoAceitaSoNumerosComPonto() {
        assertTrue(AppVersion.ehFormatoValido("1"));
        assertTrue(AppVersion.ehFormatoValido("1.2"));
        assertTrue(AppVersion.ehFormatoValido("10.20.30"));
        assertTrue(AppVersion.ehFormatoValido(" 1.2.0 "), "espaços em volta devem ser tolerados");

        assertFalse(AppVersion.ehFormatoValido("1.2."));
        assertFalse(AppVersion.ehFormatoValido("1..2"));
        assertFalse(AppVersion.ehFormatoValido("1.2.0-SNAPSHOT"));
        assertFalse(AppVersion.ehFormatoValido("9999999999"), "número grande demais para caber em int");
    }

    @Test
    void leAVersaoDoAppProperties() {
        // O app.properties é gerado pelo Maven no build. Só verifica que veio
        // algo real; o número em si muda a cada release.
        String versao = AppVersion.atual();

        assertFalse(versao.isBlank());
        assertFalse(AppVersion.DESCONHECIDA.equals(versao),
                "esperava a versão vinda do app.properties gerado pelo Maven");
    }

    @Test
    void leABaseDeAtualizacaoDoAppProperties() {
        assertTrue(AppVersion.baseDeAtualizacao().startsWith("https://"),
                "a base de atualização precisa ser uma URL https");
    }
}
