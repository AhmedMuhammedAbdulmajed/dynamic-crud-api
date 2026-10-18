package com.example.dynamiccrud.config;

import com.example.dynamiccrud.entity.Customer;
import com.example.dynamiccrud.entity.Product;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Maps URL entity names (e.g. "products", "customers") to JPA entity classes.
 * Register new entities here to expose them through the dynamic CRUD API.
 */
@Component
public class EntityRegistry {

    private final Map<String, Class<?>> registry = new LinkedHashMap<>();

    @PostConstruct
    public void init() {
        register("products", Product.class);
        register("customers", Customer.class);
    }

    public void register(String name, Class<?> entityClass) {
        registry.put(name.toLowerCase(), entityClass);
    }

    public Optional<Class<?>> resolve(String name) {
        return Optional.ofNullable(registry.get(name.toLowerCase()));
    }

    public Set<String> getRegisteredEntities() {
        return Collections.unmodifiableSet(registry.keySet());
    }

    public boolean isRegistered(String name) {
        return registry.containsKey(name.toLowerCase());
    }
}
