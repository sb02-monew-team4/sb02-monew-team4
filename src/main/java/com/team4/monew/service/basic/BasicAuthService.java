package com.team4.monew.service.basic;

import com.team4.monew.dto.user.UserDto;
import com.team4.monew.dto.user.UserLoginRequest;
import com.team4.monew.entity.User;
import com.team4.monew.exception.user.InvalidCredentialsException;
import com.team4.monew.exception.user.UserNotFoundException;
import com.team4.monew.mapper.UserMapper;
import com.team4.monew.repository.UserRepository;
import com.team4.monew.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional
public class BasicAuthService implements AuthService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;

  @Override
  public UserDto login(UserLoginRequest request) {
    String email = request.email();
    String password = request.password();

    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> UserNotFoundException.byEmail(email));

    if (!user.getPassword().equals(password)) {
      throw InvalidCredentialsException.wrongPassword();
    }

    return userMapper.toDto(user);
  }
}
