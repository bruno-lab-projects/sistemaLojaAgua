package com.distribuidora.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testa DbBackground.executarNucleo diretamente, chamando o método de pacote
 * sem passar por Task nem por reflection. É lógica pura (executa o trabalho,
 * loga a falha se houver, devolve o resultado ou repassa a exceção), então roda
 * sem o toolkit JavaFX. O comportamento de UI (Task, Platform.runLater, controles
 * desabilitados) é verificado manualmente nas Tarefas 10 e 11.
 */
class DbBackgroundTest {

    private final List<LogRecord> registrosCapturados = new ArrayList<>();
    private Logger logger;
    private Handler handlerDeTeste;
    private Level nivelOriginal;

    /**
     * Anexa um Handler ao logger de DbBackground para capturar o que é
     * registrado, sem depender de nenhum arquivo em disco.
     */
    @BeforeEach
    void capturarLogs() {
        logger = Logger.getLogger(DbBackground.class.getName());
        nivelOriginal = logger.getLevel();
        logger.setLevel(Level.ALL);

        handlerDeTeste = new Handler() {
            @Override
            public void publish(LogRecord record) {
                registrosCapturados.add(record);
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };
        handlerDeTeste.setLevel(Level.ALL);
        logger.addHandler(handlerDeTeste);
    }

    @AfterEach
    void restaurarLogger() {
        logger.removeHandler(handlerDeTeste);
        logger.setLevel(nivelOriginal);
    }

    @Test
    void devolveOResultadoDoTrabalho() throws Exception {
        String resultado = DbBackground.executarNucleo("teste", () -> "resultado");

        assertEquals("resultado", resultado);
    }

    @Test
    void propagaAExcecaoDoTrabalhoERegistraNoLog() {
        SQLException falhaOriginal = new SQLException("banco indisponível");

        SQLException e = assertThrows(SQLException.class, () ->
                DbBackground.executarNucleo("consultar pedidos", () -> {
                    throw falhaOriginal;
                }));

        assertEquals("banco indisponível", e.getMessage());
        assertTrue(registrosCapturados.stream().anyMatch(registro ->
                        registro.getLevel() == Level.SEVERE && registro.getThrown() == falhaOriginal),
                "esperava um registro SEVERE com a exceção original no log");
    }

    @Test
    void aceitaTrabalhoQueRetornaNulo() throws Exception {
        Object resultado = DbBackground.executarNucleo("teste", () -> null);

        assertEquals(null, resultado);
    }
}
