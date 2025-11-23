package com.beachbooking.model.enums;

// ============= PianoAbbonamento.java =============
public enum PianoAbbonamento {
    FREE(0.0, 20, false, false, "Piano gratuito per testare"),
    BASIC(29.0, 50, true, false, "Piano base per piccoli stabilimenti"),
    PRO(79.0, 150, true, true, "Piano professionale"),
    ENTERPRISE(199.0, 999, true, true, "Piano enterprise con supporto dedicato");

    private final Double prezzoMensile;
    private final Integer maxOmbrelloni;
    private final Boolean dominioCustom;
    private final Boolean whiteLabel;
    private final String descrizione;

    PianoAbbonamento(Double prezzoMensile, Integer maxOmbrelloni,
                     Boolean dominioCustom, Boolean whiteLabel, String descrizione) {
        this.prezzoMensile = prezzoMensile;
        this.maxOmbrelloni = maxOmbrelloni;
        this.dominioCustom = dominioCustom;
        this.whiteLabel = whiteLabel;
        this.descrizione = descrizione;
    }

    public Double getPrezzoMensile() { return prezzoMensile; }
    public Integer getMaxOmbrelloni() { return maxOmbrelloni; }
    public Boolean getDominioCustom() { return dominioCustom; }
    public Boolean getWhiteLabel() { return whiteLabel; }
    public String getDescrizione() { return descrizione; }
}