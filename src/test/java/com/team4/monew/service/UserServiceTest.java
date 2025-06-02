package com.team4.monew.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.team4.monew.dto.user.UserDto;
import com.team4.monew.dto.user.UserRegisterRequest;
import com.team4.monew.entity.User;
import com.team4.monew.exception.ErrorCode;
import com.team4.monew.exception.user.UserAlreadyExistException;
import com.team4.monew.mapper.UserMapper;
import com.team4.monew.repository.UserRepository;
import com.team4.monew.service.basic.BasicUserService;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

  @InjectMocks
  private BasicUserService userService;

  @Mock
  private UserRepository userRepository;

  @Mock
  private UserMapper userMapper;

  private UUID userId;
  private String nickname;
  private String email;
  private String password;
  private User user;
  private UserDto userDto;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    email = "test@example.com";
    nickname = "testUser";
    password = "password123";

    user = User.create(email, nickname, password);
    ReflectionTestUtils.setField(user, "id", userId);
    userDto = new UserDto(userId, email, nickname, Instant.now());
  }

  @Test
  @DisplayName("사용자 등록_성공")
  void register_Success_ShouldReturnRegisteredUser() {
    // given
    UserRegisterRequest request = new UserRegisterRequest(email, nickname, password);

    given(userRepository.save(any(User.class))).willReturn(user);
    given(userMapper.toDto(user)).willReturn(userDto);

    // when
    UserDto result = userService.register(request);

    // then
    assertThat(result.email()).isEqualTo(request.email());
    assertThat(result.nickname()).isEqualTo(request.nickname());

    then(userRepository).should().save(any(User.class));
    then(userMapper).should().toDto(user);
  }

  @Test
  @DisplayName("사용자 등록_실패_이메일이 중복되는 경우")
  void register_Failure_WhenEmailAlreadyExists() {
    // given
    UserRegisterRequest request = new UserRegisterRequest(email, nickname, password);

    given(userRepository.existsByEmail(request.email())).willReturn(Boolean.TRUE);

    // when & then
    UserAlreadyExistException exception = assertThrows(UserAlreadyExistException.class,
        () -> userService.register(request));

    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_USER);
    assertThat(exception.getDetails().get("email")).isEqualTo(request.email());

    then(userRepository).should().existsByEmail(request.email());
    then(userRepository).should(never()).save(any(User.class));
  }


}
