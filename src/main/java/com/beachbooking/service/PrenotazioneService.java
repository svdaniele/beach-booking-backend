package com.beachbooking.service;

import com.beachbooking.exception.ResourceNotFoundException;
import com.beachbooking.model.entity.Ombrellone;
import com.beachbooking.model.entity.Prenotazione;
import com.beachbooking.model.enums.StatoPrenotazione;
import com.beachbooking.model.enums.TipoPrenotazione;
import com.beachbooking.repository.OmbrelloneRepository;
import com.beachbooking.repository.PrenotazioneRepository;
import com.beachbooking.tenant.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * Service per la gestione delle prenotazioni.
 */
@Service
public class PrenotazioneService {

    @Autowired
    private PrenotazioneRepository prenotazioneRepository;

    @Autowired
    private OmbrelloneRepository ombrelloneRepository;

    // Prezzo base giornaliero (può essere configurabile per tenant)
    private static final BigDecimal PREZZO_BASE_GIORNALIERO = new BigDecimal("30.00");

    /**
     * Crea una nuova prenotazione.
     */
    @Transactional
    public Prenotazione create(UUID userId,
                               UUID ombrelloneId,
                               LocalDate dataInizio,
                               LocalDate dataFine,
                               TipoPrenotazione tipo,
                               String note) {

        UUID tenantId = TenantContext.getTenantId();

        // Verifica che l'ombrellone esista e appartenga al tenant
        Ombrellone ombrellone = ombrelloneRepository.findByIdAndTenantId(ombrelloneId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Ombrellone non trovato"));

        if (!ombrellone.getAttivo()) {
            throw new RuntimeException("Ombrellone non disponibile");
        }

        // Verifica disponibilità
        if (!prenotazioneRepository.isOmbrelloneDisponibile(ombrelloneId, dataInizio, dataFine)) {
            throw new RuntimeException("Ombrellone non disponibile nelle date selezionate");
        }

        // Calcola prezzo
        BigDecimal prezzoTotale = calcolaPrezzo(ombrellone, dataInizio, dataFine, tipo);

        Prenotazione prenotazione = Prenotazione.builder()
                .tenantId(tenantId)
                .userId(userId)
                .ombrelloneId(ombrelloneId)
                .dataInizio(dataInizio)
                .dataFine(dataFine)
                .tipoPrenotazione(tipo)
                .prezzoTotale(prezzoTotale)
                .stato(StatoPrenotazione.PENDING)
                .note(note)
                .build();

        prenotazione = prenotazioneRepository.save(prenotazione);

        // TODO: Invia email di conferma
        // emailService.sendBookingConfirmation(prenotazione);

        return prenotazione;
    }

    /**
     * Trova tutte le prenotazioni del tenant corrente.
     */
    public List<Prenotazione> findAll() {
        UUID tenantId = TenantContext.getTenantId();
        return prenotazioneRepository.findByTenantId(tenantId);
    }

    /**
     * Trova prenotazioni per utente.
     */
    public List<Prenotazione> findByUserId(UUID userId) {
        UUID tenantId = TenantContext.getTenantId();
        return prenotazioneRepository.findByUserIdAndTenantId(userId, tenantId);
    }

    /**
     * Trova prenotazioni per ombrellone.
     */
    public List<Prenotazione> findByOmbrelloneId(UUID ombrelloneId) {
        return prenotazioneRepository.findByOmbrelloneId(ombrelloneId);
    }

    /**
     * Trova una prenotazione per ID.
     */
    public Prenotazione findById(UUID id) {
        UUID tenantId = TenantContext.getTenantId();
        return prenotazioneRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Prenotazione non trovata"));
    }

    /**
     * Trova prenotazione per codice.
     */
    public Prenotazione findByCodice(String codice) {
        return prenotazioneRepository.findByCodicePrenotazione(codice)
                .orElseThrow(() -> new ResourceNotFoundException("Prenotazione non trovata"));
    }

    /**
     * Trova prenotazioni per stato.
     */
    public List<Prenotazione> findByStato(StatoPrenotazione stato) {
        UUID tenantId = TenantContext.getTenantId();
        return prenotazioneRepository.findByTenantIdAndStato(tenantId, stato);
    }

    /**
     * Trova prenotazioni in un range di date.
     */
    public List<Prenotazione> findByDateRange(LocalDate dataInizio, LocalDate dataFine) {
        UUID tenantId = TenantContext.getTenantId();
        return prenotazioneRepository.findByTenantIdAndDateRange(tenantId, dataInizio, dataFine);
    }

    /**
     * Trova prenotazioni attive (in corso oggi).
     */
    public List<Prenotazione> findPrenotazioniAttive() {
        UUID tenantId = TenantContext.getTenantId();
        return prenotazioneRepository.findPrenotazioniAttive(tenantId);
    }

    /**
     * Trova ombrelloni disponibili in un periodo.
     */
    public List<Ombrellone> findOmbrelloniDisponibili(LocalDate dataInizio, LocalDate dataFine) {
        UUID tenantId = TenantContext.getTenantId();
        return prenotazioneRepository.findOmbrelloniDisponibili(tenantId, dataInizio, dataFine);
    }

    /**
     * Conferma una prenotazione.
     */
    @Transactional
    public Prenotazione confirm(UUID id) {
        Prenotazione prenotazione = findById(id);

        if (prenotazione.getStato() != StatoPrenotazione.PENDING) {
            throw new RuntimeException("Solo prenotazioni in attesa possono essere confermate");
        }

        prenotazione.setStato(StatoPrenotazione.CONFIRMED);
        return prenotazioneRepository.save(prenotazione);
    }

    /**
     * Marca una prenotazione come pagata.
     */
    @Transactional
    public Prenotazione markAsPaid(UUID id) {
        Prenotazione prenotazione = findById(id);
        prenotazione.setStato(StatoPrenotazione.PAID);
        return prenotazioneRepository.save(prenotazione);
    }

    /**
     * Cancella una prenotazione.
     */
    @Transactional
    public Prenotazione cancel(UUID id, String motivo) {
        Prenotazione prenotazione = findById(id);

        if (prenotazione.getStato() == StatoPrenotazione.COMPLETED) {
            throw new RuntimeException("Impossibile cancellare una prenotazione completata");
        }

        prenotazione.setStato(StatoPrenotazione.CANCELLED);
        prenotazione.setNote(
                (prenotazione.getNote() != null ? prenotazione.getNote() + "\n" : "") +
                        "Cancellata: " + motivo
        );

        return prenotazioneRepository.save(prenotazione);
    }

    /**
     * Completa una prenotazione (a fine periodo).
     */
    @Transactional
    public Prenotazione complete(UUID id) {
        Prenotazione prenotazione = findById(id);

        if (prenotazione.getStato() != StatoPrenotazione.PAID) {
            throw new RuntimeException("Solo prenotazioni pagate possono essere completate");
        }

        prenotazione.setStato(StatoPrenotazione.COMPLETED);
        return prenotazioneRepository.save(prenotazione);
    }

    /**
     * Calcola il prezzo di una prenotazione.
     */
    private BigDecimal calcolaPrezzo(Ombrellone ombrellone,
                                     LocalDate dataInizio,
                                     LocalDate dataFine,
                                     TipoPrenotazione tipo) {

        long numeroGiorni = ChronoUnit.DAYS.between(dataInizio, dataFine) + 1;

        // Prezzo base
        BigDecimal prezzo = PREZZO_BASE_GIORNALIERO.multiply(BigDecimal.valueOf(numeroGiorni));

        // Applica moltiplicatore tipo ombrellone
        prezzo = prezzo.multiply(BigDecimal.valueOf(ombrellone.getTipo().getMoltiplicatorePrezzo()));

        // Applica sconti per prenotazioni lunghe
        switch (tipo) {
            case SETTIMANALE:
                prezzo = prezzo.multiply(new BigDecimal("0.90")); // 10% sconto
                break;
            case MENSILE:
                prezzo = prezzo.multiply(new BigDecimal("0.80")); // 20% sconto
                break;
            case ANNUALE:
                prezzo = prezzo.multiply(new BigDecimal("0.60")); // 40% sconto
                break;
            default:
                break;
        }

        return prezzo.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Statistiche: numero prenotazioni per stato.
     */
    public long countByStato(StatoPrenotazione stato) {
        UUID tenantId = TenantContext.getTenantId();
        return prenotazioneRepository.countByTenantIdAndStato(tenantId, stato);
    }

    /**
     * Statistiche: revenue totale.
     */
    public BigDecimal getTotalRevenue() {
        UUID tenantId = TenantContext.getTenantId();
        return prenotazioneRepository.getTotalRevenue(tenantId);
    }
}