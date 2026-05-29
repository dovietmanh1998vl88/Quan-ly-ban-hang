package com.example.qlbh.common.response;

public record BaseResponse<T>(
    boolean success,
    String message,
    T data
) {

  public static <T> BaseResponse<T> success(T data) {
    return new BaseResponse<>(
        true,
        "Success",
        data
    );
  }

  public static <T> BaseResponse<T> success(String message, T data) {
    return new BaseResponse<>(
        true,
        message,
        data
    );
  }

  public static <T> BaseResponse<T> error(String message) {
    return new BaseResponse<>(
        false,
        message,
        null
    );
  }
}