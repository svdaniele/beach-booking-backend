package com.beachbooking.model.entity;

import com.beachbooking.model.enums.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;


// ============= Pagamento.java =============
/**
 * Rappresenta un pagamento associato a una prenotazione.
 */
@Entity
@Table(name = "pagamenti",
        indexes = {
                @Index(name = "idx_pagamento_prenotazione", columnList = "prenotazione_id"),
                @Index(name = "idx_pagamento_stato", columnList = "stato")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "prenotazione_id", nullable = false)
    private UUID prenotazioneId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MetodoPagamento metodoPagamento;

    @Column(nullable = false, precision = 10, scale = 2)
    private java.math.BigDecimal importo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatoPrenotazione stato = StatoPrenotazione.PENDING;

    private String riferimentoEsterno; // PayPal transaction ID, ecc.

    private LocalDateTime dataPagamento;

    @Column(length = 1000)
    private String note;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCreazione;

    @UpdateTimestamp
    private LocalDateTime dataAggiornamento;
}