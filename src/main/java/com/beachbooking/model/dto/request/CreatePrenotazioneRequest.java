package com.beachbooking.model.dto.request;

import com.beachbooking.model.enums.TipoPrenotazione;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

// ============= CreatePrenotazioneRequest.java =============
/**
 * DTO per la creazione di una nuova prenotazione.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePrenotazioneRequest {

    /**
     * ID utente per cui creare la prenotazione.
     * Opzionale: se null, viene usato l'utente corrente.
     * Solo staff/admin possono specificare un userId diverso dal proprio.
     */
    private UUID userId;

    @NotNull(message = "ID ombrellone è obbligatorio")
    private UUID ombrelloneId;

    @NotNull(message = "Data inizio è obbligatoria")
    @FutureOrPresent(message = "Data inizio non può essere nel passato")
    private LocalDate dataInizio;

    @NotNull(message = "Data fine è obbligatoria")
    @FutureOrPresent(message = "Data fine non può essere nel passato")
    private LocalDate dataFine;

    @NotNull(message = "Tipo prenotazione è obbligatorio")
    private TipoPrenotazione tipoPrenotazione;

    @Size(max = 1000, message = "Le note non possono superare 1000 caratteri")
    private String note;

    /**
     * Validazione custom: data fine deve essere dopo data inizio.
     */
    @AssertTrue(message = "Data fine deve essere dopo o uguale a data inizio")
    public boolean isDataFineValid() {
        if (dataInizio == null || dataFine == null) {
            return true; // Sarà gestito da @NotNull
        }
        return !dataFine.isBefore(dataInizio);
    }
}