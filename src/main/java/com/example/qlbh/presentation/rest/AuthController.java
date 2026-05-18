package com.example.qlbh.presentation.rest;

import com.example.qlbh.application.auth.dto.AuthDto;

import com.example.qlbh.application.auth.dto.UserDto;
import com.example.qlbh.application.auth.usecase.LoginUseCase;

import com.example.qlbh.application.auth.usecase.RegisterUseCase;

import com.example.qlbh.presentation.mapper.AuthPresentationMapper;
import com.example.qlbh.presentation.request.LoginRequest;
import com.example.qlbh.presentation.request.RegisterRequest;
import com.example.qlbh.presentation.response.LoginResponse;
import com.example.qlbh.presentation.response.RegisterResponse;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

  private final RegisterUseCase registerUseCase;

  private final LoginUseCase loginUseCase;
  private final AuthPresentationMapper mapper;

  @PostMapping("/register")
  public RegisterResponse register(

      @Valid
      @RequestBody
      RegisterRequest request
  ) {

    UserDto dto = registerUseCase.execute(mapper.toCommand(request));
    return mapper.toRegisterResponse(dto);
  }

  @PostMapping("/login")
  public LoginResponse login(

      @Valid
      @RequestBody
      LoginRequest request
  ) {

    AuthDto dto = loginUseCase.execute(mapper.toCommand(request));
    return mapper.toLoginResponse(dto);
  }
}