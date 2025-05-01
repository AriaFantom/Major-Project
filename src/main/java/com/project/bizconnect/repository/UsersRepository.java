package com.project.bizconnect.repository;

import com.project.bizconnect.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersRepository extends JpaRepository<User, Integer> {
}
