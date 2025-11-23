package com.beachbooking.model.enums;

public enum StatoPrenotazione {
    PENDING("In attesa di conferma"),
    CONFIRMED("Confermata"),
    PAID("Pagata"),
    CANCELLED("Cancellata"),
    COMPLETED("Completata"),
    REFUNDED("Rimborsata");

    private final String descrizione;

    StatoPrenotazione(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getDescrizione() { return descrizione; }
}