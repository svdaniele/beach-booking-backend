package com.beachbooking.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO per risposta errore generico.
 * Usato da GlobalExceptionHandler per tutti gli errori.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    /**
     * Codice HTTP status (es: 400, 404, 500).
     */
    private Integer status;

    /**
     * Tipo di errore (es: "Bad Request", "Not Found").
     */
    private String error;

    /**
     * Messaggio descrittivo dell'errore.
     */
    private String message;

    /**
     * Path dell'endpoint che ha generato l'errore.
     */
    private String path;

    /**
     * Timestamp dell'errore.
     */
    private LocalDateTime timestamp;

    /**
     * Factory method per creare rapidamente un ErrorResponse.
     */
    public static ErrorResponse of(Integer status, String error, String message, String path) {
        return ErrorResponse.builder()
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }
}