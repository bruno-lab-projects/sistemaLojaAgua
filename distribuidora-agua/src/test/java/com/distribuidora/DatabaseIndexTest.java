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
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseIndexTest {

    @BeforeEach
    void apontarParaDiretorioTemporario(@TempDir Path temp) {
        System.setProperty(Database.DB_DIR_PROPERTY, temp.toString());
    }

    @AfterEach
    void limparPropriedade() {
        System.clearProperty(Database.DB_DIR_PROPERTY);
    }

    @Test
    void criaOsIndicesEsperadosEmPedidos() throws SQLException {
        Database.initialize();

        Set<String> indices = listarIndices();

        assertTrue(indices.contains("idx_pedidos_data_hora"), "índices encontrados: " + indices);
        assertTrue(indices.contains("idx_pedidos_status"), "índices encontrados: " + indices);
        assertTrue(indices.contains("idx_pedidos_cliente_id"), "índices encontrados: " + indices);
        assertTrue(indices.contains("idx_pedidos_status_data_hora"), "índices encontrados: " + indices);
    }

    @Test
    void inicializarDuasVezesNaoLanca() {
        assertDoesNotThrow(() -> {
            Database.initialize();
            Database.initialize();
        });
    }

    @Test
    void consultaPorPeriodoUsaIndice() throws SQLException {
        Database.initialize();

        String plano = explicar(
                "SELECT COUNT(*) FROM Pedidos WHERE data_hora BETWEEN '2026-01-01' AND '2026-12-31'");

        assertTrue(plano.contains("idx_pedidos_data_hora"),
                "A consulta deveria usar o índice de data_hora. Plano: " + plano);
    }

    private Set<String> listarIndices() throws SQLException {
        Set<String> nomes = new HashSet<>();
        try (Connection conn = Database.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT name FROM sqlite_master WHERE type = 'index' AND tbl_name = 'Pedidos'")) {
            while (rs.next()) {
                nomes.add(rs.getString("name"));
            }
        }
        return nomes;
    }

    private String explicar(String sql) throws SQLException {
        StringBuilder plano = new StringBuilder();
        try (Connection conn = Database.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("EXPLAIN QUERY PLAN " + sql)) {
            while (rs.next()) {
                plano.append(rs.getString("detail")).append('\n');
            }
        }
        return plano.toString();
    }
}
