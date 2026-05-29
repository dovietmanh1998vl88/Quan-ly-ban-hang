package com.example.qlbh.presentation.auth;

import com.example.qlbh.application.auth.dto.AuthDto;
import com.example.qlbh.application.auth.dto.UserDto;
import com.example.qlbh.application.auth.usecase.LoginUseCase;
import com.example.qlbh.application.auth.usecase.RegisterUseCase;
import com.example.qlbh.common.response.BaseResponse;
import com.example.qlbh.presentation.auth.mapper.AuthPresentationMapper;
import com.example.qlbh.presentation.auth.request.LoginRequest;
import com.example.qlbh.presentation.auth.request.RegisterRequest;
import com.example.qlbh.presentation.auth.response.LoginResponse;
import com.example.qlbh.presentation.auth.response.RegisterResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Đăng ký và đăng nhập")
public class AuthController {

  private final RegisterUseCase registerUseCase;

  private final LoginUseCase loginUseCase;
  private final AuthPresentationMapper mapper;

  @Operation(summary = "Đăng ký tài khoản mới")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Đăng ký thành công"),
      @ApiResponse(responseCode = "400", description = "Username đã tồn tại hoặc dữ liệu không hợp lệ")
  })
  @PostMapping("/register")
  public BaseResponse<RegisterResponse> register(

      @Valid
      @RequestBody
      RegisterRequest request
  ) {

    UserDto dto = registerUseCase.execute(mapper.toCommand(request));
    return BaseResponse.success(mapper.toRegisterResponse(dto));
  }

  @Operation(summary = "Đăng nhập — lấy JWT token")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Đăng nhập thành công"),
      @ApiResponse(responseCode = "401", description = "Sai username hoặc password")
  })
  @PostMapping("/login")
  public BaseResponse<LoginResponse> login(

      @Valid
      @RequestBody
      LoginRequest request
  ) {

    AuthDto dto = loginUseCase.execute(mapper.toCommand(request));
    return BaseResponse.success(mapper.toLoginResponse(dto));
  }
}