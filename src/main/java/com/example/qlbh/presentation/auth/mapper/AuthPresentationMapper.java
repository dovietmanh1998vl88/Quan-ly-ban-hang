package com.example.qlbh.presentation.auth.mapper;

import com.example.qlbh.application.auth.command.LoginCommand;
import com.example.qlbh.application.auth.command.RegisterCommand;
import com.example.qlbh.application.auth.dto.AuthDto;
import com.example.qlbh.application.auth.dto.UserDto;
import com.example.qlbh.presentation.auth.request.LoginRequest;
import com.example.qlbh.presentation.auth.request.RegisterRequest;
import com.example.qlbh.presentation.auth.response.LoginResponse;
import com.example.qlbh.presentation.auth.response.RegisterResponse;
import org.springframework.stereotype.Component;

@Component
public class AuthPresentationMapper {

  public LoginResponse toLoginResponse(
      AuthDto dto
  ) {

    return new LoginResponse(

        dto.getAccessToken()
    );
  }

  public RegisterResponse toRegisterResponse(
      UserDto dto
  ) {

    return new RegisterResponse(

        dto.getId(),

        dto.getUsername()
    );
  }

  public LoginCommand toCommand(
      LoginRequest request
  ) {

    LoginCommand command =
        new LoginCommand();

    command.setUsername(
        request.username()
    );

    command.setPassword(
        request.password()
    );

    return command;
  }

  public RegisterCommand toCommand(
      RegisterRequest request
  ) {

    RegisterCommand command =
        new RegisterCommand();

    command.setUsername(
        request.username()
    );

    command.setPassword(
        request.password()
    );

    return command;
  }
}
