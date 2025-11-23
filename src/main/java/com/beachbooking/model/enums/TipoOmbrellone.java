package com.beachbooking.model.enums;

public enum TipoOmbrellone {
    STANDARD("Ombrellone standard", 1.0),
    PREMIUM("Ombrellone premium con lettini", 1.5),
    VIP("Ombrellone VIP con servizi extra", 2.0),
    FAMILY("Ombrellone famiglia XL", 1.8);

    private final String descrizione;
    private final Double moltiplicatorePrezzo;

    TipoOmbrellone(String descrizione, Double moltiplicatorePrezzo) {
        this.descrizione = descrizione;
        this.moltiplicatorePrezzo = moltiplicatorePrezzo;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public Double getMoltiplicatorePrezzo() {
        return moltiplicatorePrezzo;
    }
}