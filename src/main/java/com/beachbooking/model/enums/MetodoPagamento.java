package com.beachbooking.model.enums;

public enum MetodoPagamento {
    PAYPAL("PayPal", true),
    BONIFICO("Bonifico Bancario", false),
    CARTA_CREDITO("Carta di Credito", true),
    CONTANTI("Contanti", false);

    private final String nome;
    private final Boolean online;

    MetodoPagamento(String nome, Boolean online) {
        this.nome = nome;
        this.online = online;
    }

    public String getNome() { return nome; }
    public Boolean isOnline() { return online; }
}