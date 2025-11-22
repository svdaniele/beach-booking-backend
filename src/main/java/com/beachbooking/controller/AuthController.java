package com.beachbooking.controller;

import com.beachbooking.model.dto.request.*;
import com.beachbooking.model.dto.response.*;
import com.beachbooking.model.entity.Tenant;
import com.beachbooking.model.entity.User;
import com.beachbooking.repository.TenantRepository;
import com.beachbooking.service.AuthService;
import com.beachbooking.service.TenantService;
import com.beachbooking.tenant.TenantContext;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller per autenticazione e registrazione.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private TenantService tenantService;

    @Autowired
    private TenantRepository tenantRepository;

    /**
     * POST /api/auth/login
     * Login utente (cliente, staff, admin).
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            String token = authService.login(request.getEmail(), request.getPassword());

            // Carica informazioni utente per la risposta
            User user = authService.getCurrentUser();

            // Carica informazioni tenant se presente
            String tenantSlug = null;
            String tenantNome = null;
            if (user.getTenantId() != null) {
                Tenant tenant = tenantRepository.findById(user.getTenantId()).orElse(null);
                if (tenant != null) {
                    tenantSlug = tenant.getSlug();
                    tenantNome = tenant.getNomeStabilimento();
                }
            }

            AuthResponse.UserInfo userInfo = AuthResponse.UserInfo.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .nome(user.getNome())
                    .cognome(user.getCognome())
                    .nomeCompleto(user.getNomeCompleto())
                    .ruolo(user.getRuolo())
                    .tenantId(user.getTenantId())
                    .tenantSlug(tenantSlug)
                    .tenantNome(tenantNome)
                    .emailVerificata(user.getEmailVerificata())
                    .build();

            AuthResponse response = AuthResponse.builder()
                    .token(token)
                    .type("Bearer")
                    .user(userInfo)
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(null);
        }
    }

    /**
     * POST /api/auth/register/customer
     * Registrazione nuovo cliente.
     * Richiede tenantId nel contesto (da header o subdomain).
     */
    @PostMapping("/register/customer")
    public ResponseEntity<?> registerCustomer(@Valid @RequestBody RegisterCustomerRequest request) {
        try {
            UUID tenantId = TenantContext.getTenantId();

            User user = authService.registerCustomer(
                    tenantId,
                    request.getEmail(),
                    request.getPassword(),
                    request.getNome(),
                    request.getCognome(),
                    request.getTelefono()
            );

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(MessageResponse.success(
                            "Registrazione completata. Controlla la tua email per verificare l'account."
                    ));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(MessageResponse.error(e.getMessage()));
        }
    }

    /**
     * POST /api/auth/register/tenant
     * Registrazione nuovo tenant (stabilimento).
     * Endpoint pubblico.
     */
    @PostMapping("/register/tenant")
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

            TenantResponse response = TenantResponse.builder()
                    .id(tenant.getId())
                    .nomeStabilimento(tenant.getNomeStabilimento())
                    .slug(tenant.getSlug())
                    .email(tenant.getEmail())
                    .piano(tenant.getPiano().name())
                    .stato(tenant.getStato().name())
                    .dataCreazione(tenant.getDataCreazione())
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(MessageResponse.error(e.getMessage()));
        }
    }

    /**
     * POST /api/auth/verify-email
     * Verifica email con token.
     */
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        try {
            authService.verifyEmail(request.getToken());
            return ResponseEntity.ok(
                    MessageResponse.success("Email verificata con successo")
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(MessageResponse.error(e.getMessage()));
        }
    }

    /**
     * POST /api/auth/forgot-password
     * Richiesta reset password.
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            authService.requestPasswordReset(request.getEmail());
            return ResponseEntity.ok(
                    MessageResponse.success(
                            "Se l'email esiste, riceverai le istruzioni per il reset della password"
                    )
            );
        } catch (Exception e) {
            // Non rivelare se l'email esiste o no per sicurezza
            return ResponseEntity.ok(
                    MessageResponse.success(
                            "Se l'email esiste, riceverai le istruzioni per il reset della password"
                    )
            );
        }
    }

    /**
     * POST /api/auth/reset-password
     * Reset password con token.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            authService.resetPassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok(
                    MessageResponse.success("Password reimpostata con successo")
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(MessageResponse.error(e.getMessage()));
        }
    }

    /**
     * POST /api/auth/change-password
     * Cambio password (utente autenticato).
     */
    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        try {
            // Verifica che le password coincidano
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                return ResponseEntity.badRequest()
                        .body(MessageResponse.error("Le password non coincidono"));
            }

            User currentUser = authService.getCurrentUser();
            authService.changePassword(
                    currentUser.getId(),
                    request.getOldPassword(),
                    request.getNewPassword()
            );

            return ResponseEntity.ok(
                    MessageResponse.success("Password modificata con successo")
            );

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(MessageResponse.error(e.getMessage()));
        }
    }

    /**
     * GET /api/auth/me
     * Ottieni informazioni utente corrente.
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getCurrentUser() {
        try {
            User user = authService.getCurrentUser();

            UserResponse response = UserResponse.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .nome(user.getNome())
                    .cognome(user.getCognome())
                    .nomeCompleto(user.getNomeCompleto())
                    .telefono(user.getTelefono())
                    .ruolo(user.getRuolo())
                    .attivo(user.getAttivo())
                    .emailVerificata(user.getEmailVerificata())
                    .tenantId(user.getTenantId())
                    .dataRegistrazione(user.getDataRegistrazione())
                    .ultimoAccesso(user.getUltimoAccesso())
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * POST /api/auth/logout
     * Logout (lato client deve eliminare il token).
     */
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> logout() {
        // Con JWT stateless, il logout è gestito lato client
        // eliminando il token. Qui possiamo solo confermare.
        return ResponseEntity.ok(
                MessageResponse.success("Logout effettuato con successo")
        );
    }
}