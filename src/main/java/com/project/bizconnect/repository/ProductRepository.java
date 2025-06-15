package com.project.bizconnect.repository;

import com.project.bizconnect.entity.Product;
import com.project.bizconnect.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByStore(Store store);

    @Query("SELECT p FROM Product p JOIN p.orderItems oi GROUP BY p ORDER BY COUNT(oi) DESC")
    List<Product> findBestSellingProducts(Pageable pageable);

    List<Product> findByOrderByCreatedAtDesc(Pageable pageable);

    // New method for store statistics
    long countByStore(Store store);

    // New method for searching products by name (case insensitive, partial match)
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Product> findByNameContainingIgnoreCase(@Param("searchTerm") String searchTerm);
}
