package com.beachbooking.controller;

import com.beachbooking.model.dto.request.CreateOmbrelloneRequest;
import com.beachbooking.model.entity.Ombrellone;
import com.beachbooking.model.enums.TipoOmbrellone;
import com.beachbooking.service.OmbrelloneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ombrelloni")
@Validated
public class OmbrelloneController {

    @Autowired
    private OmbrelloneService ombrelloneService;

    @PostMapping
    public ResponseEntity<Ombrellone> create(@Valid @RequestBody CreateOmbrelloneRequest request) {
        Ombrellone created = ombrelloneService.create(
                request.getNumero(),
                request.getFila(),
                request.getTipo(),
                request.getDescrizione(),
                request.getPosizioneX(),
                request.getPosizioneY()
        );
        return ResponseEntity.ok(created);
    }

    @PostMapping("/batch")
    public ResponseEntity<List<Ombrellone>> createBatch(@RequestBody List<Ombrellone> ombrelloni) {
        List<Ombrellone> created = ombrelloneService.createBatch(ombrelloni);
        return ResponseEntity.ok(created);
    }

    @GetMapping
    public ResponseEntity<List<Ombrellone>> findAll() {
        return ResponseEntity.ok(ombrelloneService.findAll());
    }

    @GetMapping("/active")
    public ResponseEntity<List<Ombrellone>> findAllActive() {
        return ResponseEntity.ok(ombrelloneService.findAllActive());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ombrellone> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ombrelloneService.findById(id));
    }

    @GetMapping("/fila/{fila}")
    public ResponseEntity<List<Ombrellone>> findByFila(@PathVariable String fila) {
        return ResponseEntity.ok(ombrelloneService.findByFila(fila));
    }

    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<Ombrellone>> findByTipo(@PathVariable TipoOmbrellone tipo) {
        return ResponseEntity.ok(ombrelloneService.findByTipo(tipo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Ombrellone> update(@PathVariable UUID id, @RequestBody Ombrellone updatedData) {
        return ResponseEntity.ok(ombrelloneService.update(id, updatedData));
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        ombrelloneService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<Void> activate(@PathVariable UUID id) {
        ombrelloneService.activate(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        ombrelloneService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count")
    public ResponseEntity<Long> count() {
        return ResponseEntity.ok(ombrelloneService.count());
    }

    @GetMapping("/count/active")
    public ResponseEntity<Long> countActive() {
        return ResponseEntity.ok(ombrelloneService.countActive());
    }
}
