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



// ============= PagamentoRepository.java =============
@Repository
public interface PagamentoRepository extends JpaRepository<Pagamento, UUID> {

    Optional<Pagamento> findByPrenotazioneId(UUID prenotazioneId);

    List<Pagamento> findByStato(StatoPrenotazione stato);

    List<Pagamento> findByMetodoPagamento(MetodoPagamento metodo);

    Optional<Pagamento> findByRiferimentoEsterno(String riferimento);

    @Query("SELECT p FROM Pagamento p " +
            "JOIN Prenotazione pr ON p.prenotazioneId = pr.id " +
            "WHERE pr.tenantId = :tenantId")
    List<Pagamento> findByTenantId(@Param("tenantId") UUID tenantId);

    @Query("SELECT COALESCE(SUM(p.importo), 0) FROM Pagamento p " +
            "JOIN Prenotazione pr ON p.prenotazioneId = pr.id " +
            "WHERE pr.tenantId = :tenantId " +
            "AND p.stato = 'PAID'")
    java.math.BigDecimal getTotalPagamentiByTenant(@Param("tenantId") UUID tenantId);
}