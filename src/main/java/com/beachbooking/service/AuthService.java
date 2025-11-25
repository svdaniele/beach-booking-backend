package com.beachbooking.service;

import com.beachbooking.exception.ResourceNotFoundException;

import com.beachbooking.model.entity.User;
import com.beachbooking.model.enums.RuoloUtente;
import com.beachbooking.repository.UserRepository;
import com.beachbooking.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service per autenticazione e gestione utenti.
 */
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    /**
     * Login utente.
     * Restituisce un JWT token.
     */
    public String login(String email, String password) {
        System.out.println("=== AuthService.login() ===");
        System.out.println("Email: " + email);

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
            System.out.println("Authentication SUCCESS");

            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            System.out.println("User found: " + user.getEmail());

            user.setUltimoAccesso(LocalDateTime.now());
            userRepository.save(user);

            String token = jwtTokenProvider.generateTokenWithClaims(
                    email,
                    user.getTenantId(),
                    user.getRuolo().name()
            );

            System.out.println("Token generated: " + token.substring(0, 20) + "...");

            return token;

        } catch (Exception e) {
            System.out.println("Authentication FAILED: " + e.getClass().getName());
            System.out.println("Message: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Registra un nuovo cliente per un tenant.
     */
    @Transactional
    public User registerCustomer(UUID tenantId,
                                 String email,
                                 String password,
                                 String nome,
                                 String cognome,
                                 String telefono) {

        // Verifica che l'email non esista già per questo tenant
        if (userRepository.existsByEmailAndTenantId(email, tenantId)) {
            throw new RuntimeException("Email già registrata per questo stabilimento");
        }

        User user = User.builder()
                .tenantId(tenantId)
                .email(email)
                .password(passwordEncoder.encode(password))
                .nome(nome)
                .cognome(cognome)
                .telefono(telefono)
                .ruolo(RuoloUtente.CUSTOMER)
                .attivo(true)
                .emailVerificata(false)
                .emailVerificationToken(generateVerificationToken())
                .build();

        user = userRepository.save(user);

        // TODO: Invia email di verifica
        // emailService.sendVerificationEmail(user);

        return user;
    }

    /**
     * Registra un nuovo membro dello staff.
     */
    @Transactional
    public User registerStaff(UUID tenantId,
                              String email,
                              String password,
                              String nome,
                              String cognome,
                              String telefono) {

        if (userRepository.existsByEmailAndTenantId(email, tenantId)) {
            throw new RuntimeException("Email già registrata");
        }

        User user = User.builder()
                .tenantId(tenantId)
                .email(email)
                .password(passwordEncoder.encode(password))
                .nome(nome)
                .cognome(cognome)
                .telefono(telefono)
                .ruolo(RuoloUtente.TENANT_ADMIN)
                .attivo(true)
                .emailVerificata(true) // Staff verificato automaticamente
                .build();

        return userRepository.save(user);
    }

    /**
     * Verifica email con token.
     */
    @Transactional
    public void verifyEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Token non valido"));

        user.setEmailVerificata(true);
        user.setEmailVerificationToken(null);
        userRepository.save(user);
    }

    /**
     * Richiedi reset password.
     */
    @Transactional
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        String resetToken = generateResetToken();
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetExpiry(LocalDateTime.now().plusHours(24));

        userRepository.save(user);

        // TODO: Invia email con token reset
        // emailService.sendPasswordResetEmail(user, resetToken);
    }

    /**
     * Reset password con token.
     */
    @Transactional
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new RuntimeException("Token non valido"));

        // Verifica scadenza token
        if (user.getPasswordResetExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token scaduto");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiry(null);

        userRepository.save(user);
    }

    /**
     * Cambia password (utente loggato).
     */
    @Transactional
    public void changePassword(UUID userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        // Verifica vecchia password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Password attuale non corretta");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * Ottieni utente corrente dal SecurityContext.
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));
    }

    private String generateVerificationToken() {
        return UUID.randomUUID().toString();
    }

    private String generateResetToken() {
        return UUID.randomUUID().toString();
    }
}