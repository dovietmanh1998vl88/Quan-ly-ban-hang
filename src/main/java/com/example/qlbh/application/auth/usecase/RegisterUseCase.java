package com.example.qlbh.application.auth.usecase;

import com.example.qlbh.application.auth.command.RegisterCommand;
import com.example.qlbh.application.auth.dto.UserDto;

public interface RegisterUseCase {

  UserDto execute(RegisterCommand command);
}
