package com.beachbooking.model.entity;

import com.beachbooking.model.enums.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;

// ============= Tenant.java =============
/**
 * Rappresenta uno stabilimento balneare nella piattaforma.
 * Ogni tenant è isolato dagli altri.
 */
@Entity
@Table(name = "tenants", indexes = {
        @Index(name = "idx_tenant_slug", columnList = "slug"),
        @Index(name = "idx_tenant_stato", columnList = "stato")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String nomeStabilimento;

    @Column(nullable = false, unique = true, length = 100)
    private String slug; // es: "lido-marechiaro"

    @Column(length = 255)
    private String dominioCustom; // es: "www.lidomarechiaro.it"

    @Column(length = 500)
    private String descrizione;

    @Column(nullable = false, length = 255)
    private String indirizzo;

    @Column(nullable = false, length = 100)
    private String citta;

    @Column(nullable = false, length = 100)
    private String provincia;

    @Column(nullable = false, length = 50)
    private String cap;

    @Column(length = 20)
    private String telefono;

    @Column(nullable = false, length = 150)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PianoAbbonamento piano = PianoAbbonamento.FREE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatoTenant stato = StatoTenant.TRIAL;

    // Configurazione personalizzata (JSON)
    @Column(columnDefinition = "TEXT")
    private String configurazione; // Colori, logo, impostazioni

    private String logoUrl;

    @Column(length = 34)
    private String iban; // Per pagamenti bonifico

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCreazione;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime dataAggiornamento;

    private LocalDateTime dataScadenzaAbbonamento;

    @PrePersist
    public void prePersist() {
        if (slug == null && nomeStabilimento != null) {
            slug = nomeStabilimento.toLowerCase()
                    .replaceAll("[^a-z0-9\\s-]", "")
                    .replaceAll("\\s+", "-");
        }
    }
}