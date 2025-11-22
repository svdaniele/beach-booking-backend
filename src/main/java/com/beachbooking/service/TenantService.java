package com.beachbooking.service;

import com.beachbooking.exception.ResourceNotFoundException;
import com.beachbooking.model.entity.Tenant;
import com.beachbooking.model.entity.User;
import com.beachbooking.model.enums.PianoAbbonamento;
import com.beachbooking.model.enums.RuoloUtente;
import com.beachbooking.model.enums.StatoTenant;
import com.beachbooking.repository.TenantRepository;
import com.beachbooking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service per la gestione dei tenant (stabilimenti balneari).
 */
@Service
public class TenantService {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Registra un nuovo tenant con un utente admin.
     * Questa è l'operazione di onboarding iniziale.
     */
    @Transactional
    public Tenant registerTenant(String nomeStabilimento,
                                 String email,
                                 String password,
                                 String nomeAdmin,
                                 String cognomeAdmin,
                                 String telefono,
                                 String indirizzo,
                                 String citta,
                                 String provincia,
                                 String cap) {

        // Verifica che lo slug sia unico
        String slug = generateSlug(nomeStabilimento);
        if (tenantRepository.existsBySlug(slug)) {
            throw new RuntimeException("Stabilimento con questo nome già esistente");
        }

        // Verifica che l'email non sia già usata
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email già registrata");
        }

        // Crea il tenant
        Tenant tenant = Tenant.builder()
                .nomeStabilimento(nomeStabilimento)
                .slug(slug)
                .email(email)
                .telefono(telefono)
                .indirizzo(indirizzo)
                .citta(citta)
                .provincia(provincia)
                .cap(cap)
                .piano(PianoAbbonamento.FREE)
                .stato(StatoTenant.TRIAL)
                .dataScadenzaAbbonamento(LocalDateTime.now().plusDays(30)) // 30 giorni trial
                .build();

        tenant = tenantRepository.save(tenant);

        // Crea l'utente admin del tenant
        User admin = User.builder()
                .tenantId(tenant.getId())
                .email(email)
                .password(passwordEncoder.encode(password))
                .nome(nomeAdmin)
                .cognome(cognomeAdmin)
                .telefono(telefono)
                .ruolo(RuoloUtente.TENANT_ADMIN)
                .attivo(true)
                .emailVerificata(false)
                .build();

        userRepository.save(admin);

        return tenant;
    }

    /**
     * Trova un tenant per ID.
     */
    public Tenant findById(UUID id) {
        return tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant non trovato"));
    }

    /**
     * Trova un tenant per slug.
     */
    public Tenant findBySlug(String slug) {
        return tenantRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant non trovato"));
    }

    /**
     * Verifica se uno slug è disponibile.
     */
    public boolean isSlugAvailable(String slug) {
        return !tenantRepository.existsBySlug(slug);
    }

    /**
     * Aggiorna le informazioni del tenant.
     */
    @Transactional
    public Tenant updateTenant(UUID tenantId, Tenant updatedData) {
        Tenant tenant = findById(tenantId);

        // Aggiorna solo i campi modificabili
        if (updatedData.getNomeStabilimento() != null) {
            tenant.setNomeStabilimento(updatedData.getNomeStabilimento());
        }
        if (updatedData.getDescrizione() != null) {
            tenant.setDescrizione(updatedData.getDescrizione());
        }
        if (updatedData.getIndirizzo() != null) {
            tenant.setIndirizzo(updatedData.getIndirizzo());
        }
        if (updatedData.getTelefono() != null) {
            tenant.setTelefono(updatedData.getTelefono());
        }
        if (updatedData.getLogoUrl() != null) {
            tenant.setLogoUrl(updatedData.getLogoUrl());
        }
        if (updatedData.getConfigurazione() != null) {
            tenant.setConfigurazione(updatedData.getConfigurazione());
        }

        return tenantRepository.save(tenant);
    }

    /**
     * Cambia il piano di abbonamento del tenant.
     */
    @Transactional
    public Tenant upgradePiano(UUID tenantId, PianoAbbonamento nuovoPiano) {
        Tenant tenant = findById(tenantId);

        // Verifica che il nuovo piano sia un upgrade
        if (nuovoPiano.getPrezzoMensile().compareTo(tenant.getPiano().getPrezzoMensile()) < 0) {
            throw new RuntimeException("Non è possibile fare downgrade diretto");
        }

        tenant.setPiano(nuovoPiano);
        tenant.setStato(StatoTenant.ACTIVE);
        tenant.setDataScadenzaAbbonamento(LocalDateTime.now().plusMonths(1));

        return tenantRepository.save(tenant);
    }

    /**
     * Sospende un tenant (es: per mancato pagamento).
     */
    @Transactional
    public void suspendTenant(UUID tenantId) {
        Tenant tenant = findById(tenantId);
        tenant.setStato(StatoTenant.SUSPENDED);
        tenantRepository.save(tenant);
    }

    /**
     * Riattiva un tenant sospeso.
     */
    @Transactional
    public void activateTenant(UUID tenantId) {
        Tenant tenant = findById(tenantId);
        tenant.setStato(StatoTenant.ACTIVE);
        tenantRepository.save(tenant);
    }

    /**
     * Trova tutti i tenant scaduti (per job automatico).
     */
    public List<Tenant> findExpiredTenants() {
        return tenantRepository.findTenantScaduti();
    }

    /**
     * Genera uno slug unico dal nome stabilimento.
     */
    private String generateSlug(String nome) {
        String slug = nome.toLowerCase()
                .replaceAll("[àáâãäå]", "a")
                .replaceAll("[èéêë]", "e")
                .replaceAll("[ìíîï]", "i")
                .replaceAll("[òóôõö]", "o")
                .replaceAll("[ùúûü]", "u")
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();

        // Aggiungi un numero se lo slug esiste già
        String finalSlug = slug;
        int counter = 1;
        while (tenantRepository.existsBySlug(finalSlug)) {
            finalSlug = slug + "-" + counter++;
        }

        return finalSlug;
    }

    /**
     * Ottieni tutti i tenant (admin).
     */
    public List<Tenant> findAll() {
        return tenantRepository.findAll();
    }
}