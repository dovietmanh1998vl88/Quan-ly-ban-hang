package com.example.qlbh.presentation.advice;

import com.example.qlbh.common.exception.BusinessException;
import com.example.qlbh.common.exception.NotFoundException;
import com.example.qlbh.common.response.ApiResponse;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(NotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ApiResponse<String> handleNotFoundException(
      NotFoundException ex
  ) {

    return ApiResponse.error(ex.getMessage());
  }

  @ExceptionHandler(BusinessException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiResponse<String> handleBusinessException(
      BusinessException ex
  ) {

    return ApiResponse.error(ex.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiResponse<String> handleValidationException(
      MethodArgumentNotValidException ex
  ) {

    String message = ex.getBindingResult()
        .getFieldError()
        .getDefaultMessage();

    return ApiResponse.error(message);
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ApiResponse<String> handleException(
      Exception ex
  ) {

    return ApiResponse.error(ex.getMessage());
  }
}