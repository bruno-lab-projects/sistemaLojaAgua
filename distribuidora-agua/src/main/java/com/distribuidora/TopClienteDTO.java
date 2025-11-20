package com.distribuidora;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

public class TopClienteDTO {
    private final SimpleStringProperty nome;
    private final SimpleIntegerProperty quantidade;
    private final SimpleDoubleProperty valor;

    public TopClienteDTO(String nome, int quantidade, double valor) {
        this.nome = new SimpleStringProperty(nome);
        this.quantidade = new SimpleIntegerProperty(quantidade);
        this.valor = new SimpleDoubleProperty(valor);
    }

    // Getters
    public String getNome() {
        return nome.get();
    }

    public int getQuantidade() {
        return quantidade.get();
    }

    public double getValor() {
        return valor.get();
    }

    // Property getters (para TableView)
    public SimpleStringProperty nomeProperty() {
        return nome;
    }

    public SimpleIntegerProperty quantidadeProperty() {
        return quantidade;
    }

    public SimpleDoubleProperty valorProperty() {
        return valor;
    }
}
