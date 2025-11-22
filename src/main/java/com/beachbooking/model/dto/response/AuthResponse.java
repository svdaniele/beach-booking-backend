package com.beachbooking.model.dto.response;

import com.beachbooking.model.enums.RuoloUtente;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

// ============= AuthResponse.java =============
/**
 * DTO per la risposta di autenticazione (login).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;
    private String type = "Bearer";
    private UserInfo user;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserInfo {
        private UUID id;
        private String email;
        private String nome;
        private String cognome;
        private String nomeCompleto;
        private RuoloUtente ruolo;
        private UUID tenantId;
        private String tenantSlug;
        private String tenantNome;
        private Boolean emailVerificata;
    }
}