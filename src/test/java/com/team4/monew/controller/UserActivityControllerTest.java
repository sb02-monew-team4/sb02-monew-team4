package com.team4.monew.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.team4.monew.dto.UserActivity.UserActivityDto;
import com.team4.monew.service.basic.BasicUserActivityService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = UserActivityController.class)
public class UserActivityControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private BasicUserActivityService userActivityService;

  @DisplayName("활동 내역 조회 성공")
  @Test
  void user_Activity_Controller_Success() throws Exception {
    // given
    UUID userId = UUID.randomUUID();

    UserActivityDto dto = new UserActivityDto(
        userId,
        "test@test.com",
        "test",
        Instant.now(),
        List.of(),
        List.of(),
        List.of(),
        List.of()
    );

    given(userActivityService.getByUserId(userId)).willReturn(dto);

    // when
    // then
    mockMvc.perform(get("/api/user-activities/{userId}", userId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nickname").value("test"));
  }
}
