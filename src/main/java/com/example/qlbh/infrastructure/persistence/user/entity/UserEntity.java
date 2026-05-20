package com.example.qlbh.infrastructure.persistence.user.entity;

import com.example.qlbh.common.enums.Role;

import com.example.qlbh.infrastructure.persistence.BaseEntity;
import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users")
public class UserEntity extends BaseEntity {

  @Column(nullable = false, unique = true)
  private String username;

  @Column(nullable = false)
  private String password;

  @Enumerated(EnumType.STRING)

  @Column(nullable = false)
  private Role role = Role.CUSTOMER;
}
