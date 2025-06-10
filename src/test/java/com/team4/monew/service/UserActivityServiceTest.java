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
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@ExtendWith(MockitoExtension.class)
public class UserActivityServiceTest {

  @MockitoBean
  private UserActivityMapper userActivityMapper;

  @MockitoBean
  private UserMapper userMapper;

  @MockitoBean
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
        Instant.now(),
        List.of()
    );

    UserDto userDto = new UserDto(
        userId,
        "test@test.com",
        "test",
        Instant.now()
    );

    UserActivity userActivity = new UserActivity(userDto);

    UserActivityDto userActivityDto = new UserActivityDto(
        userId,
        "test@test.com",
        "test",
        Instant.now(),
        List.of(),
        List.of(),
        List.of(),
        List.of()
    );

    given(userActivityRepository.save(any(UserActivity.class))).willReturn(userActivity);
    given(userActivityMapper.toDto(userActivity)).willReturn(userActivityDto);
    given(userMapper.toDto(user)).willReturn(userDto);

    // when
    UserActivityDto result = userActivityService.create(user);

    // then
    assertThat(result.id()).isEqualTo(userActivityDto.id());
    assertThat(result.email()).isEqualTo(userActivityDto.email());
  }

}
