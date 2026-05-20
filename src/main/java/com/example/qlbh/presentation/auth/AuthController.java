package com.example.qlbh.presentation.auth;

import com.example.qlbh.application.auth.dto.AuthDto;
import com.example.qlbh.application.auth.dto.UserDto;
import com.example.qlbh.application.auth.usecase.LoginUseCase;
import com.example.qlbh.application.auth.usecase.RegisterUseCase;
import com.example.qlbh.common.response.ApiResponse;
import com.example.qlbh.presentation.auth.mapper.AuthPresentationMapper;
import com.example.qlbh.presentation.auth.request.LoginRequest;
import com.example.qlbh.presentation.auth.request.RegisterRequest;
import com.example.qlbh.presentation.auth.response.LoginResponse;
import com.example.qlbh.presentation.auth.response.RegisterResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

  private final RegisterUseCase registerUseCase;

  private final LoginUseCase loginUseCase;
  private final AuthPresentationMapper mapper;

  @PostMapping("/register")
  public ApiResponse<RegisterResponse> register(

      @Valid
      @RequestBody
      RegisterRequest request
  ) {

    UserDto dto = registerUseCase.execute(mapper.toCommand(request));
    return ApiResponse.success(mapper.toRegisterResponse(dto));
  }

  @PostMapping("/login")
  public ApiResponse<LoginResponse> login(

      @Valid
      @RequestBody
      LoginRequest request
  ) {

    AuthDto dto = loginUseCase.execute(mapper.toCommand(request));
    return ApiResponse.success(mapper.toLoginResponse(dto));
  }
}