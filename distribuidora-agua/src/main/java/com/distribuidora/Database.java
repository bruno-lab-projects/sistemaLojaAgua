package com.distribuidora;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

public class Database {

    private static String NOME_ARQUIVO_DB = "distribuidora.db";

    /**
     * Estabelece uma conexão com o banco de dados SQLite.
     * 
     * @return Connection ativa para o banco de dados
     * @throws SQLException se houver erro ao conectar
     */
    public static Connection connect() throws SQLException {
        String url = "jdbc:sqlite:" + NOME_ARQUIVO_DB;
        return DriverManager.getConnection(url);
    }

    public static void initialize() throws SQLException {
        String sqlClientes = "CREATE TABLE IF NOT EXISTS Clientes ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "nome TEXT NOT NULL,"
                + "telefone TEXT,"
                + "endereco TEXT,"
                + "predio_casa TEXT,"
                + "numero TEXT,"
                + "observacoes TEXT"
                + ");";

        String sqlFuncionarios = "CREATE TABLE IF NOT EXISTS Funcionarios ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "nome TEXT NOT NULL"
                + ");";

        String sqlProdutos = "CREATE TABLE IF NOT EXISTS Produtos ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "nome TEXT NOT NULL UNIQUE,"
                + "preco REAL"
                + ");";

        String sqlPedidos = "CREATE TABLE IF NOT EXISTS Pedidos ("
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
                + ");";

        // Cria conexão e statement usando try-with-resources
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            // Cria as tabelas
            stmt.execute(sqlClientes);
            stmt.execute(sqlFuncionarios);
            stmt.execute(sqlProdutos);
            stmt.execute(sqlPedidos);
            
            // Migra dados existentes para os novos campos históricos
            migrateHistoricalData(conn);
        }
        // Propaga a exceção para ser tratada pela camada superior
    }

    /**
     * Migra pedidos existentes para incluir dados históricos.
     * Adiciona as colunas de histórico se não existirem e preenche com dados atuais.
     */
    private static void migrateHistoricalData(Connection conn) throws SQLException {
        // Adiciona colunas de histórico se não existirem
        try (Statement stmt = conn.createStatement()) {
            // Tenta adicionar as colunas (se já existirem, SQLite ignora)
            try {
                stmt.execute("ALTER TABLE Pedidos ADD COLUMN cliente_nome_historico TEXT");
            } catch (SQLException e) { /* Coluna já existe */ }
            
            try {
                stmt.execute("ALTER TABLE Pedidos ADD COLUMN cliente_telefone_historico TEXT");
            } catch (SQLException e) { /* Coluna já existe */ }
            
            try {
                stmt.execute("ALTER TABLE Pedidos ADD COLUMN cliente_endereco_historico TEXT");
            } catch (SQLException e) { /* Coluna já existe */ }
            
            try {
                stmt.execute("ALTER TABLE Pedidos ADD COLUMN funcionario_nome_historico TEXT");
            } catch (SQLException e) { /* Coluna já existe */ }
            
            try {
                stmt.execute("ALTER TABLE Pedidos ADD COLUMN produto_nome_historico TEXT");
            } catch (SQLException e) { /* Coluna já existe */ }
            
            try {
                stmt.execute("ALTER TABLE Pedidos ADD COLUMN produto_preco_historico REAL");
            } catch (SQLException e) { /* Coluna já existe */ }
        }
        
        // Preenche dados históricos para pedidos que não os têm
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
