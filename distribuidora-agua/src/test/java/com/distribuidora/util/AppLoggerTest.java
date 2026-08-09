package com.distribuidora.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppLoggerTest {

    @Test
    void gravaMensagemNoArquivoDeLog(@TempDir Path temp) throws IOException {
        AppLogger.initialize(temp);

        AppLogger.get(AppLoggerTest.class).log(Level.SEVERE, "marcador-de-teste-12345");

        String conteudo = lerTodosOsLogs(temp);
        assertTrue(conteudo.contains("marcador-de-teste-12345"),
                "O log deveria conter a mensagem gravada. Conteúdo: " + conteudo);
    }

    @Test
    void gravaStackTraceQuandoHaExcecao(@TempDir Path temp) throws IOException {
        AppLogger.initialize(temp);

        AppLogger.get(AppLoggerTest.class)
                .log(Level.SEVERE, "falha-simulada", new IllegalStateException("causa-raiz-98765"));

        String conteudo = lerTodosOsLogs(temp);
        assertTrue(conteudo.contains("causa-raiz-98765"),
                "O log deveria conter a stack trace. Conteúdo: " + conteudo);
    }

    @Test
    void inicializarDuasVezesNaoLancaENaoDuplicaHandler(@TempDir Path temp) {
        assertDoesNotThrow(() -> {
            AppLogger.initialize(temp);
            AppLogger.initialize(temp);
        });
    }

    @Test
    void naoLancaQuandoDiretorioEhInvalido() {
        assertDoesNotThrow(() -> AppLogger.initialize(Path.of("/proc/impossivel/de/criar")));
    }

    private String lerTodosOsLogs(Path diretorio) throws IOException {
        try (Stream<Path> arquivos = Files.list(diretorio)) {
            return arquivos.filter(p -> p.getFileName().toString().startsWith("app"))
                    .map(p -> {
                        try {
                            return Files.readString(p);
                        } catch (IOException e) {
                            return "";
                        }
                    })
                    .collect(Collectors.joining("\n"));
        }
    }
}
