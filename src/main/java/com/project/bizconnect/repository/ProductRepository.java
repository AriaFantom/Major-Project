package com.project.bizconnect.repository;

import com.project.bizconnect.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

}
