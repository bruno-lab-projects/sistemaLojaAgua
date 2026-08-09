package com.distribuidora;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MigrationsTest {

    @BeforeEach
    void apontarParaDiretorioTemporario(@TempDir Path temp) {
        System.setProperty(Database.DB_DIR_PROPERTY, temp.toString());
    }

    @AfterEach
    void limparPropriedade() {
        System.clearProperty(Database.DB_DIR_PROPERTY);
    }

    @Test
    void bancoNovoTerminaNaVersaoAtual() throws SQLException {
        Database.initialize();

        try (Connection conn = Database.connect()) {
            assertEquals(Migrations.SCHEMA_VERSION, Migrations.currentVersion(conn));
        }
    }

    @Test
    void bancoNovoTemTodasAsTabelas() throws SQLException {
        Database.initialize();

        try (Connection conn = Database.connect()) {
            assertTrue(tabelaExiste(conn, "Clientes"));
            assertTrue(tabelaExiste(conn, "Funcionarios"));
            assertTrue(tabelaExiste(conn, "Produtos"));
            assertTrue(tabelaExiste(conn, "Pedidos"));
        }
    }

    @Test
    void migraBancoLegadoSemAsColunasHistoricas() throws SQLException {
        criarBancoLegado();

        Database.initialize();

        try (Connection conn = Database.connect()) {
            assertEquals(Migrations.SCHEMA_VERSION, Migrations.currentVersion(conn));
            assertTrue(Migrations.columnExists(conn, "Pedidos", "cliente_nome_historico"));
            assertTrue(Migrations.columnExists(conn, "Pedidos", "cliente_telefone_historico"));
            assertTrue(Migrations.columnExists(conn, "Pedidos", "cliente_endereco_historico"));
            assertTrue(Migrations.columnExists(conn, "Pedidos", "funcionario_nome_historico"));
            assertTrue(Migrations.columnExists(conn, "Pedidos", "produto_nome_historico"));
            assertTrue(Migrations.columnExists(conn, "Pedidos", "produto_preco_historico"));
        }
    }

    @Test
    void migracaoDeBancoLegadoPreservaOsPedidosExistentes() throws SQLException {
        criarBancoLegado();

        Database.initialize();

        try (Connection conn = Database.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Pedidos")) {
            assertEquals(1, rs.next() ? rs.getInt(1) : -1, "o pedido legado não pode sumir");
        }
    }

    @Test
    void rodarDuasVezesNaoAlteraNadaENaoLanca() throws SQLException {
        Database.initialize();

        assertDoesNotThrow(Database::initialize);

        try (Connection conn = Database.connect()) {
            assertEquals(Migrations.SCHEMA_VERSION, Migrations.currentVersion(conn));
        }
    }

    @Test
    void columnExistsRetornaFalsoParaColunaInexistente() throws SQLException {
        Database.initialize();

        try (Connection conn = Database.connect()) {
            assertTrue(!Migrations.columnExists(conn, "Pedidos", "coluna_que_nao_existe"));
        }
    }

    /**
     * Recria o esquema anterior ao versionamento: Pedidos sem as colunas *_historico.
     *
     * Inclui as colunas *_avulso/pendencia_* porque, no Database.java real, elas já
     * vêm no CREATE TABLE (nunca foram adicionadas por ALTER) e o backfill as
     * referencia sem checagem prévia. Um banco realmente sem essas colunas já
     * quebraria hoje, antes desta tarefa; não é o cenário que este teste cobre.
     * O que este teste cobre é exatamente o que os seis ALTER TABLE de
     * Database.migrateHistoricalData tratavam: a falta das colunas *_historico.
     */
    private void criarBancoLegado() throws SQLException {
        try (Connection conn = Database.connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE Clientes (id INTEGER PRIMARY KEY AUTOINCREMENT, nome TEXT NOT NULL,"
                    + " telefone TEXT, endereco TEXT, predio_casa TEXT, numero TEXT, observacoes TEXT)");
            stmt.execute("CREATE TABLE Funcionarios (id INTEGER PRIMARY KEY AUTOINCREMENT, nome TEXT NOT NULL)");
            stmt.execute("CREATE TABLE Produtos (id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + " nome TEXT NOT NULL UNIQUE, preco REAL)");
            stmt.execute("CREATE TABLE Pedidos (id INTEGER PRIMARY KEY AUTOINCREMENT, cliente_id INTEGER NULL,"
                    + " funcionario_id INTEGER NULL, produto_id INTEGER NULL, status TEXT NOT NULL,"
                    + " data_hora TEXT NOT NULL, quantidade INTEGER NOT NULL DEFAULT 1,"
                    + " nome_avulso TEXT NULL, endereco_avulso TEXT NULL, predio_casa_avulso TEXT NULL,"
                    + " numero_avulso TEXT NULL, data_hora_saiu TEXT NULL, data_hora_entregue TEXT NULL,"
                    + " forma_pagamento TEXT NULL, pendencia_pagamento INTEGER DEFAULT 0,"
                    + " pendencia_garrafao INTEGER DEFAULT 0)");
            stmt.executeUpdate("INSERT INTO Produtos (nome, preco) VALUES ('Galao 20L', 12.5)");
            stmt.executeUpdate("INSERT INTO Pedidos (produto_id, status, data_hora, quantidade)"
                    + " VALUES (1, 'Feito', '2026-01-15 10:00:00', 2)");
            stmt.execute("PRAGMA user_version = 0");
        }
    }

    private boolean tabelaExiste(Connection conn, String nome) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT name FROM sqlite_master WHERE type = 'table' AND name = '" + nome + "'")) {
            return rs.next();
        }
    }
}
