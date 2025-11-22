package com.distribuidora.util;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.function.UnaryOperator;

/**
 * Classe utilitária para formatação de dados.
 * Contém métodos estáticos para formatar nomes, telefones e valores monetários.
 */
public class FormatUtils {

    /**
     * Capitaliza cada palavra de um nome.
     * Exemplo: "BRUNO SANTOS" ou "bruno santos" → "Bruno Santos"
     * 
     * @param nome Nome a ser capitalizado
     * @return Nome com cada palavra capitalizada, ou null/vazio se entrada for null/vazia
     */
    public static String capitalizarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            return nome;
        }
        
        String[] palavras = nome.trim().toLowerCase().split("\\s+");
        StringBuilder resultado = new StringBuilder();
        
        for (String palavra : palavras) {
            if (!palavra.isEmpty()) {
                // Capitaliza a primeira letra e mantém o resto em minúsculo
                resultado.append(Character.toUpperCase(palavra.charAt(0)))
                         .append(palavra.substring(1))
                         .append(" ");
            }
        }
        
        return resultado.toString().trim();
    }

    /**
     * Formata um número de telefone no padrão brasileiro.
     * Formato: (DD) 9 9999-9999
     * 
     * @param numeros String contendo apenas os dígitos do telefone
     * @return Telefone formatado
     */
    public static String formatarTelefone(String numeros) {
        if (numeros == null || numeros.isEmpty()) {
            return numeros;
        }
        
        // Formata: (DD) 9 9999-9999
        StringBuilder formatted = new StringBuilder();
        int length = numeros.length();
        
        if (length <= 2) {
            // Apenas DDD: (71
            formatted.append("(").append(numeros);
        } else if (length == 3) {
            // DDD completo + primeiro dígito: (71) 9
            formatted.append("(").append(numeros.substring(0, 2))
                     .append(") ").append(numeros.substring(2));
        } else if (length <= 7) {
            // DDD + primeiros dígitos: (71) 9 1234
            formatted.append("(").append(numeros.substring(0, 2))
                     .append(") ").append(numeros.substring(2, 3))
                     .append(" ").append(numeros.substring(3));
        } else {
            // Formato completo: (71) 9 1234-5678
            formatted.append("(").append(numeros.substring(0, 2))
                     .append(") ").append(numeros.substring(2, 3))
                     .append(" ").append(numeros.substring(3, 7))
                     .append("-").append(numeros.substring(7));
        }
        
        return formatted.toString();
    }

    /**
     * Configura uma máscara de telefone brasileiro em um TextField.
     * Formata automaticamente enquanto o usuário digita.
     * Define o padrão inicial como "(71) 9".
     * 
     * @param campo TextField onde a máscara será aplicada
     */
    public static void configurarMascaraTelefone(TextField campo) {
        // Cria um TextFormatter que formata automaticamente
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            
            // Remove tudo que não é dígito
            String apenasNumeros = newText.replaceAll("[^0-9]", "");
            
            // Se apagou tudo, permite campo vazio
            if (apenasNumeros.isEmpty()) {
                change.setText("");
                change.setRange(0, change.getControlText().length());
                return change;
            }
            
            // Limita a 11 dígitos
            if (apenasNumeros.length() > 11) {
                return null; // Rejeita a mudança
            }
            
            // Formata o telefone
            String formatted = formatarTelefone(apenasNumeros);
            
            // Atualiza o change com o texto formatado
            change.setText(formatted);
            change.setRange(0, change.getControlText().length());
            
            // Ajusta a posição do cursor
            int newCaretPos = formatted.length();
            change.setCaretPosition(newCaretPos);
            change.setAnchor(newCaretPos);
            
            return change;
        };
        
        TextFormatter<String> textFormatter = new TextFormatter<>(filter);
        campo.setTextFormatter(textFormatter);
        
        // Define o texto inicial como "(71) 9"
        campo.setText("(71) 9");
        
        // Ao focar no campo vazio, preenche com o padrão (71) 9
        campo.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused && campo.getText().isEmpty()) {
                campo.setText("(71) 9");
                campo.positionCaret(campo.getText().length());
            }
        });
    }

    /**
     * Formata um valor monetário no padrão brasileiro (R$).
     * Exemplo: 123.45 → "R$ 123,45"
     * 
     * @param valor Valor a ser formatado
     * @return String formatada com símbolo da moeda e separadores brasileiros
     */
    public static String formatarMoeda(double valor) {
        NumberFormat formatador = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        return formatador.format(valor);
    }
}
