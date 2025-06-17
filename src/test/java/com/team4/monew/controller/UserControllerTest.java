package com.team4.monew.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team4.monew.auth.OwnerCheckAspect;
import com.team4.monew.config.TestAopConfig;
import com.team4.monew.dto.user.UserDto;
import com.team4.monew.dto.user.UserRegisterRequest;
import com.team4.monew.dto.user.UserUpdateRequest;
import com.team4.monew.entity.User;
import com.team4.monew.exception.user.UserNotFoundException;
import com.team4.monew.repository.UserRepository;
import com.team4.monew.service.UserService;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@Import({ OwnerCheckAspect.class, TestAopConfig.class })
public class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private UserService userService;

  @MockitoBean
  private UserRepository userRepository;

  @Test
  @DisplayName("사용자 등록_성공")
  void register_Success_ShouldReturnRegisteredUser() throws Exception {
    // given
    UUID userId = UUID.randomUUID();

    UserRegisterRequest request = new UserRegisterRequest(
        "test@example.com", "testUser", "password123"
    );

    UserDto userDto = new UserDto(userId, "test@example.com", "testUser", LocalDateTime.now());

    given(userService.register(request)).willReturn(userDto);

    // when & then
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

    // when & then
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
        LocalDateTime.now()
    );

    // Interceptor에서 인증
    given(userRepository.existsById(userId)).willReturn(true);
    given(userService.update(userId, request)).willReturn(updatedUserDto);

    // when & then
    mockMvc.perform(patch("/api/users/{userId}", userId)
            .header("MoNew-Request-User-ID", userId.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(updatedUserDto.id().toString()))
        .andExpect(jsonPath("$.email").value("test@example.com"))
        .andExpect(jsonPath("$.nickname").value("newTestUser"));
  }

  @Test
  @DisplayName("사용자 정보 수정_인가_실패_경로 변수와 사용자 ID가 일치하지 않는 경우")
  void update_Auth_Failure_WhenUserIdMismatch() throws Exception {
    // given
    UUID pathUserId = UUID.randomUUID();
    UUID headerUserId = UUID.randomUUID();

    // Interceptor에서 인증
    given(userRepository.existsById(headerUserId)).willReturn(true);

    UserUpdateRequest request = new UserUpdateRequest("newTestUser");

    // when & then
    mockMvc.perform(patch("/api/users/{userId}", pathUserId)
            .header("MoNew-Request-User-ID", headerUserId.toString()) // 잘못된 사용자 ID
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("FORBIDDEN"))
        .andExpect(jsonPath("$.message").value("UNAUTHORIZED_ACCESS"));
  }

  @Test
  @DisplayName("사용자 정보 수정_실패_닉네임이 빈 값인 경우")
  void update_Failure_WhenNicknameBlank() throws Exception {
    // given
    UUID userId = UUID.randomUUID();
    UserUpdateRequest invalidRequest = new UserUpdateRequest(" ");

    // Interceptor에서 인증
    given(userRepository.existsById(userId)).willReturn(true);

    // when & then
    mockMvc.perform(patch("/api/users/{userId}", userId)
            .header("MoNew-Request-User-ID", userId.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
        .andExpect(jsonPath("$.message").exists())
        .andExpect(jsonPath("$.details.nickname").value("닉네임은 필수입니다"));
  }

  @Test
  @DisplayName("사용자 논리 삭제_성공")
  void soft_Delete_Success() throws Exception {
    // given
    UUID userId = UUID.randomUUID();

    //Interceptor에서 인증
    given(userRepository.existsById(userId)).willReturn(true);

    User softDeleted = User.create("test@example.com", "testUser", "password123");

    given(userService.softDelete(userId)).willReturn(softDeleted);

    // when & then
    mockMvc.perform(delete("/api/users/{userId}", userId)
            .header("MoNew-Request-User-ID", userId.toString())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("사용자 논리 삭제_인가_실패_경로 변수와 사용자 ID가 일치하지 않는 경우")
  void soft_Delete_Auth_Failure_WhenUserIdMismatch() throws Exception {
    // given
    UUID pathUserId = UUID.randomUUID();
    UUID headerUserId = UUID.randomUUID();

    // Interceptor에서 인증
    given(userRepository.existsById(headerUserId)).willReturn(true);

    // when & then
    mockMvc.perform(delete("/api/users/{userId}", pathUserId)
            .header("MoNew-Request-User-ID", headerUserId.toString()) // 잘못된 사용자 ID
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("FORBIDDEN"))
        .andExpect(jsonPath("$.message").value("UNAUTHORIZED_ACCESS"));
  }

  @Test
  @DisplayName("사용자 논리 삭제_인증_실패_인터셉터에서 사용자를 찾을 수 없는 경우")
  void soft_Delete_Auth_Failure_WhenUserNotFoundInInterceptor() throws Exception {
    // given
    UUID nonExistentUserId = UUID.randomUUID();

    //Interceptor에서 인증
    given(userRepository.existsById(nonExistentUserId)).willReturn(false);

    willThrow(UserNotFoundException.byId(nonExistentUserId))
        .given(userService).softDelete(nonExistentUserId);

    // when & then
    mockMvc.perform(delete("/api/users/{userId}", nonExistentUserId)
            .header("MoNew-Request-User-ID", nonExistentUserId.toString())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
        .andExpect(jsonPath("$.message").value("Invalid user ID"))
        .andExpect(jsonPath("$.details.userId").value(nonExistentUserId.toString()));
  }

  @Test
  @DisplayName("사용자 논리 삭제_실패_서비스에서 사용자를 찾을 수 없는 경우")
  void soft_Delete_Failure_WhenUserNotFoundInService_AfterAuthSuccess() throws Exception {
    // given
    UUID nonExistentUserId = UUID.randomUUID();

    //Interceptor에서 인증
    given(userRepository.existsById(nonExistentUserId))
        .willReturn(true)
        .willReturn(false);

    willThrow(UserNotFoundException.byId(nonExistentUserId))
        .given(userService).softDelete(nonExistentUserId);

    // when & then
    mockMvc.perform(delete("/api/users/{userId}", nonExistentUserId)
            .header("MoNew-Request-User-ID", nonExistentUserId.toString())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("NOT_FOUND"))
        .andExpect(jsonPath("$.message").value("USER_NOT_FOUND"))
        .andExpect(jsonPath("$.details.userId").value(nonExistentUserId.toString()));
  }

  @Test
  @DisplayName("사용자 물리 삭제_성공")
  void hard_Delete_Success() throws Exception {
    // given
    UUID userId = UUID.randomUUID();

    //Interceptor에서 인증
    given(userRepository.existsById(userId)).willReturn(true);

    willDoNothing().given(userService).hardDelete(userId);

    // when & then
    mockMvc.perform(delete("/api/users/{userId}/hard", userId)
            .header("MoNew-Request-User-ID", userId.toString())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("사용자 물리 삭제_인가_실패_경로 변수와 사용자 ID가 일치하지 않는 경우")
  void hard_Delete_Auth_Failure_WhenUserIdMismatch() throws Exception {
    // given
    UUID pathUserId = UUID.randomUUID();
    UUID headerUserId = UUID.randomUUID();

    //Interceptor에서 인증
    given(userRepository.existsById(headerUserId)).willReturn(true);

    // when & then
    mockMvc.perform(delete("/api/users/{userId}/hard", pathUserId)
            .header("MoNew-Request-User-ID", headerUserId.toString()) // 잘못된 사용자 ID
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("FORBIDDEN"))
        .andExpect(jsonPath("$.message").value("UNAUTHORIZED_ACCESS"));
  }

  @Test
  @DisplayName("사용자 물리 삭제_인증_실패_인터셉터에서 사용자를 찾을 수 없는 경우")
  void hard_Delete_Auth_Failure_WhenUserNotFoundInInterceptor() throws Exception {
    // given
    UUID nonExistentUserId = UUID.randomUUID();

    //Interceptor에서 인증
    given(userRepository.existsById(nonExistentUserId)).willReturn(false);

    willThrow(UserNotFoundException.byId(nonExistentUserId))
        .given(userService).hardDelete(nonExistentUserId);

    // when & then
    mockMvc.perform(delete("/api/users/{userId}/hard", nonExistentUserId)
            .header("MoNew-Request-User-ID", nonExistentUserId.toString())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
        .andExpect(jsonPath("$.message").value("Invalid user ID"))
        .andExpect(jsonPath("$.details.userId").value(nonExistentUserId.toString()));
  }

  @Test
  @DisplayName("사용자 물리 삭제_실패_서비스에서 사용자를 찾을 수 없는 경우")
  void hard_Delete_Failure_WhenUserNotFoundInService_AfterAuthSuccess() throws Exception {
    // given
    UUID nonExistentUserId = UUID.randomUUID();

    //Interceptor에서 인증
    given(userRepository.existsById(nonExistentUserId))
        .willReturn(true)
        .willReturn(false);

    willThrow(UserNotFoundException.byId(nonExistentUserId))
        .given(userService).hardDelete(nonExistentUserId);

    // when & then
    mockMvc.perform(delete("/api/users/{userId}/hard", nonExistentUserId)
            .header("MoNew-Request-User-ID", nonExistentUserId.toString())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("NOT_FOUND"))
        .andExpect(jsonPath("$.message").value("USER_NOT_FOUND"))
        .andExpect(jsonPath("$.details.userId").value(nonExistentUserId.toString()));
  }


}
