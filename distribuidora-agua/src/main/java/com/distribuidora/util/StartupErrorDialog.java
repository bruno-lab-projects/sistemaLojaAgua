package com.distribuidora.util;

import java.awt.GraphicsEnvironment;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Janela de erro para falhas ocorridas antes de o JavaFX subir.
 *
 * Usa Swing (JOptionPane) porque, no main, o toolkit do JavaFX ainda não foi
 * inicializado e um Alert lançaria ExceptionInInitializerError. O INICIAR.bat
 * chama javaw, que não tem console: sem esta janela, a falha é invisível.
 */
public final class StartupErrorDialog {

    private static final Logger LOG = Logger.getLogger(StartupErrorDialog.class.getName());

    private StartupErrorDialog() {
    }

    public static void mostrar(String titulo, String mensagem) {
        String tituloSeguro = titulo == null ? "Erro" : titulo;
        String mensagemSegura = mensagem == null ? "Ocorreu um erro inesperado." : mensagem;

        if (GraphicsEnvironment.isHeadless()) {
            System.err.println(tituloSeguro + ": " + mensagemSegura);
            return;
        }

        try {
            SwingUtilities.invokeAndWait(() ->
                    JOptionPane.showMessageDialog(null, mensagemSegura, tituloSeguro, JOptionPane.ERROR_MESSAGE));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println(tituloSeguro + ": " + mensagemSegura);
        } catch (InvocationTargetException | RuntimeException | Error e) {
            LOG.log(Level.WARNING, "Não foi possível exibir a janela de erro", e);
            System.err.println(tituloSeguro + ": " + mensagemSegura);
        }
    }
}
