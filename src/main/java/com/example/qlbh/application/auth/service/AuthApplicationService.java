package com.example.qlbh.application.auth.service;

import com.example.qlbh.application.auth.command.LoginCommand;

import com.example.qlbh.application.auth.command.RegisterCommand;

import com.example.qlbh.application.auth.dto.AuthDto;

import com.example.qlbh.application.auth.dto.UserDto;
import com.example.qlbh.application.auth.mapper.AuthApplicationMapper;

import com.example.qlbh.application.auth.usecase.LoginUseCase;

import com.example.qlbh.application.auth.usecase.RegisterUseCase;

import com.example.qlbh.application.port.TokenProvider;
import com.example.qlbh.common.exception.UnauthorizedException;
import com.example.qlbh.domain.auth.model.User;

import com.example.qlbh.domain.auth.repository.UserDomainRepository;

import com.example.qlbh.domain.auth.service.UserDomainService;
import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.AuthenticationManager;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthApplicationService
    implements RegisterUseCase,
    LoginUseCase {

  private final UserDomainRepository userRepository;

  private final AuthApplicationMapper mapper;

  private final AuthenticationManager authenticationManager;

  private final TokenProvider tokenProvider;

  private final UserDomainService userDomainService;

  @Override
  public UserDto execute(RegisterCommand command) {

    boolean exists = userRepository
        .findByUsername(command.getUsername())
        .isPresent();

    userDomainService.validateUsernameNotTaken(command.getUsername(), exists);

    User user = mapper.toDomain(command);

    User saved = userRepository.save(user);
    return new UserDto (

        saved.getId(),

        saved.getUsername()
    );
  }

  @Override
  public AuthDto execute(LoginCommand command) {

    try {
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(
              command.getUsername(),
              command.getPassword()
          )
      );
    } catch (AuthenticationException e) {
      // Spring Security throw AuthenticationException khi sai pass
      // Ta bắt lại và throw exception của mình
      throw new UnauthorizedException("Sai username hoặc password");
    }

    String token = tokenProvider.generateToken(command.getUsername());
    return new AuthDto(token);
  }
}