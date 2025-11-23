package com.beachbooking.model.enums;

public enum TipoPrenotazione {
    GIORNALIERA(1, "Prenotazione giornaliera"),
    SETTIMANALE(7, "Prenotazione settimanale"),
    MENSILE(30, "Prenotazione mensile"),
    ANNUALE(365, "Prenotazione annuale");

    private final Integer giorniDefault;
    private final String descrizione;

    TipoPrenotazione(Integer giorniDefault, String descrizione) {
        this.giorniDefault = giorniDefault;
        this.descrizione = descrizione;
    }

    public Integer getGiorniDefault() { return giorniDefault; }
    public String getDescrizione() { return descrizione; }
}