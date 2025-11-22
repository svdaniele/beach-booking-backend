package com.beachbooking.controller;

import com.beachbooking.model.dto.request.CreatePrenotazioneRequest;
import com.beachbooking.model.dto.response.MessageResponse;
import com.beachbooking.model.dto.response.OmbrelloneResponse;
import com.beachbooking.model.dto.response.PrenotazioneResponse;
import com.beachbooking.model.entity.Ombrellone;
import com.beachbooking.model.entity.Prenotazione;
import com.beachbooking.model.entity.User;
import com.beachbooking.model.enums.StatoPrenotazione;
import com.beachbooking.service.AuthService;
import com.beachbooking.service.PrenotazioneService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller per la gestione delle Prenotazioni.
 *
 * I clienti possono:
 * - Creare prenotazioni
 * - Visualizzare le proprie prenotazioni
 * - Cancellare le proprie prenotazioni
 *
 * Staff e Admin possono:
 * - Visualizzare tutte le prenotazioni
 * - Confermare/Cancellare qualsiasi prenotazione
 * - Marcare come pagate
 */
@RestController
@RequestMapping("/api/prenotazioni")
@Validated
@CrossOrigin(origins = "*", maxAge = 3600)
public class PrenotazioneController {

    @Autowired
    private PrenotazioneService prenotazioneService;

    @Autowired
    private AuthService authService;

    /**
     * POST /api/prenotazioni
     * Crea una nuova prenotazione.
     * I clienti possono prenotare per sé stessi.
     * Staff/Admin possono prenotare per qualsiasi utente.
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> create(@Valid @RequestBody CreatePrenotazioneRequest request) {
        try {
            User currentUser = authService.getCurrentUser();

            // Se userId non è specificato, usa l'utente corrente
            UUID userId = request.getUserId() != null ?
                    request.getUserId() : currentUser.getId();

            // Solo staff/admin possono prenotare per altri utenti
            if (!userId.equals(currentUser.getId()) &&
                    !hasStaffRole(currentUser)) {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(MessageResponse.error(
                                "Non puoi creare prenotazioni per altri utenti"
                        ));
            }

            Prenotazione prenotazione = prenotazioneService.create(
                    userId,
                    request.getOmbrelloneId(),
                    request.getDataInizio(),
                    request.getDataFine(),
                    request.getTipoPrenotazione(),
                    request.getNote()
            );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(mapToResponse(prenotazione));

        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(MessageResponse.error(e.getMessage()));
        }
    }

    /**
     * GET /api/prenotazioni
     * Lista prenotazioni.
     * - Clienti vedono solo le proprie
     * - Staff/Admin vedono tutte quelle del tenant
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PrenotazioneResponse>> findAll(
            @RequestParam(required = false) StatoPrenotazione stato,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInizio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFine) {

        User currentUser = authService.getCurrentUser();
        List<Prenotazione> prenotazioni;

        // Filtra in base al ruolo
        if (hasStaffRole(currentUser)) {
            // Staff/Admin vedono tutte
            if (dataInizio != null && dataFine != null) {
                prenotazioni = prenotazioneService.findByDateRange(dataInizio, dataFine);
            } else if (stato != null) {
                prenotazioni = prenotazioneService.findByStato(stato);
            } else {
                prenotazioni = prenotazioneService.findAll();
            }
        } else {
            // Cliente vede solo le sue
            prenotazioni = prenotazioneService.findByUserId(currentUser.getId());

            // Applica filtri se presenti
            if (stato != null) {
                prenotazioni = prenotazioni.stream()
                        .filter(p -> p.getStato() == stato)
                        .collect(Collectors.toList());
            }
        }

        List<PrenotazioneResponse> response = prenotazioni.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/prenotazioni/me
     * Le mie prenotazioni (shortcut per utente corrente).
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PrenotazioneResponse>> getMyPrenotazioni() {
        User currentUser = authService.getCurrentUser();
        List<Prenotazione> prenotazioni = prenotazioneService.findByUserId(currentUser.getId());

        List<PrenotazioneResponse> response = prenotazioni.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/prenotazioni/active
     * Prenotazioni attive (in corso oggi).
     * Solo staff/admin.
     */
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'STAFF', 'SUPER_ADMIN')")
    public ResponseEntity<List<PrenotazioneResponse>> getActivePrenotazioni() {
        List<Prenotazione> prenotazioni = prenotazioneService.findPrenotazioniAttive();

        List<PrenotazioneResponse> response = prenotazioni.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/prenotazioni/{id}
     * Dettagli prenotazione.
     * Cliente può vedere solo le proprie.
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> findById(@PathVariable UUID id) {
        try {
            Prenotazione prenotazione = prenotazioneService.findById(id);
            User currentUser = authService.getCurrentUser();

            // Verifica accesso
            if (!canAccessPrenotazione(prenotazione, currentUser)) {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(MessageResponse.error("Non autorizzato"));
            }

            return ResponseEntity.ok(mapToResponse(prenotazione));

        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(MessageResponse.error(e.getMessage()));
        }
    }

    /**
     * GET /api/prenotazioni/codice/{codice}
     * Trova prenotazione per codice.
     */
    @GetMapping("/codice/{codice}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> findByCodice(@PathVariable String codice) {
        try {
            Prenotazione prenotazione = prenotazioneService.findByCodice(codice);
            User currentUser = authService.getCurrentUser();

            if (!canAccessPrenotazione(prenotazione, currentUser)) {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(MessageResponse.error("Non autorizzato"));
            }

            return ResponseEntity.ok(mapToResponse(prenotazione));

        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(MessageResponse.error(e.getMessage()));
        }
    }

    /**
     * GET /api/prenotazioni/disponibili
     * Trova ombrelloni disponibili in un periodo.
     */
    @GetMapping("/disponibili")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> findOmbrelloniDisponibili(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInizio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFine) {

        if (dataInizio.isAfter(dataFine)) {
            return ResponseEntity
                    .badRequest()
                    .body(MessageResponse.error("Data inizio deve essere prima di data fine"));
        }

        List<Ombrellone> disponibili = prenotazioneService
                .findOmbrelloniDisponibili(dataInizio, dataFine);

        List<OmbrelloneResponse> response = disponibili.stream()
                .map(this::mapOmbrelloneToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/prenotazioni/{id}/confirm
     * Conferma una prenotazione.
     * Solo staff/admin.
     */
    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'STAFF', 'SUPER_ADMIN')")
    public ResponseEntity<?> confirm(@PathVariable UUID id) {
        try {
            Prenotazione prenotazione = prenotazioneService.confirm(id);
            return ResponseEntity.ok(mapToResponse(prenotazione));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(MessageResponse.error(e.getMessage()));
        }
    }

    /**
     * PUT /api/prenotazioni/{id}/pay
     * Marca prenotazione come pagata.
     * Solo staff/admin.
     */
    @PutMapping("/{id}/pay")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'STAFF', 'SUPER_ADMIN')")
    public ResponseEntity<?> markAsPaid(@PathVariable UUID id) {
        try {
            Prenotazione prenotazione = prenotazioneService.markAsPaid(id);
            return ResponseEntity.ok(mapToResponse(prenotazione));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(MessageResponse.error(e.getMessage()));
        }
    }

    /**
     * PUT /api/prenotazioni/{id}/complete
     * Completa una prenotazione (a fine periodo).
     * Solo staff/admin.
     */
    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'STAFF', 'SUPER_ADMIN')")
    public ResponseEntity<?> complete(@PathVariable UUID id) {
        try {
            Prenotazione prenotazione = prenotazioneService.complete(id);
            return ResponseEntity.ok(mapToResponse(prenotazione));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(MessageResponse.error(e.getMessage()));
        }
    }

    /**
     * DELETE /api/prenotazioni/{id}
     * Cancella una prenotazione.
     * Cliente può cancellare le proprie, staff può cancellare tutte.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> cancel(
            @PathVariable UUID id,
            @RequestParam(required = false) String motivo) {
        try {
            Prenotazione prenotazione = prenotazioneService.findById(id);
            User currentUser = authService.getCurrentUser();

            // Verifica permessi
            if (!canModifyPrenotazione(prenotazione, currentUser)) {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(MessageResponse.error("Non autorizzato"));
            }

            Prenotazione cancelled = prenotazioneService.cancel(
                    id,
                    motivo != null ? motivo : "Cancellata dall'utente"
            );

            return ResponseEntity.ok(mapToResponse(cancelled));

        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(MessageResponse.error(e.getMessage()));
        }
    }

    /**
     * GET /api/prenotazioni/stats
     * Statistiche prenotazioni.
     * Solo staff/admin.
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'STAFF', 'SUPER_ADMIN')")
    public ResponseEntity<StatsResponse> getStatistics() {
        long pending = prenotazioneService.countByStato(StatoPrenotazione.PENDING);
        long confirmed = prenotazioneService.countByStato(StatoPrenotazione.CONFIRMED);
        long paid = prenotazioneService.countByStato(StatoPrenotazione.PAID);
        long completed = prenotazioneService.countByStato(StatoPrenotazione.COMPLETED);
        long cancelled = prenotazioneService.countByStato(StatoPrenotazione.CANCELLED);

        java.math.BigDecimal totalRevenue = prenotazioneService.getTotalRevenue();

        StatsResponse stats = new StatsResponse(
                pending, confirmed, paid, completed, cancelled, totalRevenue
        );

        return ResponseEntity.ok(stats);
    }

    // ============================================
    // METODI HELPER PRIVATI
    // ============================================

    private boolean hasStaffRole(User user) {
        return user.getRuolo().name().equals("STAFF") ||
                user.getRuolo().name().equals("TENANT_ADMIN") ||
                user.getRuolo().name().equals("SUPER_ADMIN");
    }

    private boolean canAccessPrenotazione(Prenotazione prenotazione, User user) {
        return hasStaffRole(user) ||
                prenotazione.getUserId().equals(user.getId());
    }

    private boolean canModifyPrenotazione(Prenotazione prenotazione, User user) {
        return hasStaffRole(user) ||
                prenotazione.getUserId().equals(user.getId());
    }

    private PrenotazioneResponse mapToResponse(Prenotazione prenotazione) {
        return PrenotazioneResponse.builder()
                .id(prenotazione.getId())
                .userId(prenotazione.getUserId())
                .ombrelloneId(prenotazione.getOmbrelloneId())
                .dataInizio(prenotazione.getDataInizio())
                .dataFine(prenotazione.getDataFine())
                .numeroGiorni(prenotazione.getNumeroGiorni())
                .tipoPrenotazione(prenotazione.getTipoPrenotazione().name())
                .prezzoTotale(prenotazione.getPrezzoTotale())
                .stato(prenotazione.getStato().name())
                .statoDescrizione(prenotazione.getStato().getDescrizione())
                .note(prenotazione.getNote())
                .codicePrenotazione(prenotazione.getCodicePrenotazione())
                .dataCreazione(prenotazione.getDataCreazione())
                .build();
    }

    private OmbrelloneResponse mapOmbrelloneToResponse(Ombrellone o) {
        return OmbrelloneResponse.builder()
                .id(o.getId())
                .numero(o.getNumero())
                .fila(o.getFila())
                .tipo(o.getTipo().name())
                .tipoDescrizione(o.getTipo().getDescrizione())
                .descrizione(o.getDescrizione())
                .posizioneX(o.getPosizioneX())
                .posizioneY(o.getPosizioneY())
                .attivo(o.getAttivo())
                .disponibile(true) // Tutti quelli restituiti sono disponibili
                .build();
    }

    // ============================================
    // DTO INTERNI
    // ============================================

    @lombok.Data
    @lombok.AllArgsConstructor
    private static class StatsResponse {
        private Long pending;
        private Long confirmed;
        private Long paid;
        private Long completed;
        private Long cancelled;
        private java.math.BigDecimal totalRevenue;
    }
}