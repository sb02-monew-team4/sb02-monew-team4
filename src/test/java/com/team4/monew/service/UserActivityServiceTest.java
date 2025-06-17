package com.team4.monew.service;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.team4.monew.dto.UserActivity.UserActivityDto;
import com.team4.monew.dto.user.UserDto;
import com.team4.monew.entity.User;
import com.team4.monew.entity.UserActivity;
import com.team4.monew.mapper.UserActivityMapper;
import com.team4.monew.mapper.UserMapper;
import com.team4.monew.repository.UserActivityRepository;
import com.team4.monew.service.basic.BasicUserActivityService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UserActivityServiceTest {

  @Mock
  private UserActivityMapper userActivityMapper;

  @Mock
  private UserMapper userMapper;

  @Mock
  private UserActivityRepository userActivityRepository;

  @InjectMocks
  private BasicUserActivityService userActivityService;

  @DisplayName("UserActivity Create - Success")
  @Test
  void userActivityCreate_Success() {
    // given
    UUID userId = UUID.randomUUID();

    User user = new User(
        userId,
        "test@test.com",
        "test",
        "test",
        false,
        LocalDateTime.now()
    );

    UserDto userDto = new UserDto(
        userId,
        "test@test.com",
        "test",
        LocalDateTime.now()
    );

    UserActivity userActivity = new UserActivity(userDto);

    UserActivityDto userActivityDto = new UserActivityDto(
        userId,
        "test@test.com",
        "test",
        LocalDateTime.now(),
        List.of(),
        List.of(),
        List.of(),
        List.of()
    );

    given(userActivityRepository.save(any(UserActivity.class))).willReturn(userActivity);
    given(userActivityMapper.toDto(any(UserActivity.class))).willReturn(userActivityDto);
    given(userMapper.toDto(any(User.class))).willReturn(userDto);

    // when
    UserActivityDto result = userActivityService.create(user);

    // then
    assertThat(result.id()).isEqualTo(userActivityDto.id());
    assertThat(result.email()).isEqualTo(userActivityDto.email());
  }

}
