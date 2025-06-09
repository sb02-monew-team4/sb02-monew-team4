package com.team4.monew.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.team4.monew.config.WebConfig;
import com.team4.monew.dto.notifications.CursorPageResponseNotificationDto;
import com.team4.monew.dto.notifications.NotificationDto;
import com.team4.monew.entity.Notification;
import com.team4.monew.entity.ResourceType;
import com.team4.monew.entity.User;
import com.team4.monew.exception.notification.NotificationNotFoundException;
import com.team4.monew.interceptor.AuthInterceptor;
import com.team4.monew.service.NotificationService;
import java.time.Instant;
import java.util.List;
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

@WebMvcTest(controllers = NotificationController.class,
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {AuthInterceptor.class, WebConfig.class})
    }
)
public class NotificationControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private NotificationService notificationService;

  @Test
  @DisplayName("알림 목록 조회_성공")
  void findUnconfirmedByCursor_Success() throws Exception {
    // given
    UUID userId = UUID.randomUUID();
    Instant now = Instant.now();
    String cursor = now.minusSeconds(300).toString();
    Instant after = now.minusSeconds(300);
    int limit = 2;

    NotificationDto dto1 = new NotificationDto(
        UUID.randomUUID(), now.minusSeconds(200), now.minusSeconds(190),
        false, userId, "알림 1", ResourceType.COMMENT, UUID.randomUUID()
    );
    NotificationDto dto2 = new NotificationDto(
        UUID.randomUUID(), now.minusSeconds(100), now.minusSeconds(90),
        false, userId, "알림 2", ResourceType.COMMENT, UUID.randomUUID()
    );
    NotificationDto dto3 = new NotificationDto(
        UUID.randomUUID(), now.minusSeconds(5000), now.minusSeconds(70),
        false, userId, "알림 3", ResourceType.COMMENT, UUID.randomUUID()
    );

    CursorPageResponseNotificationDto responseDto = new CursorPageResponseNotificationDto(
        List.of(dto1, dto2),
        dto2.createdAt().toString(),
        dto2.createdAt(),
        limit,
        10L,
        true
    );

    given(notificationService.findUnconfirmedByCursor(cursor, after, limit, userId))
        .willReturn(responseDto);

    // when & then
    mockMvc.perform(get("/api/notifications")
            .param("cursor", cursor)
            .param("after", after.toString())
            .param("limit", String.valueOf(limit))
            .header("MoNew-Request-User-ID", userId.toString())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.nextCursor").value(dto2.createdAt().toString()))
        .andExpect(jsonPath("$.nextAfter").value(dto2.createdAt().toString()))
        .andExpect(jsonPath("$.size").value(limit))
        .andExpect(jsonPath("$.totalElements").value(10))
        .andExpect(jsonPath("$.hasNext").value(true));
  }

  @Test
  @DisplayName("전체 알림 확인_성공")
  void updateAll_Success() throws Exception {
    // given
    UUID userId = UUID.randomUUID();

    Notification updatedNotification1 = Notification.create(mock(User.class), "알림메시지1", UUID.randomUUID(), ResourceType.COMMENT, false);
    Notification updatedNotification2 = Notification.create(mock(User.class), "알림메시지2", UUID.randomUUID(), ResourceType.COMMENT, false);

    given(notificationService.updateAll(userId)).willReturn(List.of(updatedNotification1, updatedNotification2));

    // when & then
    mockMvc.perform(patch("/api/notifications")
            .header("MoNew-Request-User-ID", userId.toString())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("전체 알림 확인_실패_알림이 존재하지 않는 경우")
  void updateAll_Failure_WhenNotificationsNotFound() throws Exception {
    // given
    UUID userId = UUID.randomUUID();

    willThrow(NotificationNotFoundException.byUserId(userId))
        .given(notificationService).updateAll(userId);

    // when & then
    mockMvc.perform(patch("/api/notifications")
            .header("MoNew-Request-User-ID", userId.toString())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("NOT_FOUND"))
        .andExpect(jsonPath("$.message").value("NOTIFICATION_NOT_FOUND"))
        .andExpect(jsonPath("$.details.userId").value(userId.toString()));
  }

  @Test
  @DisplayName("알림 확인_성공")
  void update_Success() throws Exception {
    // given
    UUID userId = UUID.randomUUID();
    UUID notificationId = UUID.randomUUID();

    Notification updatedNotification = Notification.create(mock(User.class), "알림메시지", UUID.randomUUID(), ResourceType.COMMENT, false);

    given(notificationService.update(notificationId, userId)).willReturn(updatedNotification);

    // when & then
    mockMvc.perform(patch("/api/notifications/{notificationId}", notificationId)
            .header("MoNew-Request-User-ID", userId.toString())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("알림 확인_실패_해당 알림의 사용자 Id와 userId가 일치하지 않는 경우")
  void update_Failure_WhenNotificationDoesNotBelongToUser() throws Exception {
    // given
    UUID wrongUserId = UUID.randomUUID();
    UUID notificationId = UUID.randomUUID();

    willThrow(NotificationNotFoundException.byId(notificationId))
        .given(notificationService).update(notificationId, wrongUserId);

    // when & then
    mockMvc.perform(patch("/api/notifications/{notificationId}", notificationId)
            .header("MoNew-Request-User-ID", wrongUserId.toString())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("NOT_FOUND"))
        .andExpect(jsonPath("$.message").value("NOTIFICATION_NOT_FOUND"))
        .andExpect(jsonPath("$.details.notificationId").value(notificationId.toString()));
  }


}
