package com.beachbooking.model.enums;

public enum StatoTenant {
    TRIAL("Periodo di prova"),
    ACTIVE("Attivo"),
    SUSPENDED("Sospeso per mancato pagamento"),
    CANCELLED("Cancellato");

    private final String descrizione;

    StatoTenant(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getDescrizione() { return descrizione; }
}