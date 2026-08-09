package com.distribuidora.update;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exercita a gravação do download em disco com @TempDir, sem rede.
 *
 * O caso que mais importa aqui é o do hash que não bate: é ele que impede um
 * download interrompido de virar um JAR truncado, que só falharia na próxima
 * abertura do programa na loja.
 */
class UpdateDownloaderTest {

    private static final byte[] CONTEUDO = "conteudo falso de um jar".getBytes(StandardCharsets.UTF_8);

    private static String sha256De(byte[] dados) throws NoSuchAlgorithmException {
        byte[] resumo = MessageDigest.getInstance("SHA-256").digest(dados);
        StringBuilder hex = new StringBuilder();
        for (byte b : resumo) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }

    private static UpdateDownloader.Conteudo conteudoDe(byte[] dados) {
        return new UpdateDownloader.Conteudo(new ByteArrayInputStream(dados), dados.length);
    }

    private static Path parcial(Path pasta) {
        return pasta.resolve(UpdateDownloader.PASTA_UPDATE).resolve(UpdateDownloader.JAR_PARCIAL);
    }

    private static Path pronto(Path pasta) {
        return pasta.resolve(UpdateDownloader.PASTA_UPDATE).resolve(UpdateDownloader.JAR_PRONTO);
    }

    @Test
    void hashCorretoDeixaOArquivoProntoParaATroca(@TempDir Path pastaInstalacao) throws Exception {
        Path resultado = UpdateDownloader.baixarNucleo(
                pastaInstalacao, conteudoDe(CONTEUDO), sha256De(CONTEUDO), null);

        assertEquals(pronto(pastaInstalacao), resultado);
        assertTrue(Files.exists(resultado), "esperava o SistemaLoja.jar.new gravado");
        assertArrayEquals(CONTEUDO, Files.readAllBytes(resultado));
        assertFalse(Files.exists(parcial(pastaInstalacao)), "o .part deve ter sido renomeado");
    }

    @Test
    void naoTocaNoJarInstalado(@TempDir Path pastaInstalacao) throws Exception {
        // Regra central do mecanismo: no Windows o JAR em uso fica travado, e o
        // programa nunca pode tentar sobrescrever a si mesmo.
        Path jarInstalado = pastaInstalacao.resolve(UpdateDownloader.ARQUIVO_JAR);
        Files.write(jarInstalado, "jar antigo em uso".getBytes(StandardCharsets.UTF_8));

        UpdateDownloader.baixarNucleo(pastaInstalacao, conteudoDe(CONTEUDO), sha256De(CONTEUDO), null);

        assertEquals("jar antigo em uso", Files.readString(jarInstalado));
    }

    @Test
    void hashErradoApagaOParcialENaoCriaOArquivoNovo(@TempDir Path pastaInstalacao) {
        String hashDeOutroConteudo = "0".repeat(64);

        IOException erro = assertThrows(IOException.class, () -> UpdateDownloader.baixarNucleo(
                pastaInstalacao, conteudoDe(CONTEUDO), hashDeOutroConteudo, null));

        assertTrue(erro.getMessage().contains("corrompido"));
        assertFalse(Files.exists(pronto(pastaInstalacao)), "não pode deixar um .new inválido para o boot aplicar");
        assertFalse(Files.exists(parcial(pastaInstalacao)), "o .part inválido deve ser apagado");
    }

    @Test
    void downloadInterrompidoNoMeioNaoDeixaArquivoPronto(@TempDir Path pastaInstalacao) throws Exception {
        InputStream queFalhaNoMeio = new InputStream() {
            private int lidos = 0;

            @Override
            public int read() throws IOException {
                if (lidos++ > 5) {
                    throw new IOException("conexão caiu");
                }
                return 1;
            }
        };
        UpdateDownloader.Conteudo conteudo = new UpdateDownloader.Conteudo(queFalhaNoMeio, 100);

        assertThrows(IOException.class, () -> UpdateDownloader.baixarNucleo(
                pastaInstalacao, conteudo, sha256De(CONTEUDO), null));

        assertFalse(Files.exists(pronto(pastaInstalacao)));
        assertFalse(Files.exists(parcial(pastaInstalacao)));
    }

    @Test
    void descartaSobraDeUmaTentativaAnterior(@TempDir Path pastaInstalacao) throws Exception {
        Files.createDirectories(pastaInstalacao.resolve(UpdateDownloader.PASTA_UPDATE));
        Files.write(parcial(pastaInstalacao), "lixo de uma tentativa anterior".getBytes(StandardCharsets.UTF_8));

        UpdateDownloader.baixarNucleo(pastaInstalacao, conteudoDe(CONTEUDO), sha256De(CONTEUDO), null);

        // Se o .part antigo fosse reaproveitado, o conteúdo sairia concatenado.
        assertArrayEquals(CONTEUDO, Files.readAllBytes(pronto(pastaInstalacao)));
    }

    @Test
    void arquivoDeVerificacaoInvalidoNemComecaODownload(@TempDir Path pastaInstalacao) {
        assertThrows(IOException.class, () -> UpdateDownloader.baixarNucleo(
                pastaInstalacao, conteudoDe(CONTEUDO), "<html>404</html>", null));

        assertFalse(Files.exists(pronto(pastaInstalacao)));
    }

    @Test
    void relataOProgressoAteOFim(@TempDir Path pastaInstalacao) throws Exception {
        byte[] grande = new byte[64 * 1024];
        List<Double> progressos = new ArrayList<>();

        UpdateDownloader.baixarNucleo(pastaInstalacao, conteudoDe(grande), sha256De(grande), progressos::add);

        assertFalse(progressos.isEmpty(), "esperava pelo menos uma notificação de progresso");
        assertEquals(1.0, progressos.get(progressos.size() - 1), 0.0001, "o último progresso deve ser 100%");
        assertTrue(progressos.stream().allMatch(p -> p >= 0.0 && p <= 1.0));
    }

    @Test
    void tamanhoDesconhecidoRelataProgressoIndeterminado(@TempDir Path pastaInstalacao) throws Exception {
        UpdateDownloader.Conteudo semTamanho =
                new UpdateDownloader.Conteudo(new ByteArrayInputStream(CONTEUDO), -1);
        List<Double> progressos = new ArrayList<>();

        UpdateDownloader.baixarNucleo(pastaInstalacao, semTamanho, sha256De(CONTEUDO), progressos::add);

        // -1 é o valor que a ProgressBar do JavaFX entende como indeterminado.
        assertTrue(progressos.stream().allMatch(p -> p == -1.0));
        assertTrue(Files.exists(pronto(pastaInstalacao)));
    }

    @Test
    void extraiOHashNosDoisFormatos() throws Exception {
        String hash = "a".repeat(64);

        assertEquals(hash, UpdateDownloader.extrairHash(hash + "\n"));
        assertEquals(hash, UpdateDownloader.extrairHash(hash + "  SistemaLoja.jar\n"));
        assertEquals(hash, UpdateDownloader.extrairHash("  " + hash.toUpperCase() + "  "));
        assertThrows(IOException.class, () -> UpdateDownloader.extrairHash(""));
        assertThrows(IOException.class, () -> UpdateDownloader.extrairHash(null));
    }

    @Test
    void naoDescobrePastaDeInstalacaoQuandoRodaForaDeUmJar() {
        // Nos testes as classes vêm de target/classes, não de um .jar: o
        // mecanismo de atualização precisa ficar desligado nesse caso.
        assertTrue(UpdateDownloader.pastaDeInstalacao().isEmpty());
    }
}
