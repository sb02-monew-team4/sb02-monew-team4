package com.team4.monew.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team4.monew.dto.user.UserDto;
import com.team4.monew.dto.user.UserRegisterRequest;
import com.team4.monew.dto.user.UserUpdateRequest;
import com.team4.monew.service.UserService;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
public class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private UserService userService;

  @Test
  @DisplayName("사용자 등록_성공")
  void register_Success_ShouldReturnRegisteredUser() throws Exception {
    // given
    UUID userId = UUID.randomUUID();

    UserRegisterRequest request = new UserRegisterRequest(
        "test@example.com", "testUser", "password123"
    );

    UserDto userDto = new UserDto(userId, "test@example.com", "testUser", Instant.now());

    given(userService.register(request)).willReturn(userDto);

    // when, then
    mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(userDto.id().toString()))
        .andExpect(jsonPath("$.email").value("test@example.com"))
        .andExpect(jsonPath("$.nickname").value("testUser"));
  }

  @Test
  @DisplayName("사용자 등록_실패_비밀번호 길이가 6자 미만인 경우")
  void register_Failure_WhenPasswordShort() throws Exception {
    // given
    UserRegisterRequest invalidRequest = new UserRegisterRequest(
        "test@example.com", "testUser", "passw"
    );

    // when, then
    mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
        .andExpect(jsonPath("$.message").exists())
        .andExpect(jsonPath("$.details.password").value("비밀번호는 6자 이상 20자 이하여야 합니다"));
  }

  @Test
  @DisplayName("사용자 정보 수정_성공")
  void update_Success_ShouldReturnUpdatedUser() throws Exception {
    // given
    UUID userId = UUID.randomUUID();

    UserUpdateRequest request = new UserUpdateRequest("newTestUser");

    UserDto updatedUserDto = new UserDto(
        userId,
        "test@example.com",
        "newTestUser",
        Instant.now()
    );

    given(userService.update(userId, request)).willReturn(updatedUserDto);

    // when, then
    mockMvc.perform(patch("/api/users/{userId}", userId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(updatedUserDto.id().toString()))
        .andExpect(jsonPath("$.email").value("test@example.com"))
        .andExpect(jsonPath("$.nickname").value("newTestUser"));
  }

  @Test
  @DisplayName("사용자 정보 수정_실패_닉네임이 빈 값인 경우")
  void update_Success_WhenNicknameBlank() throws Exception {
    // given
    UUID userId = UUID.randomUUID();
    UserUpdateRequest invalidRequest = new UserUpdateRequest(" ");

    // when, then
    mockMvc.perform(patch("/api/users/{userId}", userId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
        .andExpect(jsonPath("$.message").exists())
        .andExpect(jsonPath("$.details.nickname").value("닉네임은 필수입니다"));
  }


}
