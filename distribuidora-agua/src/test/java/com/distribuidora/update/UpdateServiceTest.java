package com.distribuidora.update;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Cobre a decisão do "Lembrar depois", que é a única lógica do UpdateService
 * que não depende do toolkit JavaFX. O resto da classe é casca de UI e
 * agendamento, verificado manualmente (mesma abordagem do DbBackgroundTest).
 */
class UpdateServiceTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 8, 9);

    @Test
    void adiadoNoMesmoDiaSuprimeOAviso() {
        assertTrue(UpdateService.estaAdiado("2026-08-09", HOJE));
    }

    @Test
    void adiadoOntemVoltaAAvisarHoje() {
        assertFalse(UpdateService.estaAdiado("2026-08-08", HOJE));
    }

    @Test
    void semRegistroDeAdiamentoOAvisoAparece() {
        assertFalse(UpdateService.estaAdiado(null, HOJE));
        assertFalse(UpdateService.estaAdiado("", HOJE));
        assertFalse(UpdateService.estaAdiado("   ", HOJE));
    }

    @Test
    void dataGravadaInvalidaNaoImpedeOAviso() {
        // Na dúvida, avisar: perder um aviso é pior que mostrar um a mais.
        assertFalse(UpdateService.estaAdiado("ontem", HOJE));
        assertFalse(UpdateService.estaAdiado("09/08/2026", HOJE));
    }
}
