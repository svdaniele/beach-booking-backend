package com.beachbooking.model.dto.response;

import com.beachbooking.model.enums.RuoloUtente;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

// ============= UserResponse.java =============
/**
 * DTO per la risposta con informazioni utente.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private UUID id;
    private String email;
    private String nome;
    private String cognome;
    private String nomeCompleto;
    private String telefono;
    private RuoloUtente ruolo;
    private Boolean attivo;
    private Boolean emailVerificata;
    private UUID tenantId;
    private LocalDateTime dataRegistrazione;
    private LocalDateTime ultimoAccesso;
}