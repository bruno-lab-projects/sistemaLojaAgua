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

    private static final java.util.logging.Logger LOG =
            java.util.logging.Logger.getLogger(Database.class.getName());

    public static final String DB_DIR_PROPERTY = "distribuidora.db.dir";

    private static final String APP_DIRECTORY_NAME = ".distribuidora_agua";
    private static final String DB_FILE_NAME = "distribuidora.db";

    /**
     * Diretório onde ficam banco, backups e logs.
     * A system property {@value #DB_DIR_PROPERTY} sobrescreve o padrão. Ela existe
     * para os testes, que não podem escrever no diretório real do usuário.
     */
    static Path getDatabaseDirectory() {
        String override = System.getProperty(DB_DIR_PROPERTY);
        if (override != null && !override.isBlank()) {
            return Path.of(override);
        }
        return Path.of(System.getProperty("user.home"), APP_DIRECTORY_NAME);
    }

    public static Path getDatabaseFilePath() {
        return getDatabaseDirectory().resolve(DB_FILE_NAME);
    }

    private static Path getLegacyDatabasePath() {
        return Path.of(DB_FILE_NAME).toAbsolutePath().normalize();
    }

    private static void ensureDatabaseDirectoryExists() throws SQLException {
        try {
            Files.createDirectories(getDatabaseDirectory());
        } catch (IOException e) {
            throw new SQLException("Não foi possível preparar o diretório do banco de dados: " + getDatabaseDirectory(), e);
        }
    }

    private static void migrateLegacyDatabaseIfNeeded() throws SQLException {
        Path legacyDbPath = getLegacyDatabasePath();
        Path dbPath = getDatabaseFilePath();
        if (Files.exists(dbPath) || !Files.exists(legacyDbPath)) {
            return;
        }

        try {
            Files.move(legacyDbPath, dbPath, StandardCopyOption.REPLACE_EXISTING);

            Path legacyWalPath = Path.of(legacyDbPath.toString() + "-wal");
            Path legacyShmPath = Path.of(legacyDbPath.toString() + "-shm");
            Path currentWalPath = Path.of(dbPath.toString() + "-wal");
            Path currentShmPath = Path.of(dbPath.toString() + "-shm");

            moveIfExists(legacyWalPath, currentWalPath);
            moveIfExists(legacyShmPath, currentShmPath);

            LOG.info("Banco legado migrado para: " + dbPath);
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

        if (!Files.exists(getDatabaseFilePath())) {
            throw new SQLException("Arquivo do banco de dados não encontrado em: " + getDatabaseFilePath());
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
        if (!Files.exists(getDatabaseFilePath())) {
            return;
        }

        Path backupDir = getDatabaseDirectory().resolve("backups");
        Path backupFile = backupDir.resolve("distribuidora_" + LocalDate.now() + ".db");

        if (!Files.exists(backupFile)) {
            try {
                createBackup(backupFile);
                LOG.info("Backup diário realizado em: " + backupFile);
            } catch (SQLException | IOException e) {
                LOG.log(java.util.logging.Level.WARNING, "Erro ao realizar backup do banco de dados: " + e.getMessage(), e);
                return; // se o backup de hoje falhou, não apaga os antigos
            }
        }

        try {
            com.distribuidora.util.BackupRotation.rotacionar(backupDir, LocalDate.now());
        } catch (IOException | RuntimeException e) {
            LOG.log(java.util.logging.Level.WARNING, "Falha ao rotacionar os backups antigos", e);
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
        String url = "jdbc:sqlite:" + getDatabaseFilePath().toAbsolutePath();
        Connection conn = DriverManager.getConnection(url);
        configureConnection(conn);
        return conn;
    }

    public static void initialize() throws SQLException {
        ensureDatabaseDirectoryExists();
        migrateLegacyDatabaseIfNeeded();
        performDailyBackupIfNeeded();

        // Todo o esquema vive em Migrations, versionado por PRAGMA user_version.
        // A transação garante que o banco nunca fique meio migrado.
        inTransaction(conn -> {
            Migrations.applyAll(conn);
            return null;
        });
    }

    /**
     * Unidade de trabalho executada dentro de uma transação.
     * Recebe a conexão já com autoCommit desligado.
     */
    @FunctionalInterface
    public interface SqlWork<T> {
        T apply(Connection conn) throws SQLException;
    }

    /**
     * Executa o trabalho numa única transação: ou todas as escritas valem, ou nenhuma.
     *
     * Usado hoje pelo executor de migrações, que é o único fluxo multi-passo do
     * sistema. Handlers de controller executam um único UPDATE ou INSERT por ação,
     * o que já é atômico no SQLite, e por isso não precisam deste helper.
     *
     * Rollback também em RuntimeException: um NullPointerException no meio do
     * fluxo não pode deixar escritas pela metade.
     */
    public static <T> T inTransaction(SqlWork<T> trabalho) throws SQLException {
        try (Connection conn = connect()) {
            conn.setAutoCommit(false);
            try {
                T resultado = trabalho.apply(conn);
                conn.commit();
                return resultado;
            } catch (SQLException | RuntimeException e) {
                try {
                    conn.rollback();
                } catch (SQLException falhaNoRollback) {
                    e.addSuppressed(falhaNoRollback);
                }
                throw e;
            } finally {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException falhaAoRestaurarAutoCommit) {
                    LOG.log(java.util.logging.Level.WARNING,
                            "Falha ao restaurar autoCommit após transação: " + falhaAoRestaurarAutoCommit.getMessage(),
                            falhaAoRestaurarAutoCommit);
                }
            }
        }
    }
}
