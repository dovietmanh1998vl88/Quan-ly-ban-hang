package com.example.qlbh.domain.auth.model;

import com.example.qlbh.common.enums.Role;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class User {

  private Long id;

  private String username;

  private String password;

  private Role role;
}
