package com.beachbooking.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagamentoResponse {

    private UUID id;
    private UUID prenotazioneId;
    private String prenotazioneCodice; // Codice prenotazione (opzionale)

    private String metodoPagamento; // Enum as string
    private String metodoPagamentoNome; // Nome human-readable

    private BigDecimal importo;

    private String stato; // Enum as string
    private String statoDescrizione;

    private String riferimentoEsterno;
    private LocalDateTime dataPagamento;

    private String note;

    private LocalDateTime dataCreazione;
    private LocalDateTime dataAggiornamento;
}