package com.beachbooking.controller;

import com.beachbooking.model.entity.User;
import com.beachbooking.model.enums.RuoloUtente;
import com.beachbooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class CustomerController {

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
}