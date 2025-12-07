package com.beachbooking.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO per il cambio password.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {

    @NotBlank(message = "Password attuale è obbligatoria")
    private String oldPassword;

    @NotBlank(message = "Nuova password è obbligatoria")
    @Size(min = 8, message = "Password deve essere di almeno 8 caratteri")
    private String newPassword;

    @NotBlank(message = "Conferma password è obbligatoria")
    private String confirmPassword;

    public String getCurrentPassword() {
        return "";
    }
}