package com.example.qlbh.domain.auth.repository;

import com.example.qlbh.domain.auth.model.User;

import java.util.Optional;

public interface UserDomainRepository {

  Optional<User> findByUsername(String username);

  User save(User user);
}