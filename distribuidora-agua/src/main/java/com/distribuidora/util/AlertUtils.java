package com.distribuidora.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

/**
 * Classe utilitária para exibição de diálogos de alerta.
 * Simplifica a criação de alertas padrão de erro, sucesso e confirmação.
 */
public class AlertUtils {

    /**
     * Exibe um alerta de erro.
     * 
     * @param titulo Título da janela de alerta
     * @param mensagem Mensagem a ser exibida
     */
    public static void mostrarErro(String titulo, String mensagem) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    /**
     * Exibe um alerta de sucesso (informação).
     * 
     * @param titulo Título da janela de alerta
     * @param mensagem Mensagem a ser exibida
     */
    public static void mostrarSucesso(String titulo, String mensagem) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    /**
     * Exibe um alerta de confirmação com botões OK e Cancelar.
     * 
     * @param titulo Título da janela de alerta
     * @param mensagem Mensagem a ser exibida
     * @return true se o usuário clicou em OK, false se clicou em Cancelar ou fechou o diálogo
     */
    public static boolean mostrarConfirmacao(String titulo, String mensagem) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        
        return alert.showAndWait()
                   .filter(response -> response == ButtonType.OK)
                   .isPresent();
    }

    /**
     * Exibe um alerta de aviso.
     * 
     * @param titulo Título da janela de alerta
     * @param mensagem Mensagem a ser exibida
     */
    public static void mostrarAviso(String titulo, String mensagem) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}
