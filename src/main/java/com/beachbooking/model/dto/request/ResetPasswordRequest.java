package com.beachbooking.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO per il reset della password.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {

    @NotBlank(message = "Token è obbligatorio")
    private String token;

    @NotBlank(message = "Nuova password è obbligatoria")
    @Size(min = 8, message = "Password deve essere di almeno 8 caratteri")
    private String newPassword;
}