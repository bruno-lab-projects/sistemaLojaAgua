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

    private static final String TELEFONE_PADRAO_INICIAL = "(71) 9";

    /**
     * Remove espaços laterais de um texto e garante retorno não nulo.
     *
     * @param texto Texto de entrada
     * @return Texto sem espaços laterais, ou string vazia quando nulo
     */
    public static String trimToEmpty(String texto) {
        return texto == null ? "" : texto.trim();
    }

    /**
     * Retorna apenas os dígitos de uma string.
     *
     * @param texto Texto de entrada
     * @return Apenas caracteres numéricos
     */
    public static String somenteDigitos(String texto) {
        if (texto == null || texto.isEmpty()) {
            return "";
        }
        return texto.replaceAll("[^0-9]", "");
    }

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

        String apenasNumeros = somenteDigitos(numeros);
        if (apenasNumeros.isEmpty()) {
            return "";
        }

        if (apenasNumeros.length() > 11) {
            apenasNumeros = apenasNumeros.substring(0, 11);
        }
        
        // Formata: (DD) 9 9999-9999
        StringBuilder formatted = new StringBuilder();
        int length = apenasNumeros.length();
        
        if (length <= 2) {
            // Apenas DDD: (71
            formatted.append("(").append(apenasNumeros);
        } else if (length == 3) {
            // DDD completo + primeiro dígito: (71) 9
            formatted.append("(").append(apenasNumeros.substring(0, 2))
                     .append(") ").append(apenasNumeros.substring(2));
        } else if (length <= 7) {
            // DDD + primeiros dígitos: (71) 9 1234
            formatted.append("(").append(apenasNumeros.substring(0, 2))
                     .append(") ").append(apenasNumeros.substring(2, 3))
                     .append(" ").append(apenasNumeros.substring(3));
        } else {
            // Formato completo: (71) 9 1234-5678
            formatted.append("(").append(apenasNumeros.substring(0, 2))
                     .append(") ").append(apenasNumeros.substring(2, 3))
                     .append(" ").append(apenasNumeros.substring(3, 7))
                     .append("-").append(apenasNumeros.substring(7));
        }
        
        return formatted.toString();
    }

    /**
     * Normaliza telefone para persistência no banco.
     * Aceita campo vazio, mas rejeita telefone parcial.
     *
     * @param telefone Texto digitado no campo de telefone
     * @return Telefone formatado no padrão do sistema, ou vazio quando não informado
     */
    public static String normalizarTelefoneParaPersistencia(String telefone) {
        String texto = trimToEmpty(telefone);
        if (texto.isEmpty() || TELEFONE_PADRAO_INICIAL.equals(texto)) {
            return "";
        }

        String digitos = somenteDigitos(texto);
        if (digitos.length() != 11) {
            throw new IllegalArgumentException("O número de telefone informado está incompleto.");
        }

        return formatarTelefone(digitos);
    }

    /**
     * Converte preço digitado no formulário em double.
     * Aceita vírgula ou ponto como separador decimal.
     *
     * @param valorDigitado Valor digitado pelo usuário
     * @return Valor numérico convertido
     */
    public static double parsePreco(String valorDigitado) {
        String normalizado = trimToEmpty(valorDigitado).replace(",", ".");
        if (normalizado.isEmpty()) {
            throw new IllegalArgumentException("Preço inválido. Use apenas números (ex: 19,50).");
        }

        try {
            return Double.parseDouble(normalizado);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Preço inválido. Use apenas números (ex: 19,50).", e);
        }
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
        NumberFormat formatador = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));
        return formatador.format(valor);
    }
}
