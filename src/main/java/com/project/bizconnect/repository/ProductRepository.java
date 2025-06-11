package com.project.bizconnect.repository;

import com.project.bizconnect.entity.Product;
import com.project.bizconnect.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByStore(Store store);

    @Query("SELECT p FROM Product p JOIN p.orderItems oi GROUP BY p ORDER BY COUNT(oi) DESC")
    List<Product> findBestSellingProducts(Pageable pageable);

    List<Product> findByOrderByCreatedAtDesc(Pageable pageable);

    // New method for store statistics
    long countByStore(Store store);
}
