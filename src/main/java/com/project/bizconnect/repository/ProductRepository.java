package com.project.bizconnect.repository;

import com.project.bizconnect.entity.Product;
import com.project.bizconnect.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByStore(Store store);
}
