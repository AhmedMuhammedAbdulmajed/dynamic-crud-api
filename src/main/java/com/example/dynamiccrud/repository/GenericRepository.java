package com.example.dynamiccrud.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class GenericRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public <T> List<T> findAll(Class<T> entityClass) {
        String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e";
        TypedQuery<T> query = entityManager.createQuery(jpql, entityClass);
        return query.getResultList();
    }

    public <T> Optional<T> findById(Class<T> entityClass, Long id) {
        return Optional.ofNullable(entityManager.find(entityClass, id));
    }

    @Transactional
    public <T> T save(T entity) {
        return entityManager.merge(entity);
    }

    @Transactional
    public <T> void delete(T entity) {
        entityManager.remove(entityManager.contains(entity) ? entity : entityManager.merge(entity));
    }

    @Transactional
    public <T> boolean deleteById(Class<T> entityClass, Long id) {
        T entity = entityManager.find(entityClass, id);
        if (entity == null) {
            return false;
        }
        entityManager.remove(entity);
        return true;
    }
}
