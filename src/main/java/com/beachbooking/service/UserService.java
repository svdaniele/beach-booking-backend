package com.beachbooking.service;

import com.beachbooking.exception.ResourceNotFoundException;
import com.beachbooking.exception.UnauthorizedException;
import com.beachbooking.model.entity.User;
import com.beachbooking.model.enums.RuoloUtente;
import com.beachbooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User non trovato"));
    }

    public User getUtenteById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User non trovato"));
    }

    public List<User> getUsersByRuolo(RuoloUtente ruolo) {
        return userRepository.findByRuolo(ruolo);
    }

    @Transactional
    public User updateProfile(String email, Map<String, Object> updates) {
        User user = getUserByEmail(email);

        if (updates.containsKey("nome")) user.setNome((String) updates.get("nome"));
        if (updates.containsKey("cognome")) user.setCognome((String) updates.get("cognome"));
        if (updates.containsKey("telefono")) user.setTelefono((String) updates.get("telefono"));
        if (updates.containsKey("codiceFiscale")) user.setCodiceFiscale((String) updates.get("codiceFiscale"));

        return userRepository.save(user);
    }

    @Transactional
    public void changePassword(String email, String currentPassword, String newPassword) {
        User user = getUserByEmail(email);

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new UnauthorizedException("Password attuale non corretta");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public String uploadAvatar(String email, MultipartFile file) {
        User user = getUserByEmail(email);
        // Implementa upload su storage (S3, filesystem, etc.)
        String avatarUrl = "/uploads/avatars/" + user.getId() + ".jpg";
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);
        return avatarUrl;
    }

    @Transactional
    public void updatePreferences(String email, Map<String, Object> preferences) {
        User user = getUserByEmail(email);
        // Salva preferenze in campo JSON o tabella separata
        userRepository.save(user);
    }
}