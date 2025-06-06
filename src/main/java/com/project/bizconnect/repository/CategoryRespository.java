package com.project.bizconnect.repository;


import com.project.bizconnect.entity.Category;
import com.project.bizconnect.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRespository extends JpaRepository<Category, Long> {
    List<Category> findByStore(Store store);
    List<Category> findByStoreStoreId(Long storeId);
}
