package com.example.qlbh.application.auth.command;

import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginCommand {

  @NotBlank
  private String username;

  @NotBlank
  private String password;
}
