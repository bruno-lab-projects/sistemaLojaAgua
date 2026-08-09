package com.distribuidora.util;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.Cursor;

/**
 * Executa trabalho de banco fora da JavaFX Application Thread.
 *
 * Sem isso, toda consulta bloqueia o desenho da janela. Hoje passa despercebido
 * porque o banco é pequeno; com dois ou três anos de pedidos, o dashboard
 * congela a janela por segundos a cada abertura.
 *
 * A lógica de verdade (executar o trabalho, capturar falha, logar e entregar o
 * resultado ou o erro) mora em {@link #executarNucleo}, um método simples sem
 * nenhuma dependência de UI. É por isso que dá para testá-lo sem o toolkit
 * JavaFX e sem reflection. O {@link Task} usado em {@link #executar} é só uma
 * casca fina que roda esse método fora da thread da UI e devolve o resultado
 * (ou o erro) na thread certa, usando o mecanismo pronto do JavaFX
 * (setOnSucceeded/setOnFailed já rodam via Platform.runLater internamente).
 */
public final class DbBackground {

    private static final Logger LOG = Logger.getLogger(DbBackground.class.getName());

    /**
     * Uma única thread, e daemon para não segurar o encerramento da JVM.
     * Serializar as consultas é desejável aqui: o SQLite é um arquivo único e
     * paralelizar só geraria SQLITE_BUSY.
     */
    private static final Executor EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "db-background");
        t.setDaemon(true);
        return t;
    });

    @FunctionalInterface
    public interface TrabalhoDeBanco<T> {
        T executar() throws Exception;
    }

    private DbBackground() {
    }

    /**
     * Executa o trabalho de banco e devolve o resultado. Se o trabalho lançar,
     * a falha é registrada no log (não fica silenciosa) e a mesma exceção é
     * repassada para quem chamou.
     *
     * Não usa Task, thread nem nada de JavaFX: é lógica pura, por isso pode ser
     * chamada direto pelos testes sem precisar do toolkit JavaFX.
     *
     * @param descricao usada no log, por exemplo "carregar dashboard"
     * @param trabalho  o acesso ao banco a executar
     */
    static <T> T executarNucleo(String descricao, TrabalhoDeBanco<T> trabalho) throws Exception {
        try {
            return trabalho.executar();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Falha ao " + descricao, e);
            throw e;
        }
    }

    /**
     * Roda o trabalho em background, desabilita os controles enquanto executa e
     * entrega o resultado na thread da UI.
     *
     * @param descricao          usada no log, por exemplo "carregar dashboard"
     * @param trabalho           o acesso ao banco, executado fora da thread da UI
     * @param aoConcluir         recebe o resultado, já na thread da UI
     * @param mensagemDeErro     texto exibido ao usuário em caso de falha, sem jargão técnico
     * @param controlesParaDesabilitar botões e campos travados durante a execução
     */
    public static <T> void executar(String descricao,
                                    TrabalhoDeBanco<T> trabalho,
                                    Consumer<T> aoConcluir,
                                    String mensagemDeErro,
                                    Node... controlesParaDesabilitar) {

        // Task aqui é só uma casca fina: o call() delega para executarNucleo, que
        // é onde mora toda a lógica testável (execução + log + propagação do erro).
        Task<T> tarefa = new Task<T>() {
            @Override
            protected T call() throws Exception {
                return executarNucleo(descricao, trabalho);
            }
        };

        alternarControles(controlesParaDesabilitar, true);

        tarefa.setOnSucceeded(evento -> {
            alternarControles(controlesParaDesabilitar, false);
            try {
                aoConcluir.accept(tarefa.getValue());
            } catch (RuntimeException e) {
                LOG.log(Level.SEVERE, "Falha ao aplicar o resultado de " + descricao + " na tela", e);
                AlertUtils.mostrarErro("Erro", mensagemDeErro);
            }
        });

        tarefa.setOnFailed(evento -> {
            // O log da falha já aconteceu dentro de executarNucleo; aqui só cuida da UI.
            alternarControles(controlesParaDesabilitar, false);
            AlertUtils.mostrarErro("Erro", mensagemDeErro);
        });

        EXECUTOR.execute(tarefa);
    }

    private static void alternarControles(Node[] controles, boolean desabilitar) {
        if (controles == null) {
            return;
        }
        for (Node controle : controles) {
            if (controle == null) {
                continue;
            }
            controle.setDisable(desabilitar);
            controle.setCursor(desabilitar ? Cursor.WAIT : Cursor.DEFAULT);
        }
    }
}
