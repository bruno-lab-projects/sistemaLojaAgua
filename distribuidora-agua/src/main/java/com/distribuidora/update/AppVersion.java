package com.distribuidora.update;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Descobre qual versão do sistema está rodando e sabe comparar duas versões.
 *
 * É lógica pura — não acessa rede, disco nem interface — e por isso é a parte
 * mais testável do mecanismo de atualização. As classes que fazem trabalho
 * "sujo" ({@link UpdateChecker}, {@link UpdateDownloader}) dependem dela, e não
 * o contrário. Mesmo desenho de {@code DbBackground.executarNucleo}: o núcleo
 * decidível separado da casca.
 */
public final class AppVersion {

    private static final Logger LOG = Logger.getLogger(AppVersion.class.getName());

    /** Arquivo gerado pelo Maven no build, com a versão já substituída. */
    static final String RECURSO = "/app.properties";

    static final String CHAVE_VERSAO = "app.version";
    static final String CHAVE_BASE = "app.update.base";

    /**
     * Devolvido quando o app.properties não pôde ser lido. Não é um número de
     * versão válido de propósito: assim {@link #ehMaisNova} sempre responde
     * {@code false} e o sistema simplesmente não oferece atualização, em vez de
     * achar que está desatualizado e insistir num download.
     */
    public static final String DESCONHECIDA = "desconhecida";

    private AppVersion() {
    }

    /**
     * A versão desta instalação, lida do app.properties embutido no JAR.
     * Rodando pela IDE devolve a versão de desenvolvimento (ex.: 1.1-SNAPSHOT).
     */
    public static String atual() {
        return lerPropriedade(CHAVE_VERSAO, DESCONHECIDA);
    }

    /** A URL base de onde os arquivos de atualização são baixados. */
    public static String baseDeAtualizacao() {
        return lerPropriedade(CHAVE_BASE, "");
    }

    private static String lerPropriedade(String chave, String padrao) {
        try (InputStream entrada = AppVersion.class.getResourceAsStream(RECURSO)) {
            if (entrada == null) {
                LOG.warning("Arquivo " + RECURSO + " não encontrado no JAR");
                return padrao;
            }
            Properties propriedades = new Properties();
            propriedades.load(entrada);
            String valor = propriedades.getProperty(chave);
            return (valor == null || valor.isBlank()) ? padrao : valor.trim();
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Falha ao ler " + RECURSO, e);
            return padrao;
        }
    }

    /**
     * Compara duas versões segmento a segmento, como <b>número</b>.
     *
     * Comparar como texto seria um bug silencioso: {@code "1.10.0".compareTo("1.9.0")}
     * é negativo, porque o caractere '1' vem antes do '9'. Ou seja, a loja
     * ficaria presa na 1.9.0 sem nunca ver a 1.10.0.
     *
     * Segmentos que faltam contam como zero, então {@code 1.2} e {@code 1.2.0}
     * são iguais.
     *
     * @return negativo se {@code a < b}, zero se iguais, positivo se {@code a > b}
     * @throws IllegalArgumentException se alguma das versões não for do formato
     *         numérico (use {@link #ehFormatoValido} antes, ou {@link #ehMaisNova},
     *         que já trata isso)
     */
    static int comparar(String a, String b) {
        int[] segmentosA = segmentos(a);
        int[] segmentosB = segmentos(b);

        int total = Math.max(segmentosA.length, segmentosB.length);
        for (int i = 0; i < total; i++) {
            int valorA = i < segmentosA.length ? segmentosA[i] : 0;
            int valorB = i < segmentosB.length ? segmentosB[i] : 0;
            if (valorA != valorB) {
                return Integer.compare(valorA, valorB);
            }
        }
        return 0;
    }

    private static int[] segmentos(String versao) {
        if (!ehFormatoValido(versao)) {
            throw new IllegalArgumentException("Versão em formato inválido: " + versao);
        }
        String[] partes = versao.trim().split("\\.");
        int[] numeros = new int[partes.length];
        for (int i = 0; i < partes.length; i++) {
            // Cabe em int com folga: mesmo um segmento de 9 dígitos é bem menor
            // que Integer.MAX_VALUE. ehFormatoValido já limitou o tamanho.
            numeros[i] = Integer.parseInt(partes[i]);
        }
        return numeros;
    }

    /**
     * Verdadeiro só para versões formadas por números separados por ponto
     * (1, 1.2, 1.2.3...). Qualquer sufixo — {@code -SNAPSHOT}, {@code -dev.7},
     * {@code -rc1} — é considerado inválido de propósito: são builds de
     * desenvolvimento ou de teste, que não devem disparar aviso de atualização
     * na tela da loja.
     */
    static boolean ehFormatoValido(String versao) {
        if (versao == null || versao.isBlank()) {
            return false;
        }
        // Até 9 dígitos por segmento garante que o parseInt nunca estoura.
        return versao.trim().matches("\\d{1,9}(\\.\\d{1,9})*");
    }

    /**
     * Decide se vale avisar o usuário de que existe versão nova.
     *
     * Na dúvida, responde {@code false}. Um "não" errado só adia a atualização;
     * um "sim" errado enche a tela da loja com um aviso que não vai embora.
     *
     * @param remota versão publicada (lida do version.txt do release)
     * @param local  versão que está rodando aqui
     */
    public static boolean ehMaisNova(String remota, String local) {
        if (!ehFormatoValido(remota) || !ehFormatoValido(local)) {
            return false;
        }
        return comparar(remota, local) > 0;
    }
}
