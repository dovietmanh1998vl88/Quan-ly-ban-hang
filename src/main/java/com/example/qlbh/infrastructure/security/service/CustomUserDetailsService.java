package com.example.qlbh.infrastructure.security.service;

import com.example.qlbh.domain.auth.model.User;

import com.example.qlbh.domain.auth.repository.UserDomainRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService
    implements UserDetailsService {

  private final UserDomainRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(
      String username
  ) throws UsernameNotFoundException {

    User user = userRepository

        .findByUsername(username)

        .orElseThrow(() ->

            new UsernameNotFoundException(
                "User not found"
            )
        );

    return new org.springframework.security.core.userdetails.User(

        user.getUsername(),

        user.getPassword(),

        List.of(

            new SimpleGrantedAuthority(

                "ROLE_" + user.getRole().name()
            )
        )
    );
  }
}
