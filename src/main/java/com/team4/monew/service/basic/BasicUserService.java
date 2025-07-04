package com.team4.monew.service.basic;

import com.team4.monew.dto.user.UserDto;
import com.team4.monew.dto.user.UserRegisterRequest;
import com.team4.monew.dto.user.UserUpdateRequest;
import com.team4.monew.entity.User;
import com.team4.monew.exception.user.UserAlreadyExistException;
import com.team4.monew.exception.user.UserNotFoundException;
import com.team4.monew.mapper.UserMapper;
import com.team4.monew.repository.UserRepository;
import com.team4.monew.service.UserActivityService;
import com.team4.monew.service.UserService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class BasicUserService implements UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final UserActivityService userActivityService;

  @Override
  public UserDto register(UserRegisterRequest registerRequest) {
    String email = registerRequest.email();
    if (userRepository.existsByEmail(email)) {
      throw UserAlreadyExistException.byEmail(email);
    }

    User user = User.create(registerRequest.email(), registerRequest.nickname(),
        registerRequest.password());
    User registeredUser = userRepository.save(user);

    userActivityService.create(user);

    return userMapper.toDto(registeredUser);
  }

  @Override
  public UserDto update(UUID userId, UserUpdateRequest updateRequest) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> UserNotFoundException.byId(userId));

    user.update(updateRequest.nickname());

    userActivityService.updateUser(user);

    return userMapper.toDto(user);
  }

  @Override
  public User softDelete(UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> UserNotFoundException.byId(userId));

    user.updateIsDeleted();

    return user;
  }

  @Override
  public void hardDelete(UUID userId) {
    if (!userRepository.existsById(userId)) {
      throw UserNotFoundException.byId(userId);
    }
    userRepository.deleteById(userId);
  }


}
