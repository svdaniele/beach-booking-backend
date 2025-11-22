package com.beachbooking.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO per la registrazione di un nuovo tenant (stabilimento).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterTenantRequest {

    // Dati stabilimento
    @NotBlank(message = "Nome stabilimento è obbligatorio")
    @Size(min = 3, max = 200, message = "Nome deve essere tra 3 e 200 caratteri")
    private String nomeStabilimento;

    @NotBlank(message = "Indirizzo è obbligatorio")
    private String indirizzo;

    @NotBlank(message = "Città è obbligatoria")
    private String citta;

    @NotBlank(message = "Provincia è obbligatoria")
    @Size(min = 2, max = 2, message = "Provincia deve essere di 2 caratteri (es: NA)")
    private String provincia;

    @NotBlank(message = "CAP è obbligatorio")
    @Size(min = 5, max = 5, message = "CAP deve essere di 5 cifre")
    private String cap;

    @NotBlank(message = "Telefono è obbligatorio")
    private String telefono;

    // Dati amministratore
    @NotBlank(message = "Email è obbligatoria")
    @Email(message = "Email non valida")
    private String email;

    @NotBlank(message = "Password è obbligatoria")
    @Size(min = 8, message = "Password deve essere di almeno 8 caratteri")
    private String password;

    @NotBlank(message = "Nome amministratore è obbligatorio")
    private String nomeAdmin;

    @NotBlank(message = "Cognome amministratore è obbligatorio")
    private String cognomeAdmin;
}