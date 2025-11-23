package com.beachbooking.service;

import com.beachbooking.exception.ResourceNotFoundException;
import com.beachbooking.model.entity.Pagamento;
import com.beachbooking.model.entity.Prenotazione;
import com.beachbooking.model.enums.MetodoPagamento;
import com.beachbooking.model.enums.StatoPrenotazione;
import com.beachbooking.repository.PagamentoRepository;
import com.beachbooking.repository.PrenotazioneRepository;
import com.beachbooking.tenant.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service per la gestione dei pagamenti.
 */
@Service
public class PagamentoService {

    @Autowired
    private PagamentoRepository pagamentoRepository;

    @Autowired
    private PrenotazioneRepository prenotazioneRepository;

    @Autowired
    private PrenotazioneService prenotazioneService;

    /**
     * Crea un nuovo pagamento per una prenotazione.
     */
    @Transactional
    public Pagamento create(UUID prenotazioneId,
                            MetodoPagamento metodoPagamento,
                            BigDecimal importo,
                            String riferimentoEsterno,
                            String note) {

        UUID tenantId = TenantContext.getTenantId();

        // Verifica che la prenotazione esista e appartenga al tenant
        Prenotazione prenotazione = prenotazioneRepository.findByIdAndTenantId(prenotazioneId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Prenotazione non trovata"));

        // Verifica che non esista già un pagamento per questa prenotazione
        if (pagamentoRepository.findByPrenotazioneId(prenotazioneId).isPresent()) {
            throw new RuntimeException("Pagamento già esistente per questa prenotazione");
        }

        // Verifica che l'importo corrisponda al prezzo della prenotazione
        if (importo.compareTo(prenotazione.getPrezzoTotale()) != 0) {
            throw new RuntimeException("L'importo non corrisponde al prezzo della prenotazione");
        }

        Pagamento pagamento = Pagamento.builder()
                .prenotazioneId(prenotazioneId)
                .metodoPagamento(metodoPagamento)
                .importo(importo)
                .stato(StatoPrenotazione.PENDING)
                .riferimentoEsterno(riferimentoEsterno)
                .note(note)
                .build();

        return pagamentoRepository.save(pagamento);
    }

    /**
     * Conferma un pagamento (da chiamare dopo verifica PayPal/Bonifico).
     */
    @Transactional
    public Pagamento confirmPayment(UUID pagamentoId) {
        Pagamento pagamento = findById(pagamentoId);

        if (pagamento.getStato() == StatoPrenotazione.PAID) {
            throw new RuntimeException("Pagamento già confermato");
        }

        pagamento.setStato(StatoPrenotazione.PAID);
        pagamento.setDataPagamento(LocalDateTime.now());

        pagamentoRepository.save(pagamento);

        // Aggiorna anche la prenotazione
        prenotazioneService.markAsPaid(pagamento.getPrenotazioneId());

        return pagamento;
    }

    /**
     * Conferma pagamento PayPal con transaction ID.
     */
    @Transactional
    public Pagamento confirmPayPalPayment(UUID pagamentoId, String paypalTransactionId) {
        Pagamento pagamento = findById(pagamentoId);

        if (pagamento.getMetodoPagamento() != MetodoPagamento.PAYPAL) {
            throw new RuntimeException("Questo pagamento non è PayPal");
        }

        pagamento.setRiferimentoEsterno(paypalTransactionId);
        pagamento.setStato(StatoPrenotazione.PAID);
        pagamento.setDataPagamento(LocalDateTime.now());

        pagamentoRepository.save(pagamento);
        prenotazioneService.markAsPaid(pagamento.getPrenotazioneId());

        return pagamento;
    }

    /**
     * Conferma pagamento Bonifico (manuale da staff).
     */
    @Transactional
    public Pagamento confirmBonificoPayment(UUID pagamentoId, String riferimentoBonifico) {
        Pagamento pagamento = findById(pagamentoId);

        if (pagamento.getMetodoPagamento() != MetodoPagamento.BONIFICO) {
            throw new RuntimeException("Questo pagamento non è Bonifico");
        }

        pagamento.setRiferimentoEsterno(riferimentoBonifico);
        pagamento.setStato(StatoPrenotazione.PAID);
        pagamento.setDataPagamento(LocalDateTime.now());

        pagamentoRepository.save(pagamento);
        prenotazioneService.markAsPaid(pagamento.getPrenotazioneId());

        return pagamento;
    }

    /**
     * Trova un pagamento per ID.
     */
    public Pagamento findById(UUID id) {
        return pagamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento non trovato"));
    }

    /**
     * Trova pagamento per prenotazione.
     */
    public Pagamento findByPrenotazioneId(UUID prenotazioneId) {
        return pagamentoRepository.findByPrenotazioneId(prenotazioneId)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento non trovato"));
    }

    /**
     * Trova pagamento per riferimento esterno (PayPal ID, etc).
     */
    public Pagamento findByRiferimentoEsterno(String riferimento) {
        return pagamentoRepository.findByRiferimentoEsterno(riferimento)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento non trovato"));
    }

    /**
     * Lista tutti i pagamenti del tenant.
     */
    public List<Pagamento> findAll() {
        UUID tenantId = TenantContext.getTenantId();
        return pagamentoRepository.findByTenantId(tenantId);
    }

    /**
     * Lista pagamenti per stato.
     */
    public List<Pagamento> findByStato(StatoPrenotazione stato) {
        return pagamentoRepository.findByStato(stato);
    }

    /**
     * Lista pagamenti per metodo.
     */
    public List<Pagamento> findByMetodo(MetodoPagamento metodo) {
        return pagamentoRepository.findByMetodoPagamento(metodo);
    }

    /**
     * Totale pagamenti del tenant.
     */
    public BigDecimal getTotalPagamenti() {
        UUID tenantId = TenantContext.getTenantId();
        return pagamentoRepository.getTotalPagamentiByTenant(tenantId);
    }

    /**
     * Cancella un pagamento (solo se pending).
     */
    @Transactional
    public void cancel(UUID pagamentoId, String motivo) {
        Pagamento pagamento = findById(pagamentoId);

        if (pagamento.getStato() == StatoPrenotazione.PAID) {
            throw new RuntimeException("Non è possibile cancellare un pagamento già confermato");
        }

        pagamento.setStato(StatoPrenotazione.CANCELLED);
        pagamento.setNote(
                (pagamento.getNote() != null ? pagamento.getNote() + "\n" : "") +
                        "Cancellato: " + motivo
        );

        pagamentoRepository.save(pagamento);
    }

    /**
     * Rimborsa un pagamento.
     */
    @Transactional
    public Pagamento refund(UUID pagamentoId, String motivo) {
        Pagamento pagamento = findById(pagamentoId);

        if (pagamento.getStato() != StatoPrenotazione.PAID) {
            throw new RuntimeException("Solo pagamenti confermati possono essere rimborsati");
        }

        pagamento.setStato(StatoPrenotazione.REFUNDED);
        pagamento.setNote(
                (pagamento.getNote() != null ? pagamento.getNote() + "\n" : "") +
                        "Rimborsato: " + motivo
        );

        pagamentoRepository.save(pagamento);

        // Aggiorna anche la prenotazione
        prenotazioneService.cancel(pagamento.getPrenotazioneId(), "Rimborsato: " + motivo);

        return pagamento;
    }
}