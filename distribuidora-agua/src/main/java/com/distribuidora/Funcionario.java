package com.distribuidora;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Funcionario {

    private SimpleIntegerProperty id;
    private SimpleStringProperty nome;

    // Construtor
    public Funcionario(int id, String nome) {
        this.id = new SimpleIntegerProperty(id);
        this.nome = new SimpleStringProperty(nome != null ? nome : "");
    }

    // Getters e Setters para ID
    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public SimpleIntegerProperty idProperty() {
        return id;
    }

    // Getters e Setters para Nome
    public String getNome() {
        return nome.get();
    }

    public void setNome(String nome) {
        this.nome.set(nome);
    }

    public SimpleStringProperty nomeProperty() {
        return nome;
    }

    @Override
    public String toString() {
        return getNome();
    }
}
