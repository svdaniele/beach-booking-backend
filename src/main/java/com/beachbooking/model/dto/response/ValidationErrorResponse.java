package com.beachbooking.model.dto.response;

import com.beachbooking.model.enums.RuoloUtente;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * DTO per errori di validazione con dettagli campo per campo.
 * Estende ErrorResponse aggiungendo una mappa di errori per campo.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationErrorResponse extends ErrorResponse {

    /**
     * Mappa campo -> messaggio errore.
     * Es: {"email": "Email non valida", "password": "Password troppo corta"}
     */
    private Map<String, String> fieldErrors;

    public ValidationErrorResponse(Integer status, String error, String message,
                                   String path, LocalDateTime timestamp,
                                   Map<String, String> fieldErrors) {
        super(status, error, message, path, timestamp);
        this.fieldErrors = fieldErrors;
    }

    /**
     * Factory method per errori di validazione.
     */
    public static ValidationErrorResponse of(Integer status, String error, String message,
                                             String path, Map<String, String> fieldErrors) {
        return new ValidationErrorResponse(
                status,
                error,
                message,
                path,
                LocalDateTime.now(),
                fieldErrors
        );
    }
}