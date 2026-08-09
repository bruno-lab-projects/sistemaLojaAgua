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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DatabaseTransactionTest {

    @BeforeEach
    void prepararBanco(@TempDir Path temp) throws SQLException {
        System.setProperty(Database.DB_DIR_PROPERTY, temp.toString());
        Database.initialize();
    }

    @AfterEach
    void limparPropriedade() {
        System.clearProperty(Database.DB_DIR_PROPERTY);
    }

    @Test
    void confirmaTodasAsEscritasQuandoNaoHaFalha() throws SQLException {
        Database.inTransaction(conn -> {
            inserirFuncionario(conn, "Ana");
            inserirFuncionario(conn, "Bruno");
            return null;
        });

        assertEquals(2, contarFuncionarios());
    }

    @Test
    void desfazTodasAsEscritasQuandoOcorreSQLException() throws SQLException {
        assertThrows(SQLException.class, () ->
                Database.inTransaction(conn -> {
                    inserirFuncionario(conn, "Ana");
                    inserirFuncionario(conn, "Bruno");
                    throw new SQLException("falha simulada no meio do fluxo");
                }));

        assertEquals(0, contarFuncionarios(), "nenhum funcionário deveria ter sido gravado");
    }

    @Test
    void desfazTodasAsEscritasQuandoOcorreRuntimeException() throws SQLException {
        assertThrows(IllegalStateException.class, () ->
                Database.inTransaction(conn -> {
                    inserirFuncionario(conn, "Ana");
                    throw new IllegalStateException("erro de lógica no meio do fluxo");
                }));

        assertEquals(0, contarFuncionarios());
    }

    @Test
    void devolveOValorRetornadoPeloTrabalho() throws SQLException {
        int resultado = Database.inTransaction(conn -> {
            inserirFuncionario(conn, "Ana");
            return 42;
        });

        assertEquals(42, resultado);
    }

    private void inserirFuncionario(Connection conn, String nome) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("INSERT INTO Funcionarios (nome) VALUES ('" + nome + "')");
        }
    }

    private int contarFuncionarios() throws SQLException {
        try (Connection conn = Database.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Funcionarios")) {
            return rs.next() ? rs.getInt(1) : -1;
        }
    }
}
