package com.team4.monew.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team4.monew.config.WebConfig;
import com.team4.monew.dto.user.UserDto;
import com.team4.monew.dto.user.UserLoginRequest;
import com.team4.monew.exception.user.InvalidCredentialsException;
import com.team4.monew.interceptor.AuthInterceptor;
import com.team4.monew.service.AuthService;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AuthController.class,
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {AuthInterceptor.class, WebConfig.class})
    }
)
public class AuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private AuthService authService;

  @Test
  @DisplayName("로그인_성공")
  void login_Success_ShouldReturnUser() throws Exception {
    // given
    UUID userId = UUID.randomUUID();

    UserLoginRequest request = new UserLoginRequest(
        "test@example.com", "password123"
    );

    UserDto userDto = new UserDto(userId, "test@example.com", "testUser", LocalDateTime.now());

    given(authService.login(request)).willReturn(userDto);

    // when & then
    mockMvc.perform(post("/api/users/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(userDto.id().toString()))
        .andExpect(jsonPath("$.email").value("test@example.com"));
  }

  @Test
  @DisplayName("로그인_실패_비밀번호가 잘못된 경우")
  void login_Failure_WhenWrongPassword() throws Exception {
    // given
    UserLoginRequest invalidRequest = new UserLoginRequest(
        "test@example.com", "wrongPassword123"
    );

    willThrow(InvalidCredentialsException.wrongPassword())
        .given(authService).login(invalidRequest);

    // when & then
    mockMvc.perform(post("/api/users/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
        .andExpect(jsonPath("$.message").value("WRONG_PASSWORD"));
  }

}
