package com.beachbooking.controller;

import com.beachbooking.model.dto.response.MessageResponse;
import com.beachbooking.model.dto.response.TenantResponse;
import com.beachbooking.model.entity.Tenant;
import com.beachbooking.model.entity.User;
import com.beachbooking.model.enums.StatoTenant;
import com.beachbooking.repository.*;
import com.beachbooking.service.TenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller per amministrazione della piattaforma.
 * Tutti gli endpoint richiedono ruolo SUPER_ADMIN.
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('SUPER_ADMIN')")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AdminController {

    @Autowired
    private TenantService tenantService;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OmbrelloneRepository ombrelloneRepository;

    @Autowired
    private PrenotazioneRepository prenotazioneRepository;

    @Autowired
    private PagamentoRepository pagamentoRepository;

    /**
     * GET /api/admin/dashboard
     * Statistiche generali della piattaforma.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStats> getDashboardStats() {

        long totalTenants = tenantRepository.count();
        long activeTenants = tenantRepository.findByStato(StatoTenant.ACTIVE).size();
        long trialTenants = tenantRepository.findByStato(StatoTenant.TRIAL).size();
        long suspendedTenants = tenantRepository.findByStato(StatoTenant.SUSPENDED).size();

        long totalUsers = userRepository.count();
        long totalOmbrelloni = ombrelloneRepository.count();
        long totalPrenotazioni = prenotazioneRepository.count();

        // Revenue totale piattaforma
        BigDecimal totalRevenue = BigDecimal.ZERO;
        List<Tenant> tenants = tenantRepository.findAll();
        for (Tenant tenant : tenants) {
            BigDecimal tenantRevenue = prenotazioneRepository.getTotalRevenue(tenant.getId());
            if (tenantRevenue != null) {
                totalRevenue = totalRevenue.add(tenantRevenue);
            }
        }

        DashboardStats stats = new DashboardStats(
                totalTenants,
                activeTenants,
                trialTenants,
                suspendedTenants,
                totalUsers,
                totalOmbrelloni,
                totalPrenotazioni,
                totalRevenue
        );

        return ResponseEntity.ok(stats);
    }

    /**
     * GET /api/admin/tenants
     * Lista tutti i tenant con statistiche.
     */
    @GetMapping("/tenants")
    public ResponseEntity<List<TenantWithStats>> getAllTenantsWithStats() {
        List<Tenant> tenants = tenantRepository.findAll();

        List<TenantWithStats> response = tenants.stream()
                .map(this::buildTenantWithStats)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/admin/tenants/{id}
     * Dettagli tenant con statistiche complete.
     */
    @GetMapping("/tenants/{id}")
    public ResponseEntity<TenantDetailStats> getTenantDetail(@PathVariable UUID id) {
        Tenant tenant = tenantService.findById(id);

        long users = userRepository.countByTenantIdAndRuolo(id, null);
        long ombrelloni = ombrelloneRepository.countByTenantId(id);
        long prenotazioni = prenotazioneRepository.findByTenantId(id).size();
        BigDecimal revenue = prenotazioneRepository.getTotalRevenue(id);

        TenantDetailStats stats = new TenantDetailStats(
                mapToResponse(tenant),
                users,
                ombrelloni,
                prenotazioni,
                revenue != null ? revenue : BigDecimal.ZERO
        );

        return ResponseEntity.ok(stats);
    }

    /**
     * GET /api/admin/tenants/expired
     * Tenant con abbonamento scaduto.
     */
    @GetMapping("/tenants/expired")
    public ResponseEntity<List<TenantResponse>> getExpiredTenants() {
        List<Tenant> expired = tenantService.findExpiredTenants();

        List<TenantResponse> response = expired.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/admin/tenants/{id}/suspend
     * Sospende un tenant.
     */
    @PostMapping("/tenants/{id}/suspend")
    public ResponseEntity<?> suspendTenant(
            @PathVariable UUID id,
            @RequestParam(required = false) String motivo) {
        try {
            tenantService.suspendTenant(id);

            // TODO: Invia email notifica sospensione

            return ResponseEntity.ok(
                    MessageResponse.success("Tenant sospeso: " + motivo)
            );
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(MessageResponse.error(e.getMessage()));
        }
    }

    /**
     * POST /api/admin/tenants/{id}/activate
     * Attiva un tenant sospeso.
     */
    @PostMapping("/tenants/{id}/activate")
    public ResponseEntity<?> activateTenant(@PathVariable UUID id) {
        try {
            tenantService.activateTenant(id);

            // TODO: Invia email notifica attivazione

            return ResponseEntity.ok(
                    MessageResponse.success("Tenant attivato con successo")
            );
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(MessageResponse.error(e.getMessage()));
        }
    }

    /**
     * DELETE /api/admin/tenants/{id}
     * Elimina definitivamente un tenant (ATTENZIONE!).
     */
    @DeleteMapping("/tenants/{id}")
    public ResponseEntity<?> deleteTenant(@PathVariable UUID id) {
        try {
            // Verifica che il tenant sia già sospeso
            Tenant tenant = tenantService.findById(id);

            if (tenant.getStato() != StatoTenant.SUSPENDED) {
                return ResponseEntity
                        .badRequest()
                        .body(MessageResponse.error("Solo tenant sospesi possono essere eliminati"));
            }

            // Elimina tenant (cascade elimina anche users, ombrelloni, prenotazioni)
            tenantRepository.delete(tenant);

            return ResponseEntity.ok(
                    MessageResponse.success("Tenant eliminato definitivamente")
            );

        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(MessageResponse.error(e.getMessage()));
        }
    }

    /**
     * GET /api/admin/users
     * Lista tutti gli utenti della piattaforma.
     */
    @GetMapping("/users")
    public ResponseEntity<List<UserWithTenant>> getAllUsers() {
        List<User> users = userRepository.findAll();

        List<UserWithTenant> response = users.stream()
                .map(user -> {
                    String tenantName = null;
                    if (user.getTenantId() != null) {
                        tenantRepository.findById(user.getTenantId())
                                .ifPresent(t -> {});
                    }
                    return new UserWithTenant(
                            user.getId(),
                            user.getEmail(),
                            user.getNomeCompleto(),
                            user.getRuolo().name(),
                            user.getTenantId(),
                            tenantName,
                            user.getAttivo()
                    );
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/admin/stats/revenue-by-tenant
     * Revenue per tenant (top 10).
     */
    @GetMapping("/stats/revenue-by-tenant")
    public ResponseEntity<List<TenantRevenue>> getRevenueByTenant() {
        List<Tenant> tenants = tenantRepository.findAll();

        List<TenantRevenue> revenues = tenants.stream()
                .map(tenant -> {
                    BigDecimal revenue = prenotazioneRepository.getTotalRevenue(tenant.getId());
                    return new TenantRevenue(
                            tenant.getId(),
                            tenant.getNomeStabilimento(),
                            revenue != null ? revenue : BigDecimal.ZERO
                    );
                })
                .sorted((a, b) -> b.getRevenue().compareTo(a.getRevenue()))
                .limit(10)
                .collect(Collectors.toList());

        return ResponseEntity.ok(revenues);
    }

    // Helper methods
    private TenantWithStats buildTenantWithStats(Tenant tenant) {
        long users = userRepository.countByTenantIdAndRuolo(tenant.getId(), null);
        long ombrelloni = ombrelloneRepository.countByTenantId(tenant.getId());
        long prenotazioni = prenotazioneRepository.findByTenantId(tenant.getId()).size();

        return new TenantWithStats(
                tenant.getId(),
                tenant.getNomeStabilimento(),
                tenant.getSlug(),
                tenant.getPiano().name(),
                tenant.getStato().name(),
                users,
                ombrelloni,
                prenotazioni
        );
    }

    private TenantResponse mapToResponse(Tenant tenant) {
        return TenantResponse.builder()
                .id(tenant.getId())
                .nomeStabilimento(tenant.getNomeStabilimento())
                .slug(tenant.getSlug())
                .email(tenant.getEmail())
                .piano(tenant.getPiano().name())
                .stato(tenant.getStato().name())
                .dataCreazione(tenant.getDataCreazione())
                .dataScadenzaAbbonamento(tenant.getDataScadenzaAbbonamento())
                .build();
    }

    // DTO
    @lombok.Data
    @lombok.AllArgsConstructor
    private static class DashboardStats {
        private Long totalTenants;
        private Long activeTenants;
        private Long trialTenants;
        private Long suspendedTenants;
        private Long totalUsers;
        private Long totalOmbrelloni;
        private Long totalPrenotazioni;
        private BigDecimal totalRevenue;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    private static class TenantWithStats {
        private UUID id;
        private String nome;
        private String slug;
        private String piano;
        private String stato;
        private Long users;
        private Long ombrelloni;
        private Long prenotazioni;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    private static class TenantDetailStats {
        private TenantResponse tenant;
        private Long users;
        private Long ombrelloni;
        private Long prenotazioni;
        private BigDecimal revenue;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    private static class UserWithTenant {
        private UUID id;
        private String email;
        private String nomeCompleto;
        private String ruolo;
        private UUID tenantId;
        private String tenantName;
        private Boolean attivo;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    private static class TenantRevenue {
        private UUID tenantId;
        private String nomeStabilimento;
        private BigDecimal revenue;
    }
}