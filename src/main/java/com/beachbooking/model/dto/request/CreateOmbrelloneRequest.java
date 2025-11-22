package com.beachbooking.model.dto.request;

import com.beachbooking.model.enums.TipoOmbrellone;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// ============= CreateOmbrelloneRequest.java =============
/**
 * DTO per la creazione di un nuovo ombrellone.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOmbrelloneRequest {

    @NotNull(message = "Numero ombrellone è obbligatorio")
    @Min(value = 1, message = "Numero deve essere almeno 1")
    @Max(value = 9999, message = "Numero non può superare 9999")
    private Integer numero;

    @NotBlank(message = "Fila è obbligatoria")
    @Size(max = 10, message = "Fila non può superare 10 caratteri")
    private String fila;

    private TipoOmbrellone tipo; // Default: STANDARD

    @Size(max = 500, message = "Descrizione non può superare 500 caratteri")
    private String descrizione;

    @Min(value = 0, message = "Posizione X non può essere negativa")
    private Integer posizioneX;

    @Min(value = 0, message = "Posizione Y non può essere negativa")
    private Integer posizioneY;
}