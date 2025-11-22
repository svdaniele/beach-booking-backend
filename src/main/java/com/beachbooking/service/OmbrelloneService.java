package com.beachbooking.service;

import com.beachbooking.exception.ResourceNotFoundException;
import com.beachbooking.model.entity.Ombrellone;
import com.beachbooking.model.entity.Tenant;
import com.beachbooking.model.enums.TipoOmbrellone;
import com.beachbooking.repository.OmbrelloneRepository;
import com.beachbooking.repository.TenantRepository;
import com.beachbooking.tenant.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service per la gestione degli ombrelloni.
 */
@Service
public class OmbrelloneService {

    @Autowired
    private OmbrelloneRepository ombrelloneRepository;

    @Autowired
    private TenantRepository tenantRepository;

    /**
     * Crea un nuovo ombrellone per il tenant corrente.
     */
    @Transactional
    public Ombrellone create(Integer numero,
                             String fila,
                             TipoOmbrellone tipo,
                             String descrizione,
                             Integer posizioneX,
                             Integer posizioneY) {

        UUID tenantId = TenantContext.getTenantId();

        // Verifica limiti piano abbonamento
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant non trovato"));

        long currentCount = ombrelloneRepository.countByTenantIdAndAttivoTrue(tenantId);
        if (currentCount >= tenant.getPiano().getMaxOmbrelloni()) {
            throw new RuntimeException(
                    "Limite ombrelloni raggiunto per il piano " + tenant.getPiano().name() +
                            ". Effettua l'upgrade per aggiungere più ombrelloni."
            );
        }

        // Verifica che il numero non sia già usato
        if (ombrelloneRepository.existsByTenantIdAndNumero(tenantId, numero)) {
            throw new RuntimeException("Numero ombrellone già esistente");
        }

        Ombrellone ombrellone = Ombrellone.builder()
                .tenantId(tenantId)
                .numero(numero)
                .fila(fila)
                .tipo(tipo != null ? tipo : TipoOmbrellone.STANDARD)
                .descrizione(descrizione)
                .posizioneX(posizioneX)
                .posizioneY(posizioneY)
                .attivo(true)
                .build();

        return ombrelloneRepository.save(ombrellone);
    }

    /**
     * Trova tutti gli ombrelloni del tenant corrente.
     */
    public List<Ombrellone> findAll() {
        UUID tenantId = TenantContext.getTenantId();
        return ombrelloneRepository.findByTenantId(tenantId);
    }

    /**
     * Trova tutti gli ombrelloni attivi del tenant corrente.
     */
    public List<Ombrellone> findAllActive() {
        UUID tenantId = TenantContext.getTenantId();
        return ombrelloneRepository.findByTenantIdAndAttivoTrue(tenantId);
    }

    /**
     * Trova un ombrellone per ID (con verifica tenant).
     */
    public Ombrellone findById(UUID id) {
        UUID tenantId = TenantContext.getTenantId();
        return ombrelloneRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Ombrellone non trovato"));
    }

    /**
     * Trova ombrelloni per fila.
     */
    public List<Ombrellone> findByFila(String fila) {
        UUID tenantId = TenantContext.getTenantId();
        return ombrelloneRepository.findByTenantIdAndFila(tenantId, fila);
    }

    /**
     * Trova ombrelloni per tipo.
     */
    public List<Ombrellone> findByTipo(TipoOmbrellone tipo) {
        UUID tenantId = TenantContext.getTenantId();
        return ombrelloneRepository.findByTenantIdAndTipo(tenantId, tipo);
    }

    /**
     * Aggiorna un ombrellone.
     */
    @Transactional
    public Ombrellone update(UUID id, Ombrellone updatedData) {
        Ombrellone ombrellone = findById(id);

        // Aggiorna solo campi modificabili
        if (updatedData.getNumero() != null &&
                !updatedData.getNumero().equals(ombrellone.getNumero())) {
            // Verifica che il nuovo numero non sia già usato
            UUID tenantId = TenantContext.getTenantId();
            if (ombrelloneRepository.existsByTenantIdAndNumero(tenantId, updatedData.getNumero())) {
                throw new RuntimeException("Numero ombrellone già esistente");
            }
            ombrellone.setNumero(updatedData.getNumero());
        }

        if (updatedData.getFila() != null) {
            ombrellone.setFila(updatedData.getFila());
        }
        if (updatedData.getTipo() != null) {
            ombrellone.setTipo(updatedData.getTipo());
        }
        if (updatedData.getDescrizione() != null) {
            ombrellone.setDescrizione(updatedData.getDescrizione());
        }
        if (updatedData.getPosizioneX() != null) {
            ombrellone.setPosizioneX(updatedData.getPosizioneX());
        }
        if (updatedData.getPosizioneY() != null) {
            ombrellone.setPosizioneY(updatedData.getPosizioneY());
        }
        if (updatedData.getNote() != null) {
            ombrellone.setNote(updatedData.getNote());
        }

        return ombrelloneRepository.save(ombrellone);
    }

    /**
     * Disattiva un ombrellone (soft delete).
     */
    @Transactional
    public void deactivate(UUID id) {
        Ombrellone ombrellone = findById(id);
        ombrellone.setAttivo(false);
        ombrelloneRepository.save(ombrellone);
    }

    /**
     * Riattiva un ombrellone.
     */
    @Transactional
    public void activate(UUID id) {
        Ombrellone ombrellone = findById(id);
        ombrellone.setAttivo(true);
        ombrelloneRepository.save(ombrellone);
    }

    /**
     * Elimina definitivamente un ombrellone (solo se non ha prenotazioni).
     */
    @Transactional
    public void delete(UUID id) {
        Ombrellone ombrellone = findById(id);

        // Verifica che non ci siano prenotazioni attive
        // TODO: Implementare controllo prenotazioni

        ombrelloneRepository.delete(ombrellone);
    }

    /**
     * Conta gli ombrelloni del tenant corrente.
     */
    public long count() {
        UUID tenantId = TenantContext.getTenantId();
        return ombrelloneRepository.countByTenantId(tenantId);
    }

    /**
     * Conta gli ombrelloni attivi del tenant corrente.
     */
    public long countActive() {
        UUID tenantId = TenantContext.getTenantId();
        return ombrelloneRepository.countByTenantIdAndAttivoTrue(tenantId);
    }

    /**
     * Creazione batch di ombrelloni (utile per setup iniziale).
     */
    @Transactional
    public List<Ombrellone> createBatch(List<Ombrellone> ombrelloni) {
        UUID tenantId = TenantContext.getTenantId();

        // Imposta il tenantId su tutti
        ombrelloni.forEach(o -> o.setTenantId(tenantId));

        // Verifica limiti
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant non trovato"));

        long currentCount = ombrelloneRepository.countByTenantIdAndAttivoTrue(tenantId);
        if (currentCount + ombrelloni.size() > tenant.getPiano().getMaxOmbrelloni()) {
            throw new RuntimeException(
                    "Limite ombrelloni raggiunto. Piano attuale: " + tenant.getPiano().name()
            );
        }

        return ombrelloneRepository.saveAll(ombrelloni);
    }
}