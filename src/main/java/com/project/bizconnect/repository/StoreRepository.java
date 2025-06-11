package com.project.bizconnect.repository;

import com.project.bizconnect.entity.Store;
import com.project.bizconnect.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StoreRepository extends JpaRepository<Store, Long> {
    List<Store> findByOwner(User owner);
    List<Store> findByIsVerifiedTrue();

    @Query("SELECT s FROM Store s LEFT JOIN s.followers f GROUP BY s ORDER BY COUNT(f) DESC")
    List<Store> findAllByOrderByFollowersCountDesc(Pageable pageable);
}
