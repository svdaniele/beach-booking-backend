package com.beachbooking.model.entity;

import com.beachbooking.model.enums.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;




// ============= Ombrellone.java =============
/**
 * Rappresenta un ombrellone dello stabilimento.
 */
@Entity
@Table(name = "ombrelloni",
        indexes = {
                @Index(name = "idx_ombrellone_tenant", columnList = "tenant_id"),
                @Index(name = "idx_ombrellone_numero", columnList = "tenant_id, numero")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_tenant_numero", columnNames = {"tenant_id", "numero"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ombrellone {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private Integer numero;

    @Column(nullable = false, length = 10)
    private String fila; // A, B, C, etc.

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoOmbrellone tipo = TipoOmbrellone.STANDARD;

    @Column(length = 500)
    private String descrizione;

    // Posizione sulla mappa (coordinate in pixel o percentuale)
    private Integer posizioneX;
    private Integer posizioneY;

    @Column(nullable = false)
    private Boolean attivo = true;

    private String note;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCreazione;

    @UpdateTimestamp
    private LocalDateTime dataAggiornamento;

    @PrePersist
    @PreUpdate
    public void validateTenant() {
        if (tenantId == null) {
            throw new IllegalStateException("TenantId is required for Ombrellone");
        }
    }
}