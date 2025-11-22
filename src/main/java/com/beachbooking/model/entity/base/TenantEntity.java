package com.beachbooking.model.entity.base;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Classe base per tutte le entità multi-tenant.
 * Ogni entità che estende questa classe avrà automaticamente:
 * - Un campo tenantId per l'isolamento dei dati
 * - Campi di auditing (createdAt, updatedAt)
 * - ID di tipo UUID
 */
@MappedSuperclass
@Getter
@Setter
public abstract class TenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Campo fondamentale per il multi-tenancy.
     * Ogni record appartiene a un tenant specifico.
     */
    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Questo metodo viene chiamato automaticamente prima di salvare l'entità.
     * Assicura che il tenantId sia sempre impostato.
     */
    @PrePersist
    public void prePersist() {
        if (this.tenantId == null) {
            // Il TenantContext verrà impostato dal TenantInterceptor
            this.tenantId = com.beachbooking.tenant.TenantContext.getTenantId();
        }
    }

    /**
     * Previene la modifica del tenantId dopo la creazione.
     */
    @PreUpdate
    public void preUpdate() {
        if (this.tenantId == null) {
            throw new IllegalStateException("TenantId cannot be null on update");
        }
    }
}