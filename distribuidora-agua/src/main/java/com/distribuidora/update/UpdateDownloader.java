package com.distribuidora.update;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.CodeSource;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Optional;
import java.util.function.DoubleConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Baixa o JAR da versão nova e o deixa pronto para a troca no próximo boot.
 *
 * <b>O programa nunca sobrescreve o próprio SistemaLoja.jar.</b> No Windows o
 * arquivo fica travado enquanto a JVM está rodando, então a tentativa
 * simplesmente falharia — e, se funcionasse, deixaria o app se reescrevendo
 * enquanto usa o arquivo. O download vai para {@code update/SistemaLoja.jar.new}
 * e quem promove esse arquivo é o INICIAR.bat na próxima abertura, quando
 * ninguém está segurando o JAR.
 *
 * O conteúdo é validado por SHA-256 <b>durante</b> a gravação. Sem isso, um
 * download interrompido no meio produz um JAR truncado que só vai falhar na
 * próxima vez que a loja abrir o programa — no pior momento possível, e sem
 * pista nenhuma da causa.
 */
public final class UpdateDownloader {

    private static final Logger LOG = Logger.getLogger(UpdateDownloader.class.getName());

    static final String PASTA_UPDATE = "update";
    static final String ARQUIVO_JAR = "SistemaLoja.jar";
    static final String ARQUIVO_HASH = "SistemaLoja.jar.sha256";
    static final String JAR_PARCIAL = "SistemaLoja.jar.part";
    static final String JAR_PRONTO = "SistemaLoja.jar.new";

    /** Baixar alguns megabytes pode demorar mais que a leitura de um version.txt. */
    static final java.time.Duration TIMEOUT = java.time.Duration.ofMinutes(5);

    private static final int TAMANHO_BUFFER = 8192;

    /** Conteúdo binário sendo baixado, com o tamanho para calcular o progresso. */
    static final class Conteudo implements Closeable {
        final InputStream dados;
        /** Total em bytes, ou -1 quando o servidor não informa. */
        final long tamanho;

        Conteudo(InputStream dados, long tamanho) {
            this.dados = dados;
            this.tamanho = tamanho;
        }

        @Override
        public void close() throws IOException {
            dados.close();
        }
    }

    /** De onde o binário é lido. Existe para os testes trocarem a rede. */
    @FunctionalInterface
    public interface FonteBinaria {
        Conteudo abrir(URI endereco) throws IOException;
    }

    private final String base;
    private final UpdateChecker.Fonte fonteTexto;
    private final FonteBinaria fonteBinaria;

    public UpdateDownloader() {
        this(UpdateChecker.baseConfigurada(), new UpdateChecker.FonteHttp(), new FonteBinariaHttp());
    }

    /** Construtor usado pelos testes, com base e fontes controladas. */
    UpdateDownloader(String base, UpdateChecker.Fonte fonteTexto, FonteBinaria fonteBinaria) {
        this.base = base;
        this.fonteTexto = fonteTexto;
        this.fonteBinaria = fonteBinaria;
    }

    /**
     * A pasta onde o SistemaLoja.jar está instalado.
     *
     * Devolve vazio quando o programa não está rodando a partir de um .jar —
     * é o caso da IDE e do {@code mvn javafx:run}, onde as classes vêm de
     * {@code target/classes}. Nesse cenário não existe JAR para substituir, e o
     * mecanismo de atualização fica desligado.
     */
    public static Optional<Path> pastaDeInstalacao() {
        try {
            CodeSource origem = UpdateDownloader.class.getProtectionDomain().getCodeSource();
            if (origem == null || origem.getLocation() == null) {
                return Optional.empty();
            }
            Path caminho = Path.of(origem.getLocation().toURI());
            if (!caminho.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".jar")) {
                LOG.fine("Não está rodando de um .jar (" + caminho + "); atualização desligada");
                return Optional.empty();
            }
            return Optional.ofNullable(caminho.getParent());
        } catch (Exception e) {
            LOG.log(Level.FINE, "Não foi possível descobrir a pasta de instalação", e);
            return Optional.empty();
        }
    }

    /**
     * Baixa o JAR novo, valida o SHA-256 e deixa o arquivo pronto em
     * {@code update/SistemaLoja.jar.new}.
     *
     * @param pastaInstalacao pasta onde vive o SistemaLoja.jar
     * @param progresso       recebe de 0.0 a 1.0, ou -1.0 quando o tamanho é
     *                        desconhecido (a ProgressBar do JavaFX entende -1
     *                        como "indeterminado")
     * @return o caminho do arquivo pronto para a troca
     * @throws IOException em qualquer falha — rede, disco cheio, pasta sem
     *         permissão de escrita ou hash que não bate. É exceção verificada
     *         de propósito: quem chama precisa tratar e avisar o usuário.
     */
    public Path baixar(Path pastaInstalacao, DoubleConsumer progresso) throws IOException {
        String hashEsperado = extrairHash(fonteTexto.buscar(UpdateChecker.urlDe(base, ARQUIVO_HASH)));
        try (Conteudo conteudo = fonteBinaria.abrir(UpdateChecker.urlDe(base, ARQUIVO_JAR))) {
            return baixarNucleo(pastaInstalacao, conteudo, hashEsperado, progresso);
        }
    }

    /**
     * O miolo da operação, sem nada de rede: grava o fluxo recebido em disco
     * calculando o hash no caminho e só promove o arquivo se ele bater.
     *
     * É package-private para os testes exercitarem os dois desfechos que
     * importam (hash certo e hash errado) com dados na memória.
     */
    static Path baixarNucleo(Path pastaInstalacao,
                             Conteudo conteudo,
                             String hashEsperado,
                             DoubleConsumer progresso) throws IOException {

        if (hashEsperado == null || !hashEsperado.matches("[0-9a-f]{64}")) {
            throw new IOException("Arquivo de verificação inválido: não é um SHA-256");
        }

        Path pastaUpdate = pastaInstalacao.resolve(PASTA_UPDATE);
        try {
            Files.createDirectories(pastaUpdate);
        } catch (IOException e) {
            throw new IOException("Não foi possível criar a pasta " + pastaUpdate
                    + ". Verifique a permissão de escrita.", e);
        }

        Path parcial = pastaUpdate.resolve(JAR_PARCIAL);
        Path pronto = pastaUpdate.resolve(JAR_PRONTO);

        // Um .part sobrando de uma tentativa anterior interrompida não pode ser
        // aproveitado: seria concatenado com o download novo.
        Files.deleteIfExists(parcial);

        MessageDigest sha256 = criarSha256();
        long total = conteudo.tamanho;
        long baixado = 0;
        int ultimoPercentualNotificado = -1;

        try (DigestInputStream entrada = new DigestInputStream(conteudo.dados, sha256);
             OutputStream saida = Files.newOutputStream(parcial)) {

            byte[] buffer = new byte[TAMANHO_BUFFER];
            int lidos;
            while ((lidos = entrada.read(buffer)) != -1) {
                saida.write(buffer, 0, lidos);
                baixado += lidos;

                if (progresso == null) {
                    continue;
                }
                if (total <= 0) {
                    progresso.accept(-1.0); // indeterminado
                    continue;
                }
                // Só notifica quando o percentual inteiro muda: a UI redesenha a
                // barra a cada chamada, e avisar a cada 8 KB inundaria a thread
                // da interface sem nenhum ganho visual.
                int percentual = (int) (baixado * 100 / total);
                if (percentual != ultimoPercentualNotificado) {
                    ultimoPercentualNotificado = percentual;
                    progresso.accept(baixado / (double) total);
                }
            }
        } catch (IOException e) {
            Files.deleteIfExists(parcial);
            throw e;
        }

        String hashBaixado = paraHexadecimal(sha256.digest());
        if (!hashBaixado.equals(hashEsperado)) {
            Files.deleteIfExists(parcial);
            LOG.warning("Download descartado: SHA-256 esperado " + hashEsperado
                    + ", obtido " + hashBaixado);
            throw new IOException("O arquivo baixado está corrompido (verificação SHA-256 falhou).");
        }

        Files.move(parcial, pronto, StandardCopyOption.REPLACE_EXISTING);
        LOG.info("Atualização baixada e validada em " + pronto);
        return pronto;
    }

    /**
     * Aceita tanto o formato "só o hash" (que o workflow gera) quanto o
     * "hash  nome-do-arquivo" padrão do comando sha256sum.
     */
    static String extrairHash(String conteudoDoArquivo) throws IOException {
        if (conteudoDoArquivo == null || conteudoDoArquivo.isBlank()) {
            throw new IOException("Arquivo de verificação vazio");
        }
        String primeiroCampo = conteudoDoArquivo.trim().split("\\s+")[0];
        return primeiroCampo.toLowerCase(Locale.ROOT);
    }

    private static MessageDigest criarSha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 é obrigatório em qualquer JVM; se faltar, o ambiente está quebrado.
            throw new IllegalStateException("SHA-256 indisponível nesta JVM", e);
        }
    }

    private static String paraHexadecimal(byte[] bytes) {
        StringBuilder texto = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            texto.append(Character.forDigit((b >> 4) & 0xF, 16));
            texto.append(Character.forDigit(b & 0xF, 16));
        }
        return texto.toString();
    }

    /** Implementação real da {@link FonteBinaria}, sobre o HttpClient do JDK. */
    static final class FonteBinariaHttp implements FonteBinaria {

        private static final HttpClient CLIENTE = HttpClient.newBuilder()
                .connectTimeout(UpdateChecker.TIMEOUT)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        @Override
        public Conteudo abrir(URI endereco) throws IOException {
            HttpRequest requisicao = HttpRequest.newBuilder(endereco)
                    .timeout(TIMEOUT)
                    .GET()
                    .build();
            try {
                HttpResponse<InputStream> resposta =
                        CLIENTE.send(requisicao, HttpResponse.BodyHandlers.ofInputStream());
                if (resposta.statusCode() != 200) {
                    resposta.body().close();
                    throw new IOException("Resposta HTTP " + resposta.statusCode() + " em " + endereco);
                }
                long tamanho = resposta.headers().firstValueAsLong("content-length").orElse(-1L);
                return new Conteudo(resposta.body(), tamanho);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Download da atualização interrompido", e);
            }
        }
    }
}
