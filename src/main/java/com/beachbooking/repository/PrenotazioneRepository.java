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




// ============= PrenotazioneRepository.java =============
@Repository
public interface PrenotazioneRepository extends JpaRepository<Prenotazione, UUID> {

    /**
     * Trova tutte le prenotazioni di un tenant.
     */
    List<Prenotazione> findByTenantId(UUID tenantId);

    /**
     * Trova prenotazioni per utente.
     */
    List<Prenotazione> findByUserId(UUID userId);

    /**
     * Trova prenotazioni per utente e tenant.
     */
    List<Prenotazione> findByUserIdAndTenantId(UUID userId, UUID tenantId);

    /**
     * Trova prenotazioni per ombrellone.
     */
    List<Prenotazione> findByOmbrelloneId(UUID ombrelloneId);

    /**
     * Trova prenotazione specifica di un tenant.
     */
    Optional<Prenotazione> findByIdAndTenantId(UUID id, UUID tenantId);

    /**
     * Trova per codice prenotazione.
     */
    Optional<Prenotazione> findByCodicePrenotazione(String codice);

    /**
     * Trova prenotazioni per stato.
     */
    List<Prenotazione> findByTenantIdAndStato(UUID tenantId, StatoPrenotazione stato);

    /**
     * Trova prenotazioni in un range di date.
     */
    @Query("SELECT p FROM Prenotazione p " +
            "WHERE p.tenantId = :tenantId " +
            "AND p.dataInizio <= :dataFine " +
            "AND p.dataFine >= :dataInizio " +
            "ORDER BY p.dataInizio ASC")
    List<Prenotazione> findByTenantIdAndDateRange(
            @Param("tenantId") UUID tenantId,
            @Param("dataInizio") LocalDate dataInizio,
            @Param("dataFine") LocalDate dataFine
    );

    /**
     * Verifica se un ombrellone è disponibile in un periodo.
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN false ELSE true END " +
            "FROM Prenotazione p " +
            "WHERE p.ombrelloneId = :ombrelloneId " +
            "AND p.stato NOT IN ('CANCELLED', 'REFUNDED') " +
            "AND ((p.dataInizio <= :dataInizio AND p.dataFine >= :dataInizio) " +
            "OR (p.dataInizio <= :dataFine AND p.dataFine >= :dataFine) " +
            "OR (p.dataInizio >= :dataInizio AND p.dataFine <= :dataFine))")
    boolean isOmbrelloneDisponibile(
            @Param("ombrelloneId") UUID ombrelloneId,
            @Param("dataInizio") LocalDate dataInizio,
            @Param("dataFine") LocalDate dataFine
    );

    /**
     * Trova ombrelloni disponibili per un tenant in un periodo.
     */
    @Query("SELECT o FROM Ombrellone o " +
            "WHERE o.tenantId = :tenantId " +
            "AND o.attivo = true " +
            "AND o.id NOT IN (" +
            "    SELECT p.ombrelloneId FROM Prenotazione p " +
            "    WHERE p.tenantId = :tenantId " +
            "    AND p.stato NOT IN ('CANCELLED', 'REFUNDED') " +
            "    AND ((p.dataInizio <= :dataInizio AND p.dataFine >= :dataInizio) " +
            "    OR (p.dataInizio <= :dataFine AND p.dataFine >= :dataFine) " +
            "    OR (p.dataInizio >= :dataInizio AND p.dataFine <= :dataFine))" +
            ")")
    List<Ombrellone> findOmbrelloniDisponibili(
            @Param("tenantId") UUID tenantId,
            @Param("dataInizio") LocalDate dataInizio,
            @Param("dataFine") LocalDate dataFine
    );

    /**
     * Statistiche: numero prenotazioni per tenant.
     */
    @Query("SELECT COUNT(p) FROM Prenotazione p " +
            "WHERE p.tenantId = :tenantId " +
            "AND p.stato = :stato")
    long countByTenantIdAndStato(@Param("tenantId") UUID tenantId,
                                 @Param("stato") StatoPrenotazione stato);

    /**
     * Statistiche: revenue totale per tenant.
     */
    @Query("SELECT COALESCE(SUM(p.prezzoTotale), 0) FROM Prenotazione p " +
            "WHERE p.tenantId = :tenantId " +
            "AND p.stato IN ('PAID', 'COMPLETED')")
    java.math.BigDecimal getTotalRevenue(@Param("tenantId") UUID tenantId);

    /**
     * Prenotazioni attive (in corso oggi).
     */
    @Query("SELECT p FROM Prenotazione p " +
            "WHERE p.tenantId = :tenantId " +
            "AND p.dataInizio <= CURRENT_DATE " +
            "AND p.dataFine >= CURRENT_DATE " +
            "AND p.stato IN ('CONFIRMED', 'PAID')")
    List<Prenotazione> findPrenotazioniAttive(@Param("tenantId") UUID tenantId);
}