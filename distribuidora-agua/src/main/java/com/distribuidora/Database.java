package com.distribuidora;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

public class Database {

    private static final String APP_DIRECTORY_NAME = ".distribuidora_agua";
    private static final String DB_FILE_NAME = "distribuidora.db";
    private static final Path DB_DIRECTORY = Path.of(System.getProperty("user.home"), APP_DIRECTORY_NAME);
    private static final Path DB_PATH = DB_DIRECTORY.resolve(DB_FILE_NAME);

    private static Path getLegacyDatabasePath() {
        return Path.of(DB_FILE_NAME).toAbsolutePath().normalize();
    }

    public static Path getDatabaseFilePath() {
        return DB_PATH;
    }

    private static void ensureDatabaseDirectoryExists() throws SQLException {
        try {
            Files.createDirectories(DB_DIRECTORY);
        } catch (IOException e) {
            throw new SQLException("Não foi possível preparar o diretório do banco de dados: " + DB_DIRECTORY, e);
        }
    }

    private static void migrateLegacyDatabaseIfNeeded() throws SQLException {
        Path legacyDbPath = getLegacyDatabasePath();
        if (Files.exists(DB_PATH) || !Files.exists(legacyDbPath)) {
            return;
        }

        try {
            Files.move(legacyDbPath, DB_PATH, StandardCopyOption.REPLACE_EXISTING);

            Path legacyWalPath = Path.of(legacyDbPath.toString() + "-wal");
            Path legacyShmPath = Path.of(legacyDbPath.toString() + "-shm");
            Path currentWalPath = Path.of(DB_PATH.toString() + "-wal");
            Path currentShmPath = Path.of(DB_PATH.toString() + "-shm");

            moveIfExists(legacyWalPath, currentWalPath);
            moveIfExists(legacyShmPath, currentShmPath);

            System.out.println("Banco legado migrado para: " + DB_PATH);
        } catch (IOException e) {
            throw new SQLException("Falha ao migrar banco legado para o diretório padrão.", e);
        }
    }

    private static void moveIfExists(Path origem, Path destino) throws IOException {
        if (Files.exists(origem)) {
            Files.move(origem, destino, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void configureConnection(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
            stmt.execute("PRAGMA busy_timeout = 5000;");
            stmt.execute("PRAGMA journal_mode = WAL;");
            stmt.execute("PRAGMA synchronous = NORMAL;");
        }
    }

    private static String escapeForSqlLiteral(String value) {
        return value.replace("'", "''");
    }

    public static Path createBackup(Path destinationPath) throws SQLException, IOException {
        if (destinationPath == null) {
            throw new IllegalArgumentException("O caminho de destino do backup não pode ser nulo.");
        }

        if (!Files.exists(DB_PATH)) {
            throw new SQLException("Arquivo do banco de dados não encontrado em: " + DB_PATH);
        }

        Path destination = destinationPath.toAbsolutePath().normalize();
        Path parent = destination.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.deleteIfExists(destination);

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA wal_checkpoint(FULL);");
            stmt.execute("VACUUM INTO '" + escapeForSqlLiteral(destination.toString()) + "'");
        }

        return destination;
    }

    private static void performDailyBackupIfNeeded() {
        if (!Files.exists(DB_PATH)) {
            return;
        }

        Path backupDir = DB_DIRECTORY.resolve("backups");
        Path backupFile = backupDir.resolve("distribuidora_" + LocalDate.now() + ".db");
        if (Files.exists(backupFile)) {
            return;
        }

        try {
            createBackup(backupFile);
            System.out.println("Backup diário realizado em: " + backupFile);
        } catch (SQLException | IOException e) {
            System.err.println("Erro ao realizar backup do banco de dados: " + e.getMessage());
        }
    }

    /**
     * Estabelece uma conexão com o banco de dados SQLite.
     * 
     * @return Connection ativa para o banco de dados
     * @throws SQLException se houver erro ao conectar
     */
    public static Connection connect() throws SQLException {
        ensureDatabaseDirectoryExists();
        String url = "jdbc:sqlite:" + DB_PATH.toAbsolutePath();
        Connection conn = DriverManager.getConnection(url);
        configureConnection(conn);
        return conn;
    }

    public static void initialize() throws SQLException {
        ensureDatabaseDirectoryExists();
        migrateLegacyDatabaseIfNeeded();
        performDailyBackupIfNeeded();

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
