package com.example.dynamiccrud.service;

import com.example.dynamiccrud.config.EntityRegistry;
import com.example.dynamiccrud.repository.GenericRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

@Service
public class GenericCrudService {

    private final EntityRegistry entityRegistry;
    private final GenericRepository genericRepository;
    private final ObjectMapper objectMapper;

    public GenericCrudService(EntityRegistry entityRegistry,
                              GenericRepository genericRepository,
                              ObjectMapper objectMapper) {
        this.entityRegistry = entityRegistry;
        this.genericRepository = genericRepository;
        this.objectMapper = objectMapper;
    }

    public List<?> findAll(String entityName) {
        Class<?> entityClass = resolveEntity(entityName);
        return genericRepository.findAll(entityClass);
    }

    public Object findById(String entityName, Long id) {
        Class<?> entityClass = resolveEntity(entityName);
        return genericRepository.findById(entityClass, id)
                .orElseThrow(() -> notFound(entityName, id));
    }

    public Object create(String entityName, Map<String, Object> payload) {
        Class<?> entityClass = resolveEntity(entityName);
        Object entity = objectMapper.convertValue(payload, entityClass);
        clearId(entity);
        return genericRepository.save(entity);
    }

    public Object update(String entityName, Long id, Map<String, Object> payload) {
        Class<?> entityClass = resolveEntity(entityName);
        Object existing = genericRepository.findById(entityClass, id)
                .orElseThrow(() -> notFound(entityName, id));

        Object updated = objectMapper.convertValue(payload, entityClass);
        setId(updated, id);
        copyNonNullFields(updated, existing);
        return genericRepository.save(existing);
    }

    public void delete(String entityName, Long id) {
        Class<?> entityClass = resolveEntity(entityName);
        boolean deleted = genericRepository.deleteById(entityClass, id);
        if (!deleted) {
            throw notFound(entityName, id);
        }
    }

    private Class<?> resolveEntity(String entityName) {
        return entityRegistry.resolve(entityName)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Unknown entity: '" + entityName + "'. Registered: " + entityRegistry.getRegisteredEntities()
                ));
    }

    private ResponseStatusException notFound(String entityName, Long id) {
        return new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                entityName + " with id " + id + " not found"
        );
    }

    private void clearId(Object entity) {
        try {
            Field idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, null);
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
            // entity may not expose an id field via reflection naming
        }
    }

    private void setId(Object entity, Long id) {
        try {
            Field idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to set entity id", e);
        }
    }

    private void copyNonNullFields(Object source, Object target) {
        for (Field field : source.getClass().getDeclaredFields()) {
            if ("id".equals(field.getName())) {
                continue;
            }
            field.setAccessible(true);
            try {
                Object value = field.get(source);
                if (value != null) {
                    field.set(target, value);
                }
            } catch (IllegalAccessException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to update entity", e);
            }
        }
    }
}
