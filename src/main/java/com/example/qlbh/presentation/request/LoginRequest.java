package com.example.qlbh.presentation.request;

import com.example.qlbh.application.auth.command.LoginCommand;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

    @NotBlank
    String username,

    @NotBlank
    String password
) {
    public LoginCommand toCommand() {

        LoginCommand command =
            new LoginCommand();

        command.setUsername(username);

        command.setPassword(password);

        return command;
    }
}