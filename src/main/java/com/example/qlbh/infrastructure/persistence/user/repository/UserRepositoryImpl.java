package com.example.qlbh.infrastructure.persistence.user.repository;

import com.example.qlbh.domain.auth.model.User;

import com.example.qlbh.domain.auth.repository.UserDomainRepository;

import com.example.qlbh.infrastructure.persistence.user.entity.UserEntity;

import com.example.qlbh.infrastructure.persistence.user.mapper.UserMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl
    implements UserDomainRepository {

  private final UserJpaRepository userJpaRepository;

  private final UserMapper userMapper;

  @Override
  public Optional<User> findByUsername(
      String username
  ) {

    return userJpaRepository

        .findByUsername(username)

        .map(userMapper::toDomain);
  }

  @Override
  public User save(User user) {

    UserEntity entity =
        userMapper.toEntity(user);

    UserEntity saved =
        userJpaRepository.save(entity);

    return userMapper.toDomain(saved);
  }
}
