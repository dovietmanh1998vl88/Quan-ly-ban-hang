package com.example.qlbh.application.auth.usecase;

import com.example.qlbh.application.auth.command.LoginCommand;

import com.example.qlbh.application.auth.dto.AuthDto;

public interface LoginUseCase {

  AuthDto execute(LoginCommand command);
}