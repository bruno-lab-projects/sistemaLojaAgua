package com.distribuidora.update;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Usa uma {@link UpdateChecker.Fonte} falsa no lugar da rede: o build não pode
 * depender de internet, e assim dá para forçar respostas que seriam difíceis de
 * reproduzir de verdade (página de erro, conexão caindo, arquivo vazio).
 */
class UpdateCheckerTest {

    private static final String BASE = "https://exemplo.invalido/download";

    private final List<LogRecord> registrosCapturados = new ArrayList<>();
    private Logger logger;
    private Handler handlerDeTeste;
    private Level nivelOriginal;

    @BeforeEach
    void capturarLogs() {
        logger = Logger.getLogger(UpdateChecker.class.getName());
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

    private UpdateChecker checkerRespondendo(String conteudo) {
        return new UpdateChecker(BASE, endereco -> conteudo);
    }

    @Test
    void avisaQuandoAVersaoPublicadaEhMaisNova() {
        Optional<String> encontrada = checkerRespondendo("1.3.0").versaoDisponivel("1.2.0");

        assertEquals(Optional.of("1.3.0"), encontrada);
    }

    @Test
    void naoAvisaQuandoAVersaoPublicadaEhIgual() {
        assertEquals(Optional.empty(), checkerRespondendo("1.2.0").versaoDisponivel("1.2.0"));
    }

    @Test
    void naoAvisaQuandoAVersaoPublicadaEhMaisAntiga() {
        assertEquals(Optional.empty(), checkerRespondendo("1.1.0").versaoDisponivel("1.2.0"));
    }

    @Test
    void falhaDeRedeEhSilenciosaENaoAvisa() {
        UpdateChecker checker = new UpdateChecker(BASE, endereco -> {
            throw new IOException("sem conexão");
        });

        assertEquals(Optional.empty(), checker.versaoDisponivel("1.2.0"));
        assertFalse(registrosCapturados.stream().anyMatch(r -> r.getLevel().intValue() >= Level.WARNING.intValue()),
                "loja sem internet não pode gerar aviso nem erro, só log fino");
        assertTrue(registrosCapturados.stream().anyMatch(r -> r.getLevel() == Level.FINE),
                "a falha ainda precisa deixar rastro no log para diagnóstico");
    }

    @Test
    void erroInesperadoDaRedeTambemEhSilencioso() {
        // O HttpClient pode estourar RuntimeException (URL malformada, por
        // exemplo). Isso não pode derrubar a verificação nem chegar na tela.
        UpdateChecker checker = new UpdateChecker(BASE, endereco -> {
            throw new IllegalArgumentException("URL inválida");
        });

        assertEquals(Optional.empty(), checker.versaoDisponivel("1.2.0"));
    }

    @Test
    void respostaQueNaoEhUmaVersaoNaoAvisa() {
        assertEquals(Optional.empty(), checkerRespondendo("<html>404 Not Found</html>").versaoDisponivel("1.2.0"));
        assertEquals(Optional.empty(), checkerRespondendo("").versaoDisponivel("1.2.0"));
        assertEquals(Optional.empty(), checkerRespondendo("v1.3.0").versaoDisponivel("1.2.0"));
    }

    @Test
    void ignoraEspacosEQuebraDeLinhaDoArquivo() {
        // O `echo "$APP_VERSION" > version.txt` do workflow deixa \n no final.
        assertEquals(Optional.of("1.3.0"), checkerRespondendo("1.3.0\n").versaoDisponivel("1.2.0"));
        assertEquals(Optional.of("1.3.0"), checkerRespondendo("  1.3.0  \r\n").versaoDisponivel("1.2.0"));
    }

    @Test
    void naoTentaVerificarSemUrlBaseConfigurada() {
        UpdateChecker checker = new UpdateChecker("", endereco -> {
            throw new AssertionError("não deveria acessar a rede sem base configurada");
        });

        assertEquals(Optional.empty(), checker.versaoDisponivel("1.2.0"));
    }

    @Test
    void versaoLocalDeDesenvolvimentoNaoDisparaAviso() {
        assertEquals(Optional.empty(), checkerRespondendo("1.3.0").versaoDisponivel("1.1-SNAPSHOT"));
    }

    @Test
    void montaAUrlDoArquivoTolerandoBarraSobrando() {
        assertEquals(URI.create(BASE + "/version.txt"), UpdateChecker.urlDe(BASE, "version.txt"));
        assertEquals(URI.create(BASE + "/version.txt"), UpdateChecker.urlDe(BASE + "/", "version.txt"));
    }

    @Test
    void systemPropertySobrescreveABaseDoAppProperties() {
        String anterior = System.getProperty(UpdateChecker.BASE_PROPERTY);
        try {
            System.setProperty(UpdateChecker.BASE_PROPERTY, "http://localhost:8000");
            assertEquals("http://localhost:8000", UpdateChecker.baseConfigurada());
        } finally {
            if (anterior == null) {
                System.clearProperty(UpdateChecker.BASE_PROPERTY);
            } else {
                System.setProperty(UpdateChecker.BASE_PROPERTY, anterior);
            }
        }

        assertTrue(UpdateChecker.baseConfigurada().startsWith("https://"),
                "sem a property, vale a base do app.properties");
    }
}
