package com.beachbooking.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO per la risposta con informazioni prenotazione.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrenotazioneResponse {

    private UUID id;
    private UUID userId;
    private String userName; // Nome completo utente (opzionale)
    private String userEmail; // Email utente (opzionale)

    private UUID ombrelloneId;
    private Integer ombrelloneNumero; // Numero ombrellone (opzionale)
    private String ombrelloneFila; // Fila ombrellone (opzionale)

    private LocalDate dataInizio;
    private LocalDate dataFine;
    private Integer numeroGiorni;

    private String tipoPrenotazione; // Enum as string
    private BigDecimal prezzoTotale;

    private String stato; // Enum as string
    private String statoDescrizione;

    private String note;
    private String codicePrenotazione;

    private LocalDateTime dataCreazione;
    private LocalDateTime dataAggiornamento;
}