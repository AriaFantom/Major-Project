package com.project.bizconnect.repository;

import com.project.bizconnect.entity.Order;
import com.project.bizconnect.entity.Store;
import com.project.bizconnect.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerOrderByCreatedAtDesc(User customer);
    List<Order> findByStore_Owner(User seller);

    // New methods for store statistics
    List<Order> findByStore(Store store);
    List<Order> findByStoreOrderByCreatedAtDesc(Store store, Pageable pageable);
}
