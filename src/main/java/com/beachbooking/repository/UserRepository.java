package com.beachbooking.repository;

import com.beachbooking.model.entity.*;
import com.beachbooking.model.enums.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


// ============= UserRepository.java =============
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndTenantId(String email, UUID tenantId);

    List<User> findByTenantId(UUID tenantId);

    List<User> findByTenantIdAndRuolo(UUID tenantId, RuoloUtente ruolo);

    Optional<User> findByEmailVerificationToken(String token);

    Optional<User> findByPasswordResetToken(String token);

    boolean existsByEmail(String email);

    boolean existsByEmailAndTenantId(String email, UUID tenantId);

    @Query("SELECT COUNT(u) FROM User u WHERE u.tenantId = :tenantId AND u.ruolo = :ruolo")
    long countByTenantIdAndRuolo(@Param("tenantId") UUID tenantId,
                                 @Param("ruolo") RuoloUtente ruolo);

    List<User> findByRuolo(RuoloUtente ruolo);
}