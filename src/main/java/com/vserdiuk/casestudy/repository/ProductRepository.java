package com.vserdiuk.casestudy.repository;

import com.vserdiuk.casestudy.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

/**
 * Repository interface for managing {@link Product} entities.
 * Extends {@link JpaRepository} to provide standard CRUD operations and additional query methods for the {@link Product} entity.
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Checks if a {@link Product} with the specified name exists in the database.
     *
     * @param name the name of the product to check
     * @return {@code true} if a product with the given name exists, {@code false} otherwise
     */
    boolean existsByName(String name);

    /**
     * Retrieves a list of {@link Product} entities whose names match any of the provided names.
     *
     * @param names a list of product names to search for
     * @return a list of {@link Product} entities with matching names
     */
    List<Product> findByNameIn(List<String> names);

    /**
     * Retrieves a list of {@link Product} entities with the specified IDs, applying an optimistic lock.
     * The query uses {@link LockModeType#OPTIMISTIC} to ensure data consistency during transactions.
     *
     * @param ids a set of product IDs to retrieve
     * @return a list of {@link Product} entities matching the provided IDs
     */
    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT p FROM Product p WHERE p.id IN :ids")
    List<Product> findAllByIdWithLock(@Param("ids") Set<Long> ids);
}