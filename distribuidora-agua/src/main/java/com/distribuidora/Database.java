package com.distribuidora;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;

public class Database {

    private static String NOME_ARQUIVO_DB = "distribuidora.db";

    public static Connection connect() {
        String url = "jdbc:sqlite:" + NOME_ARQUIVO_DB;
        try {
            return DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.err.println("Erro ao conectar ao banco: " + e.getMessage());
            return null;
        }
    }

    public static void initialize() {
        String sqlClientes = "CREATE TABLE IF NOT EXISTS Clientes ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "nome TEXT NOT NULL,"
                + "telefone TEXT,"
                + "endereco TEXT,"
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
                + "data_hora_saiu TEXT NULL,"
                + "data_hora_entregue TEXT NULL,"
                + "FOREIGN KEY (cliente_id) REFERENCES Clientes(id),"
                + "FOREIGN KEY (funcionario_id) REFERENCES Funcionarios(id),"
                + "FOREIGN KEY (produto_id) REFERENCES Produtos(id)"
                + ");";

        // Cria conexão e statement usando try-with-resources
        try (Connection conn = connect()) {
            if (conn == null) {
                System.err.println("Não foi possível conectar ao banco de dados.");
                return;
            }
            try (Statement stmt = conn.createStatement()) {
                // Cria as tabelas
                stmt.execute(sqlClientes);
                stmt.execute(sqlFuncionarios);
                stmt.execute(sqlProdutos);
                stmt.execute(sqlPedidos);

                // Seed dos produtos (plantar dados iniciais)
                String seedProduto1 = "INSERT INTO Produtos (nome, preco) "
                        + "SELECT 'Água Indaia', 19.00 "
                        + "WHERE NOT EXISTS (SELECT 1 FROM Produtos WHERE nome = 'Água Indaia');";
                
                String seedProduto2 = "INSERT INTO Produtos (nome, preco) "
                        + "SELECT 'Água Maiorca', 13.00 "
                        + "WHERE NOT EXISTS (SELECT 1 FROM Produtos WHERE nome = 'Água Maiorca');";
                
                stmt.execute(seedProduto1);
                stmt.execute(seedProduto2);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao inicializar o banco: " + e.getMessage());
        }
    }
}
