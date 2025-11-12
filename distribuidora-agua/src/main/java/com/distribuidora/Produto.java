package com.distribuidora;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class Produto {

    private SimpleIntegerProperty id;
    private SimpleStringProperty nome;
    private SimpleDoubleProperty preco;

    // Construtor
    public Produto(int id, String nome, double preco) {
        this.id = new SimpleIntegerProperty(id);
        this.nome = new SimpleStringProperty(nome);
        this.preco = new SimpleDoubleProperty(preco);
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

    // Getters e Setters para Preco
    public double getPreco() {
        return preco.get();
    }

    public void setPreco(double preco) {
        this.preco.set(preco);
    }

    public SimpleDoubleProperty precoProperty() {
        return preco;
    }

    @Override
    public String toString() {
        return getNome() + " - R$" + String.format("%.2f", getPreco());
    }
}
