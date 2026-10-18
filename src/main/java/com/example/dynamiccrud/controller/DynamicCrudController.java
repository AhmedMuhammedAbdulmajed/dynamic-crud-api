package com.example.dynamiccrud.controller;

import com.example.dynamiccrud.config.EntityRegistry;
import com.example.dynamiccrud.service.GenericCrudService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api")
public class DynamicCrudController {

    private final GenericCrudService crudService;
    private final EntityRegistry entityRegistry;

    public DynamicCrudController(GenericCrudService crudService, EntityRegistry entityRegistry) {
        this.crudService = crudService;
        this.entityRegistry = entityRegistry;
    }

    @GetMapping("/entities")
    public Set<String> listEntities() {
        return entityRegistry.getRegisteredEntities();
    }

    @GetMapping("/{entity}")
    public ResponseEntity<?> findAll(@PathVariable String entity) {
        return ResponseEntity.ok(crudService.findAll(entity));
    }

    @GetMapping("/{entity}/{id}")
    public ResponseEntity<?> findById(@PathVariable String entity, @PathVariable Long id) {
        return ResponseEntity.ok(crudService.findById(entity, id));
    }

    @PostMapping("/{entity}")
    public ResponseEntity<?> create(@PathVariable String entity, @RequestBody Map<String, Object> body) {
        Object created = crudService.create(entity, body);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{entity}/{id}")
    public ResponseEntity<?> update(@PathVariable String entity,
                                    @PathVariable Long id,
                                    @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(crudService.update(entity, id, body));
    }

    @DeleteMapping("/{entity}/{id}")
    public ResponseEntity<Void> delete(@PathVariable String entity, @PathVariable Long id) {
        crudService.delete(entity, id);
        return ResponseEntity.noContent().build();
    }
}
