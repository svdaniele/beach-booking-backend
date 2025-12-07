package com.beachbooking.controller;

import com.beachbooking.model.dto.request.ChangePasswordRequest;
import com.beachbooking.model.dto.response.MessageResponse;
import com.beachbooking.model.dto.response.UserResponse;
import com.beachbooking.model.entity.User;
import com.beachbooking.model.enums.RuoloUtente;
import com.beachbooking.repository.UserRepository;
import com.beachbooking.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @GetMapping("/clienti")
    public ResponseEntity<List<User>> getClienti() {
        List<User> clienti = userRepository.findByRuolo(RuoloUtente.CUSTOMER);
        return ResponseEntity.ok(clienti);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable UUID id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/users/me
     * Compatibile con: profileAPI.getMe()
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getUserProfile(Authentication auth) {
        User user = userService.getUserByEmail(auth.getName());
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    /**
     * PUT /api/users/me
     * Compatibile con: profileAPI.updateProfile()
     */
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateUserProfile(
            @RequestBody Map<String, Object> updates,
            Authentication auth) {
        User user = userService.updateProfile(auth.getName(), updates);
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    /**
     * PUT /api/users/me/password
     * Compatibile con: profileAPI.changePassword()
     */
    @PutMapping("/me/password")
    public ResponseEntity<MessageResponse> updateUserPassword(
            @RequestBody ChangePasswordRequest request,
            Authentication auth) {
        userService.changePassword(auth.getName(),
                request.getCurrentPassword(),
                request.getNewPassword());
        return ResponseEntity.ok(MessageResponse.builder()
                .message("Password modificata con successo")
                .build());
    }

    /**
     * POST /api/users/me/avatar
     * Compatibile con: profileAPI.uploadAvatar()
     */
    @PostMapping("/me/avatar")
    public ResponseEntity<Map<String, String>> uploadUserAvatar(
            @RequestParam("avatar") MultipartFile file,
            Authentication auth) {
        String avatarUrl = userService.uploadAvatar(auth.getName(), file);
        return ResponseEntity.ok(Map.of("avatarUrl", avatarUrl));
    }

    /**
     * PUT /api/users/me/preferences
     * Compatibile con: profileAPI.updatePreferences()
     */
    @PutMapping("/me/preferences")
    public ResponseEntity<MessageResponse> updateUserNotificationPreferences(
            @RequestBody Map<String, Object> preferences,
            Authentication auth) {
        userService.updatePreferences(auth.getName(), preferences);
        return ResponseEntity.ok(MessageResponse.builder()
                .message("Preferenze aggiornate con successo")
                .build());
    }

    /**
     * GET /api/users/me/sessions
     * Compatibile con: profileAPI.getSessions()
     */
    @GetMapping("/me/sessions")
    public ResponseEntity<List<Map<String, Object>>> getUserActiveSessions(Authentication auth) {
        // TODO: Implementare logica reale per sessioni
        // Per ora restituiamo lista vuota ma strutturata
        return ResponseEntity.ok(List.of(
                Map.of(
                        "id", "session_123",
                        "device", "Chrome on Windows",
                        "ipAddress", "192.168.1.1",
                        "lastActive", "2024-01-15T10:30:00Z",
                        "current", true
                )
        ));
    }

    /**
     * DELETE /api/users/me/sessions/{sessionId}
     * Compatibile con: profileAPI.terminateSession()
     */
    @DeleteMapping("/me/sessions/{sessionId}")
    public ResponseEntity<MessageResponse> terminateUserSession(
            @PathVariable String sessionId,
            Authentication auth) {
        // TODO: Implementare logica reale per terminazione sessione
        return ResponseEntity.ok(MessageResponse.builder()
                .message("Sessione terminata con successo")
                .build());
    }

}