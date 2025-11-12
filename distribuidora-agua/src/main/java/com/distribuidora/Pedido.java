package com.distribuidora;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class Pedido {

    private SimpleIntegerProperty id;
    private SimpleStringProperty clienteNome;
    private SimpleStringProperty produtoNome;
    private SimpleIntegerProperty quantidade;
    private SimpleStringProperty funcionarioNome;
    private SimpleStringProperty status;
    private SimpleStringProperty dataHora;
    private SimpleDoubleProperty precoTotal;

    // Construtor
    public Pedido(int id, String clienteNome, String produtoNome, int quantidade,
                  String funcionarioNome, String status, String dataHora, double precoTotal) {
        this.id = new SimpleIntegerProperty(id);
        this.clienteNome = new SimpleStringProperty(clienteNome);
        this.produtoNome = new SimpleStringProperty(produtoNome);
        this.quantidade = new SimpleIntegerProperty(quantidade);
        this.funcionarioNome = new SimpleStringProperty(funcionarioNome);
        this.status = new SimpleStringProperty(status);
        this.dataHora = new SimpleStringProperty(dataHora);
        this.precoTotal = new SimpleDoubleProperty(precoTotal);
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

    // Getters e Setters para ClienteNome
    public String getClienteNome() {
        return clienteNome.get();
    }

    public void setClienteNome(String clienteNome) {
        this.clienteNome.set(clienteNome);
    }

    public SimpleStringProperty clienteNomeProperty() {
        return clienteNome;
    }

    // Getters e Setters para ProdutoNome
    public String getProdutoNome() {
        return produtoNome.get();
    }

    public void setProdutoNome(String produtoNome) {
        this.produtoNome.set(produtoNome);
    }

    public SimpleStringProperty produtoNomeProperty() {
        return produtoNome;
    }

    // Getters e Setters para Quantidade
    public int getQuantidade() {
        return quantidade.get();
    }

    public void setQuantidade(int quantidade) {
        this.quantidade.set(quantidade);
    }

    public SimpleIntegerProperty quantidadeProperty() {
        return quantidade;
    }

    // Getters e Setters para FuncionarioNome
    public String getFuncionarioNome() {
        return funcionarioNome.get();
    }

    public void setFuncionarioNome(String funcionarioNome) {
        this.funcionarioNome.set(funcionarioNome);
    }

    public SimpleStringProperty funcionarioNomeProperty() {
        return funcionarioNome;
    }

    // Getters e Setters para Status
    public String getStatus() {
        return status.get();
    }

    public void setStatus(String status) {
        this.status.set(status);
    }

    public SimpleStringProperty statusProperty() {
        return status;
    }

    // Getters e Setters para DataHora
    public String getDataHora() {
        return dataHora.get();
    }

    public void setDataHora(String dataHora) {
        this.dataHora.set(dataHora);
    }

    public SimpleStringProperty dataHoraProperty() {
        return dataHora;
    }

    // Getters e Setters para PrecoTotal
    public double getPrecoTotal() {
        return precoTotal.get();
    }

    public void setPrecoTotal(double precoTotal) {
        this.precoTotal.set(precoTotal);
    }

    public SimpleDoubleProperty precoTotalProperty() {
        return precoTotal;
    }
}
