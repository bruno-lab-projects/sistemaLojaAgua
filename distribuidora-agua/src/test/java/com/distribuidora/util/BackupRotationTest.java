package com.distribuidora.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BackupRotationTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 8, 7);

    @Test
    void mantemTodosOsBackupsDosUltimos30Dias(@TempDir Path dir) throws IOException {
        criarBackup(dir, "2026-08-07");
        criarBackup(dir, "2026-08-01");
        criarBackup(dir, "2026-07-15");
        criarBackup(dir, "2026-07-09"); // exatamente 29 dias atrás

        int apagados = BackupRotation.rotacionar(dir, HOJE);

        assertEquals(0, apagados);
        assertTrue(Files.exists(dir.resolve("distribuidora_2026-07-09.db")));
    }

    @Test
    void mantemApenasOPrimeiroDeCadaMesAntigo(@TempDir Path dir) throws IOException {
        criarBackup(dir, "2026-03-02");
        criarBackup(dir, "2026-03-15");
        criarBackup(dir, "2026-03-28");
        criarBackup(dir, "2026-04-05");
        criarBackup(dir, "2026-04-20");

        int apagados = BackupRotation.rotacionar(dir, HOJE);

        assertEquals(3, apagados);
        assertTrue(Files.exists(dir.resolve("distribuidora_2026-03-02.db")), "primeiro de março fica");
        assertFalse(Files.exists(dir.resolve("distribuidora_2026-03-15.db")));
        assertFalse(Files.exists(dir.resolve("distribuidora_2026-03-28.db")));
        assertTrue(Files.exists(dir.resolve("distribuidora_2026-04-05.db")), "primeiro de abril fica");
        assertFalse(Files.exists(dir.resolve("distribuidora_2026-04-20.db")));
    }

    @Test
    void naoApagaArquivosComNomeForaDoPadrao(@TempDir Path dir) throws IOException {
        Files.writeString(dir.resolve("backup_manual_importante.db"), "x");
        Files.writeString(dir.resolve("distribuidora_sem-data.db"), "x");
        Files.writeString(dir.resolve("leia-me.txt"), "x");
        criarBackup(dir, "2026-01-10");
        criarBackup(dir, "2026-01-20");

        BackupRotation.rotacionar(dir, HOJE);

        assertTrue(Files.exists(dir.resolve("backup_manual_importante.db")));
        assertTrue(Files.exists(dir.resolve("distribuidora_sem-data.db")));
        assertTrue(Files.exists(dir.resolve("leia-me.txt")));
    }

    @Test
    void nuncaApagaOBackupMaisRecente(@TempDir Path dir) throws IOException {
        criarBackup(dir, "2026-08-07");

        BackupRotation.rotacionar(dir, HOJE);

        assertTrue(Files.exists(dir.resolve("distribuidora_2026-08-07.db")));
    }

    @Test
    void naoLancaQuandoDiretorioNaoExiste(@TempDir Path dir) throws IOException {
        assertEquals(0, BackupRotation.rotacionar(dir.resolve("nao-existe"), HOJE));
    }

    @Test
    void naoLancaComDiretorioVazio(@TempDir Path dir) throws IOException {
        assertEquals(0, BackupRotation.rotacionar(dir, HOJE));
    }

    @Test
    void ignoraDiretorioComNomeNoPadrao(@TempDir Path dir) throws IOException {
        Files.createDirectory(dir.resolve("distribuidora_2020-01-15.db"));
        criarBackup(dir, "2020-01-01");
        criarBackup(dir, "2020-02-01");

        BackupRotation.rotacionar(dir, HOJE);

        assertTrue(Files.isDirectory(dir.resolve("distribuidora_2020-01-15.db")), "diretório com nome no padrão nunca é considerado candidato");
    }

    private void criarBackup(Path dir, String data) throws IOException {
        Files.writeString(dir.resolve("distribuidora_" + data + ".db"), "conteudo");
    }
}
