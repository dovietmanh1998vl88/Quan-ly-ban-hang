package com.example.qlbh.infrastructure.persistence.mapper;

import com.example.qlbh.domain.auth.model.User;

import com.example.qlbh.infrastructure.persistence.entity.UserEntity;

import org.springframework.stereotype.Component;

@Component
public class UserMapper {

  public User toDomain(UserEntity entity) {

    return User.builder()

        .id(entity.getId())

        .username(entity.getUsername())

        .password(entity.getPassword())

        .role(entity.getRole())

        .build();
  }

  public UserEntity toEntity(User domain) {

    UserEntity entity = new UserEntity();

    entity.setId(domain.getId());

    entity.setUsername(domain.getUsername());

    entity.setPassword(domain.getPassword());

    entity.setRole(domain.getRole());

    return entity;
  }
}