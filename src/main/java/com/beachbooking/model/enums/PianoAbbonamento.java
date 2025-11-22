// java
package com.beachbooking.model.enums;

import java.math.BigDecimal;

public enum PianoAbbonamento {
    FREE(10, BigDecimal.ZERO),
    BASIC(50, new BigDecimal("9.99")),
    PRO(200, new BigDecimal("29.99")),
    ENTERPRISE(Integer.MAX_VALUE, new BigDecimal("99.99"));

    private final int maxOmbrelloni;
    private final BigDecimal prezzoMensile;

    PianoAbbonamento(int maxOmbrelloni, BigDecimal prezzoMensile) {
        this.maxOmbrelloni = maxOmbrelloni;
        this.prezzoMensile = prezzoMensile;
    }

    public int getMaxOmbrelloni() {
        return maxOmbrelloni;
    }

    public BigDecimal getPrezzoMensile() {
        return prezzoMensile;
    }
}