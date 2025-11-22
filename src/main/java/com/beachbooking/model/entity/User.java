package com.beachbooking.model.entity;

import com.beachbooking.model.enums.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;


// ============= User.java =============
/**
 * Utente del sistema (può essere admin tenant, staff o cliente).
 */
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_tenant", columnList = "tenant_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id")
    private UUID tenantId; // Nullable per SUPER_ADMIN

    @Column(nullable = false, length = 150)
    private String email;

    @Column(nullable = false, length = 255)
    private String password; // BCrypt hashed

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 100)
    private String cognome;

    @Column(length = 20)
    private String telefono;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RuoloUtente ruolo;

    @Column(nullable = false)
    private Boolean attivo = true;

    @Column(nullable = false)
    private Boolean emailVerificata = false;

    private String emailVerificationToken;

    private String passwordResetToken;

    private LocalDateTime passwordResetExpiry;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime dataRegistrazione;

    @UpdateTimestamp
    private LocalDateTime dataAggiornamento;

    private LocalDateTime ultimoAccesso;

    public String getNomeCompleto() {
        return nome + " " + cognome;
    }
}