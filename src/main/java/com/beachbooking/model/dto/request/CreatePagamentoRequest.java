package com.beachbooking.model.dto.request;

import com.beachbooking.model.enums.MetodoPagamento;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

// ============= CreatePagamentoRequest.java =============
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePagamentoRequest {

    @NotNull(message = "ID prenotazione è obbligatorio")
    private UUID prenotazioneId;

    @NotNull(message = "Metodo pagamento è obbligatorio")
    private MetodoPagamento metodoPagamento;

    @NotNull(message = "Importo è obbligatorio")
    @DecimalMin(value = "0.01", message = "Importo deve essere maggiore di 0")
    private BigDecimal importo;

    private String riferimentoEsterno; // PayPal ID, CRO bonifico, etc

    @Size(max = 1000, message = "Note non possono superare 1000 caratteri")
    private String note;
}