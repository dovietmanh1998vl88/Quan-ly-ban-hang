package com.example.qlbh.application.auth.mapper;

import com.example.qlbh.application.auth.command.RegisterCommand;

import com.example.qlbh.common.enums.Role;

import com.example.qlbh.domain.auth.model.User;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthApplicationMapper {

  private final PasswordEncoder passwordEncoder;

  public User toDomain(RegisterCommand command) {

    return User.builder()

        .username(command.getUsername())

        .password(
            passwordEncoder.encode(
                command.getPassword()
            )
        )

        .role(Role.CUSTOMER)

        .build();
  }
}
