package com.distribuidora.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Retenção dos backups diários.
 *
 * Regra: todos os backups dos últimos 30 dias ficam. Dos mais antigos, fica
 * apenas o primeiro de cada mês. Em três anos isso resulta em cerca de 60
 * arquivos, e não em mil.
 *
 * Só reconhece o padrão distribuidora_YYYY-MM-DD.db, gerado por
 * Database.performDailyBackupIfNeeded. Qualquer outro arquivo do diretório é
 * ignorado, inclusive backups manuais que o usuário tenha salvo ali.
 */
public final class BackupRotation {

    private static final Logger LOG = Logger.getLogger(BackupRotation.class.getName());

    private static final String PREFIXO = "distribuidora_";
    private static final String SUFIXO = ".db";
    private static final int DIAS_MANTIDOS_INTEGRALMENTE = 30;

    private BackupRotation() {
    }

    /**
     * @return quantidade de arquivos apagados
     */
    public static int rotacionar(Path diretorio, LocalDate hoje) throws IOException {
        if (diretorio == null || !Files.isDirectory(diretorio)) {
            return 0;
        }

        List<Path> candidatos = new ArrayList<>();
        Map<Path, LocalDate> datas = new HashMap<>();

        try (Stream<Path> arquivos = Files.list(diretorio)) {
            for (Path arquivo : (Iterable<Path>) arquivos::iterator) {
                if (!Files.isRegularFile(arquivo)) {
                    continue; // diretório (ou outro tipo especial) com nome no padrão não é candidato
                }
                LocalDate data = extrairData(arquivo);
                if (data == null) {
                    continue;
                }
                candidatos.add(arquivo);
                datas.put(arquivo, data);
            }
        }

        LocalDate limite = hoje.minusDays(DIAS_MANTIDOS_INTEGRALMENTE);

        // Para cada mês antigo, guarda a data do primeiro backup daquele mês.
        Map<YearMonth, LocalDate> primeiroDoMes = new HashMap<>();
        for (Path arquivo : candidatos) {
            LocalDate data = datas.get(arquivo);
            if (data.isAfter(limite)) {
                continue;
            }
            YearMonth mes = YearMonth.from(data);
            LocalDate atual = primeiroDoMes.get(mes);
            if (atual == null || data.isBefore(atual)) {
                primeiroDoMes.put(mes, data);
            }
        }

        int apagados = 0;
        for (Path arquivo : candidatos) {
            LocalDate data = datas.get(arquivo);

            if (data.isAfter(limite)) {
                continue; // dentro da janela de 30 dias
            }
            if (data.equals(primeiroDoMes.get(YearMonth.from(data)))) {
                continue; // é o representante mensal
            }

            try {
                Files.delete(arquivo);
                apagados++;
            } catch (IOException e) {
                LOG.log(Level.WARNING, "Não foi possível apagar o backup antigo " + arquivo, e);
            }
        }

        if (apagados > 0) {
            LOG.info("Rotação de backups: " + apagados + " arquivo(s) antigo(s) removido(s)");
        }
        return apagados;
    }

    /** Retorna a data do nome do arquivo, ou null se o nome não seguir o padrão. */
    private static LocalDate extrairData(Path arquivo) {
        String nome = arquivo.getFileName().toString();
        if (!nome.startsWith(PREFIXO) || !nome.endsWith(SUFIXO)) {
            return null;
        }
        String meio = nome.substring(PREFIXO.length(), nome.length() - SUFIXO.length());
        try {
            return LocalDate.parse(meio);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
