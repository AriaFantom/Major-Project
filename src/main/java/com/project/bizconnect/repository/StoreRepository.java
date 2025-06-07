package com.project.bizconnect.repository;

import com.project.bizconnect.entity.Store;
import com.project.bizconnect.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoreRepository extends JpaRepository<Store, Long> {
    List<Store> findByOwner(User owner);
    List<Store> findByIsVerifiedTrue();
}
