package com.distribuidora;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class Pedido {

    private SimpleIntegerProperty id;
    private SimpleStringProperty clienteNome;
    private SimpleStringProperty produtoNome;
    private SimpleIntegerProperty quantidade;
    private SimpleStringProperty funcionarioNome;
    private SimpleStringProperty status;
    private SimpleStringProperty hora;
    private SimpleStringProperty endereco;
    private SimpleDoubleProperty precoTotal;
    private ObjectProperty<FormaPagamento> formaPagamento;

    // Construtor
    public Pedido(int id, String clienteNome, String produtoNome, int quantidade,
                  String funcionarioNome, String status, String hora, String endereco, double precoTotal,
                  FormaPagamento pagamento) {
        this.id = new SimpleIntegerProperty(id);
        this.clienteNome = new SimpleStringProperty(clienteNome);
        this.produtoNome = new SimpleStringProperty(produtoNome);
        this.quantidade = new SimpleIntegerProperty(quantidade);
        this.funcionarioNome = new SimpleStringProperty(funcionarioNome);
        this.status = new SimpleStringProperty(status);
        this.hora = new SimpleStringProperty(hora);
        this.endereco = new SimpleStringProperty(endereco);
        this.precoTotal = new SimpleDoubleProperty(precoTotal);
        this.formaPagamento = new SimpleObjectProperty<>(pagamento);
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

    // Getters e Setters para Hora
    public String getHora() {
        return hora.get();
    }

    public void setHora(String hora) {
        this.hora.set(hora);
    }

    public SimpleStringProperty horaProperty() {
        return hora;
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

    // Getters e Setters para FormaPagamento
    public FormaPagamento getFormaPagamento() {
        return formaPagamento.get();
    }

    public void setFormaPagamento(FormaPagamento formaPagamento) {
        this.formaPagamento.set(formaPagamento);
    }

    public ObjectProperty<FormaPagamento> formaPagamentoProperty() {
        return formaPagamento;
    }
}
