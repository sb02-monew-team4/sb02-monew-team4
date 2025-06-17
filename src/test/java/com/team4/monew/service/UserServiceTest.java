package com.team4.monew.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.team4.monew.dto.user.UserDto;
import com.team4.monew.dto.user.UserRegisterRequest;
import com.team4.monew.dto.user.UserUpdateRequest;
import com.team4.monew.entity.User;
import com.team4.monew.exception.ErrorCode;
import com.team4.monew.exception.user.UserAlreadyExistException;
import com.team4.monew.exception.user.UserNotFoundException;
import com.team4.monew.mapper.UserMapper;
import com.team4.monew.repository.UserRepository;
import com.team4.monew.service.basic.BasicUserService;
import java.time.LocalDateTime;
import java.util.Optional;
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
  private UserActivityService userActivityService;

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
    ReflectionTestUtils.setField(user, "createdAt", LocalDateTime.now());
    userDto = new UserDto(userId, email, nickname, LocalDateTime.now());
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

  @Test
  @DisplayName("사용자 정보 수정_성공")
  void update_Success_ShouldReturnUpdatedUser() {
    // given
    UserUpdateRequest request = new UserUpdateRequest("newNickname");

    given(userRepository.findById(userId)).willReturn(Optional.of(user));

    UserDto updatedUserDto = new UserDto(
        user.getId(),
        user.getEmail(),
        request.nickname(),
        user.getCreatedAt()
    );

    given(userMapper.toDto(user)).willReturn(updatedUserDto);

    // when
    UserDto result = userService.update(userId, request);

    // then
    assertThat(result.nickname()).isEqualTo(request.nickname());

    then(userRepository).should().findById(userId);
    then(userMapper).should().toDto(user);
  }

  @Test
  @DisplayName("사용자 정보 수정_실패_사용자가 존재하지 않는 경우")
  void update_Failure_WhenUserNotFound() {
    // given
    UserUpdateRequest request = new UserUpdateRequest("newNickname");

    given(userRepository.findById(userId)).willReturn(Optional.empty());

    // when & then
    UserNotFoundException exception = assertThrows(UserNotFoundException.class,
        () -> userService.update(userId, request));

    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
    assertThat(exception.getDetails().get("userId")).isEqualTo(userId);

    then(userRepository).should().findById(userId);
    then(userMapper).should(never()).toDto(any(User.class));
  }

  @Test
  @DisplayName("사용자 논리 삭제_성공")
  void soft_Delete_Success() {
    // given
    given(userRepository.findById(userId)).willReturn(Optional.of(user));

    // when
    User deletedUser = userService.softDelete(userId);

    // then
    assertThat(deletedUser.isDeleted()).isTrue();

    then(userRepository).should().findById(userId);
  }

  @Test
  @DisplayName("사용자 논리 삭제_실패_사용자가 존재하지 않는 경우")
  void soft_Delete_Failure_WhenUserNotFound() {
    // given
    given(userRepository.findById(userId)).willReturn(Optional.empty());

    // when & then
    UserNotFoundException exception = assertThrows(UserNotFoundException.class,
        () -> userService.softDelete(userId));

    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
    assertThat(exception.getDetails().get("userId")).isEqualTo(userId);

    then(userRepository).should().findById(userId);
  }

  @Test
  @DisplayName("사용자 물리 삭제_성공")
  void hard_Delete_Success() {
    // given
    given(userRepository.existsById(userId)).willReturn(true);

    // when
    userService.hardDelete(userId);

    // then
    then(userRepository).should().existsById(userId);
    then(userRepository).should().deleteById(userId);
  }

  @Test
  @DisplayName("사용자 물리 삭제_실패_사용자가 존재하지 않는 경우")
  void hard_Delete_Failure_WhenUserNotFound() {
    // given
    given(userRepository.existsById(userId)).willReturn(false);

    // when & then
    UserNotFoundException exception = assertThrows(UserNotFoundException.class,
        () -> userService.hardDelete(userId));

    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
    assertThat(exception.getDetails().get("userId")).isEqualTo(userId);

    then(userRepository).should().existsById(userId);
    then(userRepository).should(never()).deleteById(userId);
  }


}
