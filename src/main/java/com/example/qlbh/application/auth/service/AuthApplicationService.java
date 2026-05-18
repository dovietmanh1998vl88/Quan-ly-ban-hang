package com.example.qlbh.application.auth.service;

import com.example.qlbh.application.auth.command.LoginCommand;

import com.example.qlbh.application.auth.command.RegisterCommand;

import com.example.qlbh.application.auth.dto.AuthDto;

import com.example.qlbh.application.auth.dto.UserDto;
import com.example.qlbh.application.auth.mapper.AuthApplicationMapper;

import com.example.qlbh.application.auth.usecase.LoginUseCase;

import com.example.qlbh.application.auth.usecase.RegisterUseCase;

import com.example.qlbh.application.port.TokenProvider;
import com.example.qlbh.domain.auth.model.User;

import com.example.qlbh.domain.auth.repository.UserDomainRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.AuthenticationManager;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

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

  @Override
  public UserDto execute(RegisterCommand command) {

    User user = mapper.toDomain(command);

    User saved = userRepository.save(user);
    return new UserDto (

        saved.getId(),

        saved.getUsername()
    );
  }

  @Override
  public AuthDto  execute(LoginCommand command) {

    authenticationManager.authenticate(

        new UsernamePasswordAuthenticationToken(

            command.getUsername(),

            command.getPassword()
        )
    );

    String token = tokenProvider.generateToken(
        command.getUsername()
    );

    return new AuthDto (token);
  }
}