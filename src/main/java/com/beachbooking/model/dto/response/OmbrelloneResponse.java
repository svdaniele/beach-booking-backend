
package com.beachbooking.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO per la risposta con informazioni ombrellone.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OmbrelloneResponse {

    private UUID id;
    private Integer numero;
    private String fila;
    private String tipo; // Enum as string
    private String tipoDescrizione;
    private String descrizione;
    private Integer posizioneX;
    private Integer posizioneY;
    private Boolean attivo;
    private String note;
    private LocalDateTime dataCreazione;

    // Campi opzionali per lista prenotazioni
    private Boolean disponibile; // Calcolato in base alle prenotazioni
    private String prossimaPrenotazione; // Data prossima prenotazione
}