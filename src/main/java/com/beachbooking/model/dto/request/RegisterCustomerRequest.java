package com.beachbooking.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO per la registrazione di un nuovo cliente.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterCustomerRequest {

    @NotBlank(message = "Email è obbligatoria")
    @Email(message = "Email non valida")
    private String email;

    @NotBlank(message = "Password è obbligatoria")
    @Size(min = 8, message = "Password deve essere di almeno 8 caratteri")
    private String password;

    @NotBlank(message = "Nome è obbligatorio")
    @Size(min = 2, max = 100, message = "Nome deve essere tra 2 e 100 caratteri")
    private String nome;

    @NotBlank(message = "Cognome è obbligatorio")
    @Size(min = 2, max = 100, message = "Cognome deve essere tra 2 e 100 caratteri")
    private String cognome;

    @NotBlank(message = "Telefono è obbligatorio")
    @Size(min = 10, max = 20, message = "Telefono deve essere tra 10 e 20 caratteri")
    private String telefono;
}