package com.beachbooking.repository;

import com.beachbooking.model.entity.*;
import com.beachbooking.model.enums.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// ============= TenantRepository.java =============
@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID> {

    Optional<Tenant> findBySlug(String slug);

    Optional<Tenant> findByDominioCustom(String dominio);

    Optional<Tenant> findByEmail(String email);

    List<Tenant> findByStato(StatoTenant stato);

    List<Tenant> findByPiano(PianoAbbonamento piano);

    @Query("SELECT t FROM Tenant t WHERE t.dataScadenzaAbbonamento < CURRENT_DATE " +
            "AND t.stato = 'ACTIVE'")
    List<Tenant> findTenantScaduti();

    boolean existsBySlug(String slug);

    boolean existsByEmail(String email);
}