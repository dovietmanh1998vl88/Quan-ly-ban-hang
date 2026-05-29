package com.example.qlbh.common.advice;

import com.example.qlbh.common.exception.BusinessException;
import com.example.qlbh.common.exception.ForbiddenException;
import com.example.qlbh.common.exception.NotFoundException;
import com.example.qlbh.common.exception.UnauthorizedException;
import com.example.qlbh.common.response.BaseResponse;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {


  // ✅ Con trước — cụ thể nhất
  @ExceptionHandler(NotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public BaseResponse<String> handleNotFoundException(NotFoundException ex) {
    return BaseResponse.error(ex.getMessage());
  }

  // ✅ Con trước
  @ExceptionHandler(UnauthorizedException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public BaseResponse<String> handleUnauthorizedException(UnauthorizedException ex) {
    return BaseResponse.error(ex.getMessage());
  }

  @ExceptionHandler(ForbiddenException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public BaseResponse<String> handleForbiddenException(ForbiddenException ex) {
    return BaseResponse.error(ex.getMessage());
  }

  // ✅ Cha sau — tổng quát hơn
  @ExceptionHandler(BusinessException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public BaseResponse<String> handleBusinessException(BusinessException ex) {
    return BaseResponse.error(ex.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public BaseResponse<String> handleValidationException(MethodArgumentNotValidException ex) {
    String message = ex.getBindingResult().getFieldError().getDefaultMessage();
    return BaseResponse.error(message);
  }

  // ✅ Exception.class — catch-all, luôn đặt cuối cùng
  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public BaseResponse<String> handleException(Exception ex) {
    return BaseResponse.error(ex.getMessage());
  }
}