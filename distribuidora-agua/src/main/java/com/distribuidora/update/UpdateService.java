package com.distribuidora.update;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javafx.application.Platform;
import javafx.concurrent.Task;

/**
 * Orquestra a atualização: agenda a verificação, dispara o download sob clique
 * do usuário e reinicia o programa para aplicar.
 *
 * Segue o mesmo desenho do {@code DbBackground}: o trabalho lento acontece fora
 * da thread da interface e o resultado volta para ela através dos handlers do
 * {@link Task}, que já rodam na thread certa.
 *
 * Nada é aplicado sozinho. A verificação só acende um aviso; o download depende
 * de um clique; e a troca do arquivo só acontece no próximo início do programa.
 */
public final class UpdateService {

    private static final Logger LOG = Logger.getLogger(UpdateService.class.getName());

    /** De quanto em quanto tempo verificar, com o programa aberto o dia todo. */
    static final long INTERVALO_HORAS = 4;

    /** Guarda o dia em que o usuário clicou em "Lembrar depois". */
    static final String PREF_ADIADO_EM = "atualizacao.adiada.em";

    static final String SCRIPT_INICIALIZACAO = "INICIAR.bat";

    /**
     * Daemon pelo mesmo motivo do DbBackground: uma thread de verificação de
     * atualização não pode impedir o programa de fechar.
     */
    private static final ScheduledExecutorService AGENDADOR =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "update-checker");
                t.setDaemon(true);
                return t;
            });

    private final UpdateChecker checker;
    private final UpdateDownloader downloader;

    public UpdateService() {
        this(new UpdateChecker(), new UpdateDownloader());
    }

    UpdateService(UpdateChecker checker, UpdateDownloader downloader) {
        this.checker = checker;
        this.downloader = downloader;
    }

    /**
     * Verifica na abertura e depois a cada {@value #INTERVALO_HORAS} horas,
     * avisando só quando existe versão nova. É o gancho usado pela tela
     * principal.
     *
     * @param aoEncontrarVersaoNova recebe o número da versão, já na thread da UI
     */
    public void verificarEmBackground(Consumer<String> aoEncontrarVersaoNova) {
        if (UpdateDownloader.pastaDeInstalacao().isEmpty()) {
            // Rodando pela IDE não há JAR para trocar; nem vale acessar a rede.
            LOG.fine("Atualização automática desligada: não está rodando de um .jar");
            return;
        }

        AGENDADOR.scheduleAtFixedRate(() -> {
            if (adiadoHoje()) {
                LOG.fine("Aviso de atualização adiado pelo usuário até amanhã");
                return;
            }
            Task<Optional<String>> tarefa = novaTarefaDeVerificacao();
            tarefa.setOnSucceeded(evento -> tarefa.getValue().ifPresent(aoEncontrarVersaoNova));
            // Já estamos numa thread de background: rodar a Task aqui mesmo é o
            // suficiente, e os handlers continuam sendo entregues na thread da UI.
            tarefa.run();
        }, 0, INTERVALO_HORAS, TimeUnit.HOURS);
    }

    /**
     * Verificação manual, disparada pelo botão da tela administrativa. Ao
     * contrário da automática, aqui o resultado aparece mesmo quando não há
     * novidade — o usuário pediu e merece uma resposta.
     *
     * @param aoConcluir recebe a versão nova, ou vazio, já na thread da UI
     */
    public void verificarAgora(Consumer<Optional<String>> aoConcluir) {
        Task<Optional<String>> tarefa = novaTarefaDeVerificacao();
        tarefa.setOnSucceeded(evento -> aoConcluir.accept(tarefa.getValue()));
        tarefa.setOnFailed(evento -> aoConcluir.accept(Optional.empty()));
        AGENDADOR.execute(tarefa);
    }

    private Task<Optional<String>> novaTarefaDeVerificacao() {
        return new Task<Optional<String>>() {
            @Override
            protected Optional<String> call() {
                // versaoDisponivel já trata as falhas internamente e nunca lança.
                return checker.versaoDisponivel(AppVersion.atual());
            }
        };
    }

    /**
     * Baixa a versão nova em background e deixa o arquivo pronto para a troca.
     *
     * @param progresso  recebe de 0.0 a 1.0 (ou -1 para indeterminado), na thread da UI
     * @param aoConcluir chamado quando o arquivo está validado e pronto
     * @param aoFalhar   recebe a causa da falha, para a tela mostrar um recado curto
     */
    public void baixarEmBackground(DoubleConsumer progresso, Runnable aoConcluir, Consumer<Throwable> aoFalhar) {
        Optional<Path> pasta = UpdateDownloader.pastaDeInstalacao();
        if (pasta.isEmpty()) {
            aoFalhar.accept(new IOException("Não foi possível localizar a pasta de instalação do sistema."));
            return;
        }

        Task<Path> tarefa = new Task<Path>() {
            @Override
            protected Path call() throws IOException {
                // updateProgress alimenta o progressProperty, que a UI observa
                // na thread certa sem precisar de Platform.runLater manual.
                return downloader.baixar(pasta.get(), fracao -> {
                    if (fracao < 0) {
                        updateProgress(-1, 1); // indeterminado
                    } else {
                        updateProgress(fracao, 1.0);
                    }
                });
            }
        };

        tarefa.progressProperty().addListener((obs, anterior, atual) -> progresso.accept(atual.doubleValue()));
        tarefa.setOnSucceeded(evento -> aoConcluir.run());
        tarefa.setOnFailed(evento -> {
            Throwable causa = tarefa.getException();
            LOG.log(Level.WARNING, "Falha ao baixar a atualização", causa);
            aoFalhar.accept(causa);
        });

        AGENDADOR.execute(tarefa);
    }

    /**
     * Fecha o programa e o abre de novo pelo INICIAR.bat, que é quem promove o
     * SistemaLoja.jar.new antes de subir a JVM.
     *
     * Só faz sentido no Windows, onde o .bat existe. Em outros sistemas devolve
     * {@code false} e a tela pede para o usuário fechar e abrir manualmente.
     */
    public static boolean reiniciar() {
        Optional<Path> pasta = UpdateDownloader.pastaDeInstalacao();
        if (pasta.isEmpty() || !ehWindows()) {
            LOG.info("Reinício automático indisponível neste ambiente");
            return false;
        }

        try {
            // "start" abre o .bat numa janela nova e desacoplada: sem isso o
            // novo processo morreria junto com este ao chamar Platform.exit().
            // O "" logo depois é o título da janela, que o comando start exige.
            new ProcessBuilder("cmd", "/c", "start", "", SCRIPT_INICIALIZACAO)
                    .directory(pasta.get().toFile())
                    .start();
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Não foi possível reiniciar o sistema automaticamente", e);
            return false;
        }

        Platform.exit();
        return true;
    }

    static boolean ehWindows() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
    }

    /** Registra que o usuário pediu para ser lembrado depois. */
    public static void adiarParaAmanha() {
        preferencias().put(PREF_ADIADO_EM, LocalDate.now().toString());
        LOG.fine("Aviso de atualização adiado");
    }

    /** Verdadeiro enquanto for o mesmo dia em que o usuário clicou em "Lembrar depois". */
    public static boolean adiadoHoje() {
        return estaAdiado(preferencias().get(PREF_ADIADO_EM, null), LocalDate.now());
    }

    /**
     * Lógica pura da decisão, separada do Preferences para poder ser testada.
     * Data inválida ou ausente conta como "não adiado": na dúvida, avisar.
     */
    static boolean estaAdiado(String dataGravada, LocalDate hoje) {
        if (dataGravada == null || dataGravada.isBlank()) {
            return false;
        }
        try {
            return LocalDate.parse(dataGravada.trim()).isEqual(hoje);
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static Preferences preferencias() {
        return Preferences.userNodeForPackage(UpdateService.class);
    }
}
