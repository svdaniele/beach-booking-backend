package com.beachbooking.controller;

import com.beachbooking.model.dto.request.CreatePagamentoRequest;
import com.beachbooking.model.dto.response.MessageResponse;
import com.beachbooking.model.dto.response.PagamentoResponse;
import com.beachbooking.model.entity.Pagamento;
import com.beachbooking.model.enums.MetodoPagamento;
import com.beachbooking.model.enums.StatoPrenotazione;
import com.beachbooking.service.PagamentoService;
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
 * Controller per la gestione dei Pagamenti.
 */
@RestController
@RequestMapping("/api/pagamenti")
@Validated
@CrossOrigin(origins = "*", maxAge = 3600)
public class PagamentoController {

    @Autowired
    private PagamentoService pagamentoService;

    /**
     * POST /api/pagamenti
     * Crea un nuovo pagamento per una prenotazione.
     * Solo staff/admin.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'STAFF', 'SUPER_ADMIN')")
    public ResponseEntity<?> create(@Valid @RequestBody CreatePagamentoRequest request) {
        try {
            Pagamento pagamento = pagamentoService.create(
                    request.getPrenotazioneId(),
                    request.getMetodoPagamento(),
                    request.getImporto(),
                    request.getRiferimentoEsterno(),
                    request.getNote()
            );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(mapToResponse(pagamento));

        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(MessageResponse.error(e.getMessage()));
        }
    }

    /**
     * GET /api/pagamenti
     * Lista tutti i pagamenti del tenant.
     * Solo staff/admin.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'STAFF', 'SUPER_ADMIN')")
    public ResponseEntity<List<PagamentoResponse>> findAll(
            @RequestParam(required = false) StatoPrenotazione stato,
            @RequestParam(required = false) MetodoPagamento metodo) {

        List<Pagamento> pagamenti;

        if (stato != null) {
            pagamenti = pagamentoService.findByStato(stato);
        } else if (metodo != null) {
            pagamenti = pagamentoService.findByMetodo(metodo);
        } else {
            pagamenti = pagamentoService.findAll();
        }

        List<PagamentoResponse> response = pagamenti.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/pagamenti/{id}
     * Dettagli pagamento.
     * Solo staff/admin.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'STAFF', 'SUPER_ADMIN')")
    public ResponseEntity<?> findById(@PathVariable UUID id) {
        try {
            Pagamento pagamento = pagamentoService.findById(id);
            return ResponseEntity.ok(mapToResponse(pagamento));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(MessageResponse.error(e.getMessage()));
        }
    }

    /**
     * GET /api/pagamenti/prenotazione/{prenotazioneId}
     * Pagamento per prenotazione.
     */
    @GetMapping("/prenotazione/{prenotazioneId}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'STAFF', 'SUPER_ADMIN')")
    public ResponseEntity<?> findByPrenotazione(@PathVariable UUID prenotazioneId) {
        try {
            Pagamento pagamento = pagamentoService.findByPrenotazioneId(prenotazioneId);
            return ResponseEntity.ok(mapToResponse(pagamento));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(MessageResponse.error(e.getMessage()));
        }
    }

    /**
     * PUT /api/pagamenti/{id}/confirm
     * Conferma un pagamento generico.
     * Solo staff/admin.
     */
    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'STAFF', 'SUPER_ADMIN')")
    public ResponseEntity<?> confirm(@PathVariable UUID id) {
        try {
            Pagamento pagamento = pagamentoService.confirmPayment(id);
            return ResponseEntity.ok(mapToResponse(pagamento));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(MessageResponse.error(e.getMessage()));
        }
    }

    /**
     * PUT /api/pagamenti/{id}/confirm-paypal
     * Conferma pagamento PayPal con transaction ID.
     */
    @PutMapping("/{id}/confirm-paypal")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'STAFF', 'SUPER_ADMIN')")
    public ResponseEntity<?> confirmPayPal(
            @PathVariable UUID id,
            @RequestParam String transactionId) {
        try {
            Pagamento pagamento = pagamentoService.confirmPayPalPayment(id, transactionId);
            return ResponseEntity.ok(mapToResponse(pagamento));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(MessageResponse.error(e.getMessage()));
        }
    }

    /**
     * PUT /api/pagamenti/{id}/confirm-bonifico
     * Conferma pagamento Bonifico con riferimento.
     */
    @PutMapping("/{id}/confirm-bonifico")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'STAFF', 'SUPER_ADMIN')")
    public ResponseEntity<?> confirmBonifico(
            @PathVariable UUID id,
            @RequestParam String riferimento) {
        try {
            Pagamento pagamento = pagamentoService.confirmBonificoPayment(id, riferimento);
            return ResponseEntity.ok(mapToResponse(pagamento));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(MessageResponse.error(e.getMessage()));
        }
    }

    /**
     * DELETE /api/pagamenti/{id}
     * Cancella un pagamento (solo pending).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> cancel(
            @PathVariable UUID id,
            @RequestParam(required = false) String motivo) {
        try {
            pagamentoService.cancel(id, motivo != null ? motivo : "Cancellato");
            return ResponseEntity.ok(
                    MessageResponse.success("Pagamento cancellato con successo")
            );
        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(MessageResponse.error(e.getMessage()));
        }
    }

    /**
     * POST /api/pagamenti/{id}/refund
     * Rimborsa un pagamento confermato.
     */
    @PostMapping("/{id}/refund")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> refund(
            @PathVariable UUID id,
            @RequestParam String motivo) {
        try {
            Pagamento pagamento = pagamentoService.refund(id, motivo);
            return ResponseEntity.ok(mapToResponse(pagamento));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(MessageResponse.error(e.getMessage()));
        }
    }

    /**
     * GET /api/pagamenti/stats/total
     * Totale pagamenti del tenant.
     */
    @GetMapping("/stats/total")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'STAFF', 'SUPER_ADMIN')")
    public ResponseEntity<TotalResponse> getTotal() {
        java.math.BigDecimal total = pagamentoService.getTotalPagamenti();
        return ResponseEntity.ok(new TotalResponse(total));
    }

    // Helper
    private PagamentoResponse mapToResponse(Pagamento p) {
        return PagamentoResponse.builder()
                .id(p.getId())
                .prenotazioneId(p.getPrenotazioneId())
                .metodoPagamento(p.getMetodoPagamento().name())
                .importo(p.getImporto())
                .stato(p.getStato().name())
                .riferimentoEsterno(p.getRiferimentoEsterno())
                .dataPagamento(p.getDataPagamento())
                .note(p.getNote())
                .dataCreazione(p.getDataCreazione())
                .build();
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    private static class TotalResponse {
        private java.math.BigDecimal totalePagamenti;
    }
}