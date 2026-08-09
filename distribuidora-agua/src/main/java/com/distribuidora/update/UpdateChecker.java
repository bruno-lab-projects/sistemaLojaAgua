package com.distribuidora.update;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Descobre se existe uma versão publicada mais nova que a instalada.
 *
 * Lê um arquivo de texto simples ({@code version.txt}) publicado junto do
 * release, em vez de consultar a API do GitHub. Assim não há limite de 60
 * requisições por hora por IP, não é preciso interpretar JSON e nenhuma
 * biblioteca nova entra no projeto — {@code java.net.http.HttpClient} já vem
 * no Java 11.
 *
 * O acesso à rede fica atrás da interface {@link Fonte}. É o que permite testar
 * todas as decisões desta classe (versão maior, igual, menor, resposta
 * inválida, falha de conexão) sem depender de internet no build.
 */
public final class UpdateChecker {

    private static final Logger LOG = Logger.getLogger(UpdateChecker.class.getName());

    /**
     * Sobrescreve a URL base, para apontar o app a um servidor local durante os
     * testes manuais. Mesmo padrão de {@code distribuidora.db.dir}.
     */
    public static final String BASE_PROPERTY = "distribuidora.update.base";

    static final String ARQUIVO_VERSAO = "version.txt";

    /**
     * Vale tanto para conectar quanto para receber a resposta. Dez segundos é
     * bastante para um arquivo de poucos bytes e curto o suficiente para uma
     * internet ruim na loja não deixar a verificação pendurada.
     */
    static final Duration TIMEOUT = Duration.ofSeconds(10);

    /** De onde o texto de uma URL é lido. Existe para os testes trocarem a rede. */
    @FunctionalInterface
    public interface Fonte {
        String buscar(URI endereco) throws IOException;
    }

    private final String base;
    private final Fonte fonte;

    public UpdateChecker() {
        this(baseConfigurada(), new FonteHttp());
    }

    /** Construtor usado pelos testes, com base e fonte controladas. */
    UpdateChecker(String base, Fonte fonte) {
        this.base = base;
        this.fonte = fonte;
    }

    /**
     * A URL base dos arquivos de atualização: a system property
     * {@value #BASE_PROPERTY} tem prioridade sobre o valor do app.properties.
     */
    public static String baseConfigurada() {
        String override = System.getProperty(BASE_PROPERTY);
        if (override != null && !override.isBlank()) {
            return override.trim();
        }
        return AppVersion.baseDeAtualizacao();
    }

    /** Monta a URL de um arquivo do release, tolerando barra sobrando na base. */
    static URI urlDe(String base, String arquivo) {
        String semBarraFinal = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
        return URI.create(semBarraFinal + "/" + arquivo);
    }

    /**
     * Devolve a versão publicada quando ela é mais nova que a instalada, ou
     * {@link Optional#empty()} em qualquer outro caso — inclusive quando a rede
     * falha.
     *
     * A falha é intencionalmente silenciosa (só {@code LOG.fine}): uma
     * distribuidora sem internet não pode ver mensagem de erro toda vez que
     * abre o programa.
     *
     * @param versaoLocal versão que está rodando, normalmente {@link AppVersion#atual()}
     */
    public Optional<String> versaoDisponivel(String versaoLocal) {
        if (base == null || base.isBlank()) {
            LOG.fine("Verificação de atualização desligada: nenhuma URL base configurada");
            return Optional.empty();
        }

        String remota;
        try {
            remota = fonte.buscar(urlDe(base, ARQUIVO_VERSAO));
        } catch (IOException | RuntimeException e) {
            // Inclui RuntimeException porque URI.create e o próprio HttpClient
            // podem estourar sem ser IOException (URL malformada, por exemplo).
            LOG.log(Level.FINE, "Não foi possível verificar atualizações", e);
            return Optional.empty();
        }

        String limpa = primeiraLinha(remota);
        if (!AppVersion.ehMaisNova(limpa, versaoLocal)) {
            LOG.fine("Nenhuma atualização: publicada=" + limpa + " instalada=" + versaoLocal);
            return Optional.empty();
        }

        LOG.info("Versão nova disponível: " + limpa + " (instalada: " + versaoLocal + ")");
        return Optional.of(limpa);
    }

    /**
     * Fica só com a primeira linha e sem espaços. Protege contra o caso em que
     * a URL responde algo que não é o version.txt esperado (uma página de erro
     * do GitHub, por exemplo) — o texto extra vira formato inválido e o
     * AppVersion recusa, em vez de tentar interpretar.
     */
    private static String primeiraLinha(String texto) {
        if (texto == null) {
            return "";
        }
        int quebra = texto.indexOf('\n');
        String linha = quebra >= 0 ? texto.substring(0, quebra) : texto;
        return linha.trim();
    }

    /** Implementação real da {@link Fonte}, sobre o HttpClient do próprio JDK. */
    static final class FonteHttp implements Fonte {

        private static final HttpClient CLIENTE = HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                // Obrigatório: o GitHub responde 302 redirecionando o
                // /releases/latest/download/... para o CDN que serve o arquivo.
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        @Override
        public String buscar(URI endereco) throws IOException {
            HttpRequest requisicao = HttpRequest.newBuilder(endereco)
                    .timeout(TIMEOUT)
                    .GET()
                    .build();
            try {
                HttpResponse<String> resposta =
                        CLIENTE.send(requisicao, HttpResponse.BodyHandlers.ofString());
                if (resposta.statusCode() != 200) {
                    throw new IOException("Resposta HTTP " + resposta.statusCode() + " em " + endereco);
                }
                return resposta.body();
            } catch (InterruptedException e) {
                // Restaura a flag para não engolir o pedido de interrupção da thread.
                Thread.currentThread().interrupt();
                throw new IOException("Verificação de atualização interrompida", e);
            }
        }
    }
}
