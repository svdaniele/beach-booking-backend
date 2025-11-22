package com.beachbooking.repository;

import com.beachbooking.model.entity.*;
import com.beachbooking.model.enums.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


// ============= OmbrelloneRepository.java =============
@Repository
public interface OmbrelloneRepository extends JpaRepository<Ombrellone, UUID> {

    /**
     * Trova tutti gli ombrelloni di un tenant.
     */
    List<Ombrellone> findByTenantId(UUID tenantId);

    /**
     * Trova tutti gli ombrelloni attivi di un tenant.
     */
    List<Ombrellone> findByTenantIdAndAttivoTrue(UUID tenantId);

    /**
     * Trova un ombrellone specifico di un tenant.
     */
    Optional<Ombrellone> findByIdAndTenantId(UUID id, UUID tenantId);

    /**
     * Trova ombrelloni per numero in un tenant.
     */
    List<Ombrellone> findByTenantIdAndNumero(UUID tenantId, Integer numero);

    /**
     * Trova ombrelloni per fila in un tenant.
     */
    List<Ombrellone> findByTenantIdAndFila(UUID tenantId, String fila);

    /**
     * Trova ombrelloni per tipo in un tenant.
     */
    List<Ombrellone> findByTenantIdAndTipo(UUID tenantId, TipoOmbrellone tipo);

    /**
     * Conta gli ombrelloni di un tenant.
     */
    long countByTenantId(UUID tenantId);

    /**
     * Conta gli ombrelloni attivi di un tenant.
     */
    long countByTenantIdAndAttivoTrue(UUID tenantId);

    /**
     * Verifica se un tenant ha raggiunto il limite di ombrelloni.
     */
    @Query("SELECT COUNT(o) < :maxLimit FROM Ombrellone o " +
            "WHERE o.tenantId = :tenantId AND o.attivo = true")
    boolean canAddMoreOmbrelloni(@Param("tenantId") UUID tenantId,
                                 @Param("maxLimit") Integer maxLimit);

    boolean existsByTenantIdAndNumero(UUID tenantId, Integer numero);
}