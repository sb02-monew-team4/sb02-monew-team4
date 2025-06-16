package com.team4.monew.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team4.monew.dto.interest.*;
import com.team4.monew.interceptor.AuthInterceptor;
import com.team4.monew.repository.UserRepository;
import com.team4.monew.service.basic.BasicInterestService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InterestController.class)
@Import(AuthInterceptor.class)
class InterestControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private BasicInterestService interestService;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private UserRepository userRepository;

  @MockitoBean
  private AuthInterceptor authInterceptor;

  @BeforeEach
  void setUp() throws Exception {
    given(authInterceptor.preHandle(any(HttpServletRequest.class), any(HttpServletResponse.class), any()))
        .willAnswer(invocation -> {
          HttpServletRequest request = invocation.getArgument(0);
          request.setAttribute("authenticatedUserId", UUID.fromString("64acb692-46c0-4650-a6d1-6f7ebfba61c0"));
          return true;
        });
  }

  private static final UUID USER_ID = UUID.randomUUID();
  private static final UUID INTEREST_ID = UUID.randomUUID();

  private static final String AUTH_ATTR = "authenticatedUserId";

  @Test
  @DisplayName("관심사 등록 성공")
  void registerInterest_success() throws Exception {
    UUID testUserId = UUID.fromString("64acb692-46c0-4650-a6d1-6f7ebfba61c0");
    given(userRepository.existsById(testUserId)).willReturn(true);

    InterestRegisterRequest request = new InterestRegisterRequest("관심사", List.of("키워드1", "키워드2"));

    InterestDto mockResponse = new InterestDto(
        UUID.randomUUID(),
        "관심사",
        List.of("키워드1", "키워드2"),
        0,
        false
    );

    given(interestService.register(eq(testUserId), any(InterestRegisterRequest.class))).willReturn(mockResponse);

    mockMvc.perform(post("/api/interests")
            .contentType(MediaType.APPLICATION_JSON)
            .header("MoNew-Request-User-ID", testUserId.toString())
            .requestAttr("authenticatedUserId", testUserId)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("관심사"));
  }

  @DisplayName("관심사 수정 성공")
  @Test
  void updateInterest_success() throws Exception {
    UUID interestId = UUID.randomUUID();

    List<String> keywords = List.of("키워드1", "키워드2");
    InterestUpdateRequest request = new InterestUpdateRequest(keywords);

    InterestDto updatedDto = new InterestDto(
        interestId,
        "키워드수정",
        keywords,
        42,
        true
    );

    given(interestService.update(eq(interestId), any(InterestUpdateRequest.class)))
        .willReturn(updatedDto);

    mockMvc.perform(patch("/api/interests/{id}", interestId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(interestId.toString()))
        .andExpect(jsonPath("$.name").value("키워드수정"))
        .andExpect(jsonPath("$.keywords[0]").value("키워드1"))
        .andExpect(jsonPath("$.keywords[1]").value("키워드2"))
        .andExpect(jsonPath("$.subscribedByMe").value(true));
  }

  @DisplayName("관심사 목록 조회 성공")
  @Test
  void getInterests_success() throws Exception {
    UUID USER_ID = UUID.fromString("64acb692-46c0-4650-a6d1-6f7ebfba61c0");

    InterestDto interest = new InterestDto(
        UUID.randomUUID(),
        "테스트",
        List.of("키워드1", "키워드2"),
        42,
        true
    );

    CursorPageResponseInterestDto response = new CursorPageResponseInterestDto(
        List.of(interest),
        null,
        null,
        1,
        1L,
        false
    );

    given(interestService.getInterests(
        nullable(String.class),
        eq("name"),
        eq("ASC"),
        nullable(String.class),
        nullable(String.class),
        eq(10),
        any(UUID.class)
    )).willReturn(response);

    mockMvc.perform(get("/api/interests")
            .param("orderBy", "name")
            .param("direction", "ASC")
            .param("limit", "10")
            .param("keyword", "")
            .requestAttr("authenticatedUserId", USER_ID)
            .accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content[0].name").value("테스트"))
        .andExpect(jsonPath("$.content[0].keywords[0]").value("키워드1"))
        .andExpect(jsonPath("$.content[0].keywords[1]").value("키워드2"))
        .andExpect(jsonPath("$.content[0].subscribedByMe").value(true))
        .andExpect(jsonPath("$.nextCursor").doesNotExist())
        .andExpect(jsonPath("$.hasNext").value(false));
  }

  @Test
  @DisplayName("관심사 구독 성공")
  void subscribeInterest_success() throws Exception {
    UUID fixedUserId = UUID.fromString("64acb692-46c0-4650-a6d1-6f7ebfba61c0");

    SubscriptionDto response = new SubscriptionDto(
        UUID.randomUUID(),
        INTEREST_ID,
        "테스트 관심사",
        List.of("키워드1", "키워드2"),
        42,
        Instant.now()
    );

    Mockito.when(interestService.subscribeInterest(INTEREST_ID, fixedUserId))
        .thenReturn(response);

    mockMvc.perform(post("/api/interests/" + INTEREST_ID + "/subscriptions")
            .requestAttr(AUTH_ATTR, fixedUserId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.interestId").value(INTEREST_ID.toString()))
        .andExpect(jsonPath("$.interestName").value("테스트 관심사"))
        .andExpect(jsonPath("$.interestKeywords").isArray())
        .andExpect(jsonPath("$.interestSubscriberCount").value(42))
        .andExpect(jsonPath("$.createdAt").exists())
        .andExpect(jsonPath("$.id").exists());
  }

  @Test
  @DisplayName("관심사 구독 취소 성공")
  void unsubscribeInterest_success() throws Exception {
    UUID fixedUserId = UUID.fromString("64acb692-46c0-4650-a6d1-6f7ebfba61c0");

    mockMvc.perform(delete("/api/interests/" + INTEREST_ID + "/subscriptions")
            .requestAttr(AUTH_ATTR, fixedUserId))
        .andExpect(status().isOk());

    Mockito.verify(interestService).unsubscribeInterest(INTEREST_ID, fixedUserId);
  }

  @Test
  @DisplayName("관심사 삭제 성공")
  void deleteInterest_success() throws Exception {
    UUID fixedUserId = UUID.fromString("64acb692-46c0-4650-a6d1-6f7ebfba61c0");

    mockMvc.perform(delete("/api/interests/" + INTEREST_ID)
            .requestAttr(AUTH_ATTR, fixedUserId))
        .andExpect(status().isNoContent());

    Mockito.verify(interestService).hardDelete(INTEREST_ID, fixedUserId);
  }

}
