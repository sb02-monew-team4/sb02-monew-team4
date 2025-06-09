package com.team4.monew.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.team4.monew.dto.user.UserDto;
import com.team4.monew.dto.user.UserLoginRequest;
import com.team4.monew.entity.User;
import com.team4.monew.exception.ErrorCode;
import com.team4.monew.exception.user.InvalidCredentialsException;
import com.team4.monew.exception.user.UserNotFoundException;
import com.team4.monew.mapper.UserMapper;
import com.team4.monew.repository.UserRepository;
import com.team4.monew.service.basic.BasicAuthService;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

  @InjectMocks
  private BasicAuthService authService;

  @Mock
  private UserRepository userRepository;

  @Mock
  private UserMapper userMapper;

  @Test
  @DisplayName("로그인_성공")
  void login_Success_ShouldReturnUser() {
    // given
    UserLoginRequest request = new UserLoginRequest("login@email.com", "password123");
    String email = request.email();
    String nickname = "testUser";
    UUID userId = UUID.randomUUID();

    User user = User.create(email, nickname, request.password());
    UserDto userDto = new UserDto(userId, email, nickname, Instant.now());

    given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
    given(userMapper.toDto(user)).willReturn(userDto);

    // when
    UserDto result = authService.login(request);

    // then
    assertThat(result.email()).isEqualTo(email);

    then(userRepository).should().findByEmail(email);
    then(userMapper).should().toDto(user);
  }

  @Test
  @DisplayName("로그인_실패_사용자가 존재하지 않는 경우")
  void login_Failure_WhenUserNotFound() {
    // given
    UserLoginRequest request = new UserLoginRequest("login@email.com", "password123");
    String email = request.email();

    given(userRepository.findByEmail(email)).willReturn(Optional.empty());

    // when & then
    UserNotFoundException exception = assertThrows(UserNotFoundException.class,
        () -> authService.login(request));

    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
    assertThat(exception.getDetails().get("email")).isEqualTo(email);

    then(userRepository).should().findByEmail(email);
    then(userMapper).should(never()).toDto(any(User.class));
  }

  @Test
  @DisplayName("로그인_실패_비밀번호가 잘못된 경우")
  void login_Failure_WhenWrongPassword() {
    // given
    String wrongPassword = "password123";
    String originalPassword = "password321";

    UserLoginRequest request = new UserLoginRequest("login@email.com", wrongPassword);
    String email = request.email();

    User user = User.create(email, "testUser", originalPassword);

    given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

    // when & then
    InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class,
        () -> authService.login(request));

    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.WRONG_PASSWORD);

    then(userRepository).should().findByEmail(email);
    then(userMapper).should(never()).toDto(any(User.class));
  }

}
