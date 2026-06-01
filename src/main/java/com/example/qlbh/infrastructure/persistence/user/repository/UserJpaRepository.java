package com.example.qlbh.infrastructure.persistence.user.repository;

import com.example.qlbh.infrastructure.persistence.user.entity.UserEntity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository
    extends JpaRepository<UserEntity, String> {

  Optional<UserEntity> findByUsername(String username);
}
