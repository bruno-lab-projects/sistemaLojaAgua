package com.distribuidora;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Cliente {

    private SimpleIntegerProperty id;
    private SimpleStringProperty nome;
    private SimpleStringProperty telefone;
    private SimpleStringProperty endereco;
    private SimpleStringProperty predioCasa;
    private SimpleStringProperty numero;
    private SimpleStringProperty observacoes;

    // Construtor
    public Cliente(int id, String nome, String telefone, String endereco, String predioCasa, String numero, String observacoes) {
        this.id = new SimpleIntegerProperty(id);
        this.nome = new SimpleStringProperty(nome);
        this.telefone = new SimpleStringProperty(telefone);
        this.endereco = new SimpleStringProperty(endereco);
        this.predioCasa = new SimpleStringProperty(predioCasa);
        this.numero = new SimpleStringProperty(numero);
        this.observacoes = new SimpleStringProperty(observacoes);
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

    // Getters e Setters para Telefone
    public String getTelefone() {
        return telefone.get();
    }

    public void setTelefone(String telefone) {
        this.telefone.set(telefone);
    }

    public SimpleStringProperty telefoneProperty() {
        return telefone;
    }

    // Getters e Setters para Endereco
    public String getEndereco() {
        return endereco.get();
    }

    public void setEndereco(String endereco) {
        this.endereco.set(endereco);
    }

    public SimpleStringProperty enderecoProperty() {
        return endereco;
    }

    // Getters e Setters para Predio/Casa
    public String getPredioCasa() {
        return predioCasa.get();
    }

    public void setPredioCasa(String predioCasa) {
        this.predioCasa.set(predioCasa);
    }

    public SimpleStringProperty predioCasaProperty() {
        return predioCasa;
    }

    // Getters e Setters para Numero
    public String getNumero() {
        return numero.get();
    }

    public void setNumero(String numero) {
        this.numero.set(numero);
    }

    public SimpleStringProperty numeroProperty() {
        return numero;
    }

    // Getters e Setters para Observacoes
    public String getObservacoes() {
        return observacoes.get();
    }

    public void setObservacoes(String observacoes) {
        this.observacoes.set(observacoes);
    }

    public SimpleStringProperty observacoesProperty() {
        return observacoes;
    }

    @Override
    public String toString() {
        return getNome();
    }
}
