package com.example.qlbh.domain.auth.service;

import com.example.qlbh.common.exception.BusinessException;


public class UserDomainService {

  public void validateUsernameNotTaken(String username, boolean alreadyExists) {
    if (alreadyExists) {
      throw new BusinessException("Username đã tồn tại");
    }
  }

}
