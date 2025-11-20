package com.distribuidora;

public enum FormaPagamento {
    DINHEIRO("Dinheiro"),
    PIX("Pix"),
    CARTAO("Cartão"),
    A_PAGAR("À Pagar");
    
    private final String descricao;
    
    FormaPagamento(String descricao) {
        this.descricao = descricao;
    }
    
    @Override
    public String toString() {
        return descricao;
    }
}
