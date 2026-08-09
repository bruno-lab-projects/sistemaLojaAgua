package com.distribuidora;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabasePathTest {

    @AfterEach
    void limparPropriedade() {
        System.clearProperty(Database.DB_DIR_PROPERTY);
    }

    @Test
    void usaDiretorioDaSystemPropertyQuandoDefinida(@TempDir Path temp) {
        System.setProperty(Database.DB_DIR_PROPERTY, temp.toString());

        assertEquals(temp, Database.getDatabaseDirectory());
        assertEquals(temp.resolve("distribuidora.db"), Database.getDatabaseFilePath());
    }

    @Test
    void usaHomeDoUsuarioQuandoPropriedadeAusente() {
        System.clearProperty(Database.DB_DIR_PROPERTY);

        Path esperado = Path.of(System.getProperty("user.home"), ".distribuidora_agua");

        assertEquals(esperado, Database.getDatabaseDirectory());
    }

    @Test
    void ignoraPropriedadeEmBranco(@TempDir Path temp) {
        System.setProperty(Database.DB_DIR_PROPERTY, "   ");

        assertTrue(Database.getDatabaseDirectory().toString().endsWith(".distribuidora_agua"));
    }
}
