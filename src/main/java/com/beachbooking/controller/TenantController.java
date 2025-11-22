package com.beachbooking.controller;

import com.beachbooking.model.dto.request.RegisterTenantRequest;
import com.beachbooking.model.dto.response.MessageResponse;
import com.beachbooking.model.dto.response.TenantResponse;
import com.beachbooking.model.entity.Tenant;
import com.beachbooking.model.enums.PianoAbbonamento;
import com.beachbooking.repository.OmbrelloneRepository;
import com.beachbooking.service.TenantService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller per la gestione dei Tenant (Stabilimenti Balneari).
 *
 * Endpoints pubblici:
 * - POST /register : Registrazione nuovo stabilimento
 * - GET /check-slug/{slug} : Verifica disponibilità slug
 *
 * Endpoints protetti:
 * - GET /current : Info tenant corrente (TENANT_ADMIN, STAFF)
 * - PUT /{id} : Aggiorna tenant (TENANT_ADMIN)
 * - POST /{id}/upgrade : Upgrade piano (TENANT_ADMIN)
 *
 * Endpoints admin:
 * - GET / : Lista tutti i tenant (SUPER_ADMIN)
 * - GET /{id} : Dettagli tenant (SUPER_ADMIN)
 * - POST /{id}/suspend : Sospendi tenant (SUPER_ADMIN)
 * - POST /{id}/activate : Attiva tenant (SUPER_ADMIN)
 */
@RestController
@RequestMapping("/api/tenants")
@Validated
@CrossOrigin(origins = "*", maxAge = 3600)
public class TenantController {

    @Autowired
    private TenantService tenantService;

    @Autowired
    private OmbrelloneRepository ombrelloneRepository;

    /**
     * POST /api/tenants/register
     * Registra un nuovo tenant (stabilimento).
     * Endpoint PUBBLICO.
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerTenant(@Valid @RequestBody RegisterTenantRequest request) {
        try {
            Tenant tenant = tenantService.registerTenant(
                    request.getNomeStabilimento(),
                    request.getEmail(),
                    request.getPassword(),
                    request.getNomeAdmin(),
                    request.getCognomeAdmin(),
                    request.getTelefono(),
                    request.getIndirizzo(),
                    request.getCitta(),
                    request.getProvincia(),
                    request.getCap()
            );

            TenantResponse response = mapToResponse(tenant);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response);

        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(MessageResponse.error(e.getMessage()));
        }
    }

    /**
     * GET /api/tenants/check-slug/{slug}
     * Verifica se uno slug è disponibile.
     * Endpoint PUBBLICO.
     */
    @GetMapping("/check-slug/{slug}")
    public ResponseEntity<?> checkSlugAvailability(@PathVariable String slug) {
        boolean available = tenantService.isSlugAvailable(slug);

        return ResponseEntity.ok(new SlugAvailabilityResponse(slug, available));
    }

    /**
     * GET /api/tenants/current
     * Ottiene informazioni del tenant corrente.
     * Richiede autenticazione e tenant nel context.
     */
    @GetMapping("/current")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'STAFF', 'SUPER_ADMIN')")
    public ResponseEntity<TenantResponse> getCurrentTenant() {
        UUID tenantId = com.beachbooking.tenant.TenantContext.getTenantId();
        Tenant tenant = tenantService.findById(tenantId);

        TenantResponse response = mapToResponse(tenant);

        // Aggiungi statistiche
        long numeroOmbrelloni = ombrelloneRepository.countByTenantIdAndAttivoTrue(tenantId);
        response.setNumeroOmbrelloni(numeroOmbrelloni);
        response.setMaxOmbrelloni((long) tenant.getPiano().getMaxOmbrelloni());

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/tenants/{id}
     * Ottiene un tenant per ID.
     * Solo SUPER_ADMIN.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<TenantResponse> getById(@PathVariable UUID id) {
        Tenant tenant = tenantService.findById(id);
        return ResponseEntity.ok(mapToResponse(tenant));
    }

    /**
     * GET /api/tenants/slug/{slug}
     * Ottiene un tenant per slug.
     * Solo SUPER_ADMIN.
     */
    @GetMapping("/slug/{slug}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<TenantResponse> getBySlug(@PathVariable String slug) {
        Tenant tenant = tenantService.findBySlug(slug);
        return ResponseEntity.ok(mapToResponse(tenant));
    }

    /**
     * GET /api/tenants
     * Lista tutti i tenant.
     * Solo SUPER_ADMIN.
     */
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<TenantResponse>> findAll() {
        List<Tenant> tenants = tenantService.findAll();
        List<TenantResponse> response = tenants.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/tenants/{id}
     * Aggiorna un tenant.
     * TENANT_ADMIN può aggiornare solo il proprio tenant.
     * SUPER_ADMIN può aggiornare qualsiasi tenant.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> updateTenant(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTenantRequest request) {

        try {
            // Verifica che TENANT_ADMIN possa modificare solo il proprio tenant
            if (!isAllowedToModify(id)) {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(MessageResponse.error("Non autorizzato a modificare questo tenant"));
            }

            Tenant updatedData = new Tenant();
            updatedData.setNomeStabilimento(request.getNomeStabilimento());
            updatedData.setDescrizione(request.getDescrizione());
            updatedData.setIndirizzo(request.getIndirizzo());
            updatedData.setTelefono(request.getTelefono());
            updatedData.setLogoUrl(request.getLogoUrl());
            updatedData.setConfigurazione(request.getConfigurazione());

            Tenant updated = tenantService.updateTenant(id, updatedData);

            return ResponseEntity.ok(mapToResponse(updated));

        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(MessageResponse.error(e.getMessage()));
        }
    }

    /**
     * POST /api/tenants/{id}/upgrade
     * Upgrade del piano di abbonamento.
     * Solo TENANT_ADMIN del tenant specifico.
     */
    @PostMapping("/{id}/upgrade")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> upgradePiano(
            @PathVariable UUID id,
            @RequestParam("piano") PianoAbbonamento nuovoPiano) {

        try {
            if (!isAllowedToModify(id)) {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(MessageResponse.error("Non autorizzato"));
            }

            Tenant tenant = tenantService.upgradePiano(id, nuovoPiano);

            return ResponseEntity.ok(mapToResponse(tenant));

        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(MessageResponse.error(e.getMessage()));
        }
    }

    /**
     * POST /api/tenants/{id}/suspend
     * Sospende un tenant.
     * Solo SUPER_ADMIN.
     */
    @PostMapping("/{id}/suspend")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> suspendTenant(@PathVariable UUID id) {
        tenantService.suspendTenant(id);
        return ResponseEntity.ok(
                MessageResponse.success("Tenant sospeso con successo")
        );
    }

    /**
     * POST /api/tenants/{id}/activate
     * Attiva un tenant sospeso.
     * Solo SUPER_ADMIN.
     */
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> activateTenant(@PathVariable UUID id) {
        tenantService.activateTenant(id);
        return ResponseEntity.ok(
                MessageResponse.success("Tenant attivato con successo")
        );
    }

    /**
     * GET /api/tenants/expired
     * Lista tenant scaduti.
     * Solo SUPER_ADMIN (per job automatici).
     */
    @GetMapping("/expired")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<TenantResponse>> findExpiredTenants() {
        List<Tenant> tenants = tenantService.findExpiredTenants();
        List<TenantResponse> response = tenants.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    // ============================================
    // METODI HELPER PRIVATI
    // ============================================

    /**
     * Verifica se l'utente corrente può modificare il tenant.
     */
    private boolean isAllowedToModify(UUID tenantId) {
        try {
            // SUPER_ADMIN può modificare qualsiasi tenant
            org.springframework.security.core.Authentication auth =
                    org.springframework.security.core.context.SecurityContextHolder
                            .getContext().getAuthentication();

            boolean isSuperAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));

            if (isSuperAdmin) {
                return true;
            }

            // TENANT_ADMIN può modificare solo il proprio tenant
            UUID currentTenantId = com.beachbooking.tenant.TenantContext.getTenantId();
            return tenantId.equals(currentTenantId);

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Mappa Tenant entity a TenantResponse DTO.
     */
    private TenantResponse mapToResponse(Tenant tenant) {
        return TenantResponse.builder()
                .id(tenant.getId())
                .nomeStabilimento(tenant.getNomeStabilimento())
                .slug(tenant.getSlug())
                .descrizione(tenant.getDescrizione())
                .indirizzo(tenant.getIndirizzo())
                .citta(tenant.getCitta())
                .provincia(tenant.getProvincia())
                .cap(tenant.getCap())
                .telefono(tenant.getTelefono())
                .email(tenant.getEmail())
                .logoUrl(tenant.getLogoUrl())
                .piano(tenant.getPiano().name())
                .stato(tenant.getStato().name())
                .dataCreazione(tenant.getDataCreazione())
                .dataScadenzaAbbonamento(tenant.getDataScadenzaAbbonamento())
                .build();
    }

    // ============================================
    // DTO INTERNI
    // ============================================

    /**
     * DTO per risposta disponibilità slug.
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    private static class SlugAvailabilityResponse {
        private String slug;
        private Boolean available;
    }

    /**
     * DTO per richiesta aggiornamento tenant.
     */
    @lombok.Data
    private static class UpdateTenantRequest {
        private String nomeStabilimento;
        private String descrizione;
        private String indirizzo;
        private String telefono;
        private String logoUrl;
        private String configurazione;
    }
}