package com.example.qlbh.presentation.auth.request;

import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(

    @NotBlank
    String username,

    @NotBlank
    String password
) {
}
