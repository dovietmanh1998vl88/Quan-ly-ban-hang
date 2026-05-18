package com.example.qlbh.presentation.request;

import com.example.qlbh.application.auth.command.RegisterCommand;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(

    @NotBlank
    String username,

    @NotBlank
    String password
) {
    public RegisterCommand toCommand() {

        RegisterCommand command =
            new RegisterCommand();

        command.setUsername(username);

        command.setPassword(password);

        return command;
    }
}
