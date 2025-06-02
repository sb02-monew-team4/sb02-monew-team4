package com.team4.monew.service;

import com.team4.monew.dto.user.UserDto;
import com.team4.monew.dto.user.UserRegisterRequest;
import com.team4.monew.dto.user.UserUpdateRequest;
import com.team4.monew.entity.User;
import java.util.UUID;

public interface UserService {
  UserDto register(UserRegisterRequest registerRequest);
  UserDto update(UUID userId, UserUpdateRequest updateRequest);
  User softDelete(UUID userId);
  void hardDelete(UUID userId);
}
