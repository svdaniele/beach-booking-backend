package com.beachbooking.model.enums;

public enum RuoloUtente {
    SUPER_ADMIN("Amministratore piattaforma"),
    TENANT_ADMIN("Proprietario stabilimento"),
    STAFF("Staff stabilimento"),
    CUSTOMER("Cliente");

    private final String descrizione;

    RuoloUtente(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getDescrizione() { return descrizione; }
}