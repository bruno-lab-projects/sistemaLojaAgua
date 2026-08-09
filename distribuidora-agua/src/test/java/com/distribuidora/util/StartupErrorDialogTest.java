package com.distribuidora.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * O Surefire roda sem display. O teste garante que, em ambiente headless,
 * a chamada degrada para o console em vez de lançar HeadlessException,
 * que é exatamente o que aconteceria numa máquina Windows sem sessão gráfica.
 */
class StartupErrorDialogTest {

    @Test
    void naoLancaEmAmbienteHeadless() {
        System.setProperty("java.awt.headless", "true");

        assertDoesNotThrow(() ->
                StartupErrorDialog.mostrar("Erro de teste", "Mensagem de teste"));
    }

    @Test
    void naoLancaComMensagemNula() {
        System.setProperty("java.awt.headless", "true");

        assertDoesNotThrow(() -> StartupErrorDialog.mostrar(null, null));
    }
}
