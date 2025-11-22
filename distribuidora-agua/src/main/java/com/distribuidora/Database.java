package com.distribuidora;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;

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
                + "produto_id INTEGER NOT NULL,"
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
                + "FOREIGN KEY (cliente_id) REFERENCES Clientes(id),"
                + "FOREIGN KEY (funcionario_id) REFERENCES Funcionarios(id),"
                + "FOREIGN KEY (produto_id) REFERENCES Produtos(id)"
                + ");";

        // Cria conexão e statement usando try-with-resources
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            // Cria as tabelas
            stmt.execute(sqlClientes);
            stmt.execute(sqlFuncionarios);
            stmt.execute(sqlProdutos);
            stmt.execute(sqlPedidos);
        }
        // Propaga a exceção para ser tratada pela camada superior
    }
}
