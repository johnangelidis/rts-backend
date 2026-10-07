package com.rts.application.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rts.application.entity.User;

public interface UserRepository extends JpaRepository<User, Integer> {
    boolean existsByUsername(String username);
    Optional<User> findByUsername(String username);
}
