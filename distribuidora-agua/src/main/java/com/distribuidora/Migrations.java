package com.distribuidora;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

/**
 * Esquema versionado por PRAGMA user_version.
 *
 * Substitui os ALTER TABLE com catch vazio que existiam em Database. Ali, disco
 * cheio, permissão negada e banco corrompido eram indistinguíveis de "a coluna
 * já existe", e o app seguia como se estivesse tudo certo.
 *
 * Regras para adicionar uma migração nova:
 *   1. escreva um método migrateVNParaVN+1(Connection);
 *   2. adicione o case correspondente em applyAll;
 *   3. incremente SCHEMA_VERSION;
 *   4. escreva o teste que parte de um banco na versão anterior.
 * Nunca edite uma migração já publicada: bancos em produção já a rodaram.
 */
final class Migrations {

    private static final Logger LOG = Logger.getLogger(Migrations.class.getName());

    /** Versão de esquema que este código espera. */
    static final int SCHEMA_VERSION = 2;

    private Migrations() {
    }

    /**
     * Aplica, em ordem, todas as migrações pendentes.
     * Espera receber uma conexão já dentro de transação (ver Database.inTransaction).
     */
    static void applyAll(Connection conn) throws SQLException {
        int versaoAtual = currentVersion(conn);

        if (versaoAtual > SCHEMA_VERSION) {
            throw new SQLException("O banco de dados está na versão " + versaoAtual
                    + ", mais nova que a versão " + SCHEMA_VERSION + " suportada por esta instalação."
                    + " Atualize o sistema para a versão mais recente.");
        }

        if (versaoAtual == SCHEMA_VERSION) {
            return;
        }

        LOG.info("Migrando o esquema do banco da versão " + versaoAtual + " para " + SCHEMA_VERSION);

        for (int alvo = versaoAtual + 1; alvo <= SCHEMA_VERSION; alvo++) {
            switch (alvo) {
                case 1:
                    migrarPara1Baseline(conn);
                    break;
                case 2:
                    migrarPara2Indices(conn);
                    break;
                default:
                    throw new SQLException("Migração para a versão " + alvo + " não implementada");
            }
            setVersion(conn, alvo);
            LOG.info("Esquema migrado para a versão " + alvo);
        }
    }

    static int currentVersion(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("PRAGMA user_version")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private static void setVersion(Connection conn, int versao) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // PRAGMA não aceita parâmetro de PreparedStatement.
            // O valor vem de uma constante int do próprio código, não de entrada externa.
            stmt.execute("PRAGMA user_version = " + versao);
        }
    }

    /**
     * v1: esquema base. Idempotente, porque roda tanto sobre banco novo quanto
     * sobre banco criado antes do versionamento (que também reporta user_version 0).
     *
     * O CREATE TABLE e o UPDATE de backfill abaixo são copiados byte a byte do
     * Database.java anterior a esta tarefa (initialize()/migrateHistoricalData()).
     * Esta tarefa entrega apenas versionamento, transação e idempotência dos
     * ALTER TABLE; nenhuma mudança de comportamento visível foi introduzida aqui.
     */
    private static void migrarPara1Baseline(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS Clientes ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "nome TEXT NOT NULL,"
                    + "telefone TEXT,"
                    + "endereco TEXT,"
                    + "predio_casa TEXT,"
                    + "numero TEXT,"
                    + "observacoes TEXT"
                    + ");");

            stmt.execute("CREATE TABLE IF NOT EXISTS Funcionarios ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "nome TEXT NOT NULL"
                    + ");");

            stmt.execute("CREATE TABLE IF NOT EXISTS Produtos ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "nome TEXT NOT NULL UNIQUE,"
                    + "preco REAL"
                    + ");");

            stmt.execute("CREATE TABLE IF NOT EXISTS Pedidos ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "cliente_id INTEGER NULL,"
                    + "funcionario_id INTEGER NULL,"
                    + "produto_id INTEGER NULL,"
                    + "status TEXT NOT NULL,"
                    + "data_hora TEXT NOT NULL,"
                    + "quantidade INTEGER NOT NULL DEFAULT 1,"
                    + "nome_avulso TEXT NULL,"
                    + "endereco_avulso TEXT NULL,"
                    + "predio_casa_avulso TEXT NULL,"
                    + "numero_avulso TEXT NULL,"
                    + "data_hora_saiu TEXT NULL,"
                    + "data_hora_entregue TEXT NULL,"
                    + "forma_pagamento TEXT NULL,"
                    + "pendencia_pagamento INTEGER DEFAULT 0,"
                    + "pendencia_garrafao INTEGER DEFAULT 0,"
                    // Dados históricos (snapshot) - preservam informações mesmo após exclusão/alteração
                    + "cliente_nome_historico TEXT,"
                    + "cliente_telefone_historico TEXT,"
                    + "cliente_endereco_historico TEXT,"
                    + "funcionario_nome_historico TEXT,"
                    + "produto_nome_historico TEXT NOT NULL,"
                    + "produto_preco_historico REAL"
                    + ");");
        }

        // Bancos legados têm Pedidos sem estas colunas. Verificar antes de alterar,
        // em vez de tentar e engolir a exceção (ALTER TABLE ADD COLUMN não é
        // idempotente no SQLite: rodar duas vezes lança "duplicate column name").
        adicionarColunaSeAusente(conn, "Pedidos", "cliente_nome_historico", "TEXT");
        adicionarColunaSeAusente(conn, "Pedidos", "cliente_telefone_historico", "TEXT");
        adicionarColunaSeAusente(conn, "Pedidos", "cliente_endereco_historico", "TEXT");
        adicionarColunaSeAusente(conn, "Pedidos", "funcionario_nome_historico", "TEXT");
        adicionarColunaSeAusente(conn, "Pedidos", "produto_nome_historico", "TEXT");
        adicionarColunaSeAusente(conn, "Pedidos", "produto_preco_historico", "REAL");

        preencherDadosHistoricos(conn);
    }

    /** v2: índices das colunas usadas pelos filtros do dashboard e das abas de pedidos. */
    private static void migrarPara2Indices(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_pedidos_data_hora ON Pedidos(data_hora)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_pedidos_status ON Pedidos(status)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_pedidos_cliente_id ON Pedidos(cliente_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_pedidos_status_data_hora ON Pedidos(status, data_hora)");
        }
    }

    private static void adicionarColunaSeAusente(Connection conn, String tabela, String coluna, String tipo)
            throws SQLException {
        if (columnExists(conn, tabela, coluna)) {
            return;
        }
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("ALTER TABLE " + tabela + " ADD COLUMN " + coluna + " " + tipo);
            LOG.info("Coluna " + tabela + "." + coluna + " adicionada");
        }
    }

    static boolean columnExists(Connection conn, String tabela, String coluna) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("PRAGMA table_info(" + tabela + ")")) {
            while (rs.next()) {
                if (coluna.equalsIgnoreCase(rs.getString("name"))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Migra pedidos existentes para incluir dados históricos.
     * Adiciona as colunas de histórico se não existirem e preenche com dados atuais.
     *
     * SQL idêntico, byte a byte, ao antigo Database.migrateHistoricalData(Connection):
     * mesmos três níveis de COALESCE e mesmos literais ('Cliente Removido',
     * 'Funcionário Não Informado', 'Produto Removido'). Decisão do dono do projeto:
     * esta tarefa entrega apenas versionamento/transação/idempotência, não deve
     * haver nenhuma mudança de comportamento visível no backfill.
     */
    private static void preencherDadosHistoricos(Connection conn) throws SQLException {
        String updateSql = "UPDATE Pedidos SET "
                + "cliente_nome_historico = COALESCE(cliente_nome_historico, "
                + "  (SELECT nome FROM Clientes WHERE Clientes.id = Pedidos.cliente_id), "
                + "  Pedidos.nome_avulso, 'Cliente Removido'), "
                + "cliente_telefone_historico = COALESCE(cliente_telefone_historico, "
                + "  (SELECT telefone FROM Clientes WHERE Clientes.id = Pedidos.cliente_id), ''), "
                + "cliente_endereco_historico = COALESCE(cliente_endereco_historico, "
                + "  (SELECT endereco || ' ' || predio_casa || ' ' || numero FROM Clientes "
                + "   WHERE Clientes.id = Pedidos.cliente_id), "
                + "  Pedidos.endereco_avulso || ' ' || Pedidos.predio_casa_avulso || ' ' || Pedidos.numero_avulso, ''), "
                + "funcionario_nome_historico = COALESCE(funcionario_nome_historico, "
                + "  (SELECT nome FROM Funcionarios WHERE Funcionarios.id = Pedidos.funcionario_id), "
                + "  'Funcionário Não Informado'), "
                + "produto_nome_historico = COALESCE(produto_nome_historico, "
                + "  (SELECT nome FROM Produtos WHERE Produtos.id = Pedidos.produto_id), "
                + "  'Produto Removido'), "
                + "produto_preco_historico = COALESCE(produto_preco_historico, "
                + "  (SELECT preco FROM Produtos WHERE Produtos.id = Pedidos.produto_id), 0) "
                + "WHERE produto_nome_historico IS NULL OR produto_nome_historico = ''";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(updateSql);
        }
    }
}
