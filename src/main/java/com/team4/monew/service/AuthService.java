package com.team4.monew.service;

import com.team4.monew.dto.user.UserDto;
import com.team4.monew.dto.user.UserLoginRequest;

public interface AuthService {
  UserDto login(UserLoginRequest request);
}
