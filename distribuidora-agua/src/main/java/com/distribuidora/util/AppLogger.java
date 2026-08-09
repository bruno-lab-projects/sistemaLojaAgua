package com.distribuidora.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Configura o java.util.logging para gravar em arquivo com rotação.
 *
 * Sem log, uma falha na máquina da loja é um relato por telefone. Esta classe é
 * pré-requisito da atualização remota: um update com problema precisa deixar rastro.
 *
 * Nenhum método aqui lança exceção. Falha de logging nunca pode derrubar o app.
 */
public final class AppLogger {

    /** Logger raiz da aplicação. Todo Logger de com.distribuidora herda dele. */
    private static final String ROOT_LOGGER_NAME = "com.distribuidora";

    /** 1 MB por arquivo, 5 arquivos em rodízio. Máximo de 5 MB em disco. */
    private static final int TAMANHO_MAXIMO_BYTES = 1_000_000;
    private static final int QUANTIDADE_DE_ARQUIVOS = 5;

    private static boolean inicializado = false;
    private static Path diretorioDeLogs = null;

    private AppLogger() {
    }

    /**
     * Cria o diretório de logs e instala o FileHandler no logger raiz da aplicação.
     * Chamar de novo com o mesmo diretório não tem efeito; chamar com um diretório
     * diferente reinstala o handler nesse novo diretório.
     */
    public static synchronized void initialize(Path diretorio) {
        if (inicializado && diretorio.equals(diretorioDeLogs)) {
            return;
        }

        try {
            Files.createDirectories(diretorio);

            // %g é o número da geração no rodízio, %u desambigua se houver duas JVMs.
            String padrao = diretorio.resolve("app-%g.log").toString();

            FileHandler handler = new FileHandler(padrao, TAMANHO_MAXIMO_BYTES, QUANTIDADE_DE_ARQUIVOS, true);
            handler.setFormatter(new FormatadorDeLinha());
            handler.setLevel(Level.ALL);

            Logger raiz = Logger.getLogger(ROOT_LOGGER_NAME);
            for (Handler existente : raiz.getHandlers()) {
                raiz.removeHandler(existente);
            }
            raiz.addHandler(handler);
            raiz.setLevel(Level.INFO);
            raiz.setUseParentHandlers(true); // mantém o console durante o desenvolvimento

            diretorioDeLogs = diretorio;
            inicializado = true;

            raiz.info("Logging inicializado em " + diretorio);
        } catch (IOException | RuntimeException e) {
            // Sem log em arquivo o app ainda funciona. Só avisa no console e segue.
            System.err.println("Não foi possível inicializar o arquivo de log: " + e);
        }
    }

    public static Logger get(Class<?> classe) {
        return Logger.getLogger(classe.getName());
    }

    public static Path getLogDirectory() {
        return diretorioDeLogs;
    }

    /** Uma linha por evento, com a stack trace anexada quando houver exceção. */
    private static final class FormatadorDeLinha extends Formatter {

        private static final java.time.format.DateTimeFormatter DATA =
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        @Override
        public String format(LogRecord registro) {
            StringBuilder saida = new StringBuilder();
            saida.append(DATA.format(java.time.LocalDateTime.ofInstant(
                            registro.getInstant(), java.time.ZoneId.systemDefault())))
                    .append(" [").append(registro.getLevel().getName()).append("] ")
                    .append(registro.getLoggerName()).append(" - ")
                    .append(formatMessage(registro))
                    .append(System.lineSeparator());

            if (registro.getThrown() != null) {
                java.io.StringWriter texto = new java.io.StringWriter();
                registro.getThrown().printStackTrace(new java.io.PrintWriter(texto));
                saida.append(texto).append(System.lineSeparator());
            }
            return saida.toString();
        }
    }
}
