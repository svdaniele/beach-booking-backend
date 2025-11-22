package com.beachbooking.model.entity;

import com.beachbooking.model.enums.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;

// ============= Prenotazione.java =============
/**
 * Rappresenta una prenotazione di un ombrellone.
 */
@Entity
@Table(name = "prenotazioni",
        indexes = {
                @Index(name = "idx_prenotazione_tenant", columnList = "tenant_id"),
                @Index(name = "idx_prenotazione_user", columnList = "user_id"),
                @Index(name = "idx_prenotazione_ombrellone", columnList = "ombrellone_id"),
                @Index(name = "idx_prenotazione_date", columnList = "data_inizio, data_fine"),
                @Index(name = "idx_prenotazione_stato", columnList = "stato")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prenotazione {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "ombrellone_id", nullable = false)
    private UUID ombrelloneId;

    @Column(nullable = false)
    private java.time.LocalDate dataInizio;

    @Column(nullable = false)
    private java.time.LocalDate dataFine;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoPrenotazione tipoPrenotazione;

    @Column(nullable = false, precision = 10, scale = 2)
    private java.math.BigDecimal prezzoTotale;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatoPrenotazione stato = StatoPrenotazione.PENDING;

    @Column(length = 1000)
    private String note;

    private String codicePrenotazione; // Codice univoco per il cliente

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCreazione;

    @UpdateTimestamp
    private LocalDateTime dataAggiornamento;

    @PrePersist
    public void generateCodice() {
        if (codicePrenotazione == null) {
            codicePrenotazione = "BK" + System.currentTimeMillis() +
                    (int)(Math.random() * 1000);
        }
    }

    public Integer getNumeroGiorni() {
        return (int) java.time.temporal.ChronoUnit.DAYS.between(dataInizio, dataFine) + 1;
    }
}