package com.beachbooking.model.dto.response;

import com.beachbooking.model.enums.RuoloUtente;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

// ============= TenantResponse.java =============
/**
 * DTO per la risposta con informazioni tenant.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantResponse {

    private UUID id;
    private String nomeStabilimento;
    private String slug;
    private String descrizione;
    private String indirizzo;
    private String citta;
    private String provincia;
    private String cap;
    private String telefono;
    private String email;
    private String logoUrl;
    private String piano;
    private String stato;
    private LocalDateTime dataCreazione;
    private LocalDateTime dataScadenzaAbbonamento;

    // Statistiche (opzionale)
    private Long numeroOmbrelloni;
    private Long maxOmbrelloni;
}