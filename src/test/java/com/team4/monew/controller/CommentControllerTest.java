package com.team4.monew.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team4.monew.config.WebConfig;
import com.team4.monew.dto.comment.CommentDto;
import com.team4.monew.dto.comment.CommentLikeDto;
import com.team4.monew.dto.comment.CommentRegisterRequest;
import com.team4.monew.dto.comment.CommentUpdateRequest;
import com.team4.monew.dto.comment.CursorPageResponseCommentDto;
import com.team4.monew.exception.ErrorCode;
import com.team4.monew.exception.MonewException;
import com.team4.monew.interceptor.AuthInterceptor;
import com.team4.monew.service.basic.BasicCommentService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = CommentController.class,
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {AuthInterceptor.class,
            WebConfig.class})
    }
)
class CommentControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private BasicCommentService commentService;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  @DisplayName("댓글 등록")
  void registerComment() throws Exception {
    UUID authenticatedUserId = UUID.randomUUID();
    UUID articleId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    String content = "내용";

    CommentRegisterRequest request = new CommentRegisterRequest(articleId, userId, content);
    CommentDto response = new CommentDto(
        UUID.randomUUID(),
        articleId,
        userId,
        "닉네임",
        content,
        0,
        false,
        LocalDateTime.now()
    );

    Mockito.when(commentService.register(any(UUID.class), any(CommentRegisterRequest.class)))
        .thenReturn(response);

    mockMvc.perform(post("/api/comments")
            .requestAttr("authenticatedUserId", authenticatedUserId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.content").value("내용"));
  }

  @Test
  @DisplayName("댓글 등록 실패 - 유효하지 않은 입력")
  void registerComment_invalidInput() throws Exception {
    CommentRegisterRequest request = new CommentRegisterRequest(
        UUID.randomUUID(),
        UUID.randomUUID(),
        ""
    );

    mockMvc.perform(post("/api/comments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("댓글 목록 조회 - 정상 응답")
  void getComments() throws Exception {
    UUID articleId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    UUID userId = UUID.fromString("22222222-2222-2222-2222-222222222222");
    UUID commentId = UUID.fromString("33333333-3333-3333-3333-333333333333");

    CommentDto comment = new CommentDto(
        commentId,
        articleId,
        userId,
        "닉네임",
        "테스트 댓글입니다",
        5,
        true,
        LocalDateTime.now()
    );

    CursorPageResponseCommentDto response = new CursorPageResponseCommentDto(
        List.of(comment),
        "nextCursorSample",
        "nextAfterSample",
        1,
        1L,
        false
    );

    Mockito.when(commentService.getCommentsByArticleWithCursor(
        eq(articleId),
        eq("createdAt"),
        eq("desc"),
        isNull(),
        isNull(),
        eq(10),
        eq(userId)
    )).thenReturn(response);

    mockMvc.perform(get("/api/comments")
            .param("articleId", articleId.toString())
            .param("orderBy", "createdAt")
            .param("direction", "desc")
            .param("limit", "10")
            .header("Monew-Request-User-ID", userId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].content").value("테스트 댓글입니다"))
        .andExpect(jsonPath("$.content[0].likeCount").value(5));
  }

  @Test
  @DisplayName("댓글 목록 조회 실패 - 유효하지 않은 articleId")
  void debugException() throws Exception {
    UUID userId = UUID.randomUUID();

    mockMvc.perform(get("/api/comments")
            .param("articleId", "not-a-uuid")
            .param("orderBy", "createdAt")
            .param("direction", "desc")
            .param("limit", "10")
            .header("Monew-Request-User-ID", userId))
        .andDo(result -> {
          System.out.println("응답 상태: " + result.getResponse().getStatus());
          System.out.println("응답 본문: " + result.getResponse().getContentAsString());
        });
  }

  @Test
  @DisplayName("댓글 좋아요")
  void likeComment() throws Exception {
    UUID commentId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    CommentLikeDto dto = new CommentLikeDto(
        UUID.randomUUID(),
        userId,
        LocalDateTime.now(),
        commentId,
        UUID.randomUUID(),
        UUID.randomUUID(),
        "nickname",
        "comment content",
        1,
        LocalDateTime.now()
    );

    Mockito.when(commentService.likeComment(eq(commentId), eq(userId)))
        .thenReturn(dto);

    mockMvc.perform(post("/api/comments/" + commentId + "/comment-likes")
            .header("Monew-Request-User-ID", userId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.commentContent").value("comment content"));
  }

  @Test
  @DisplayName("댓글 좋아요 실패 - 존재하지 않는 댓글")
  void likeComment_notFound() throws Exception {
    UUID commentId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    Mockito.when(commentService.likeComment(commentId, userId))
        .thenThrow(new MonewException(ErrorCode.COMMENT_NOT_FOUND));

    mockMvc.perform(post("/api/comments/" + commentId + "/comment-likes")
            .header("Monew-Request-User-ID", userId))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("댓글 좋아요 취소")
  void unlikeComment() throws Exception {
    UUID commentId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    Mockito.doNothing().when(commentService).unlikeComment(commentId, userId);

    mockMvc.perform(delete("/api/comments/" + commentId + "/comment-likes")
            .header("Monew-Request-User-ID", userId))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("댓글 좋아요 취소 실패 - 좋아요 안 된 상태")
  void unlikeComment_invalidState() throws Exception {
    UUID commentId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    Mockito.doThrow(new MonewException(ErrorCode.COMMENT_LIKE_NOT_ALLOWED))
        .when(commentService).unlikeComment(commentId, userId);

    mockMvc.perform(delete("/api/comments/" + commentId + "/comment-likes")
            .header("Monew-Request-User-ID", userId))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("댓글 수정")
  void updateComment() throws Exception {
    UUID commentId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    UUID articleId = UUID.randomUUID();

    CommentUpdateRequest request = new CommentUpdateRequest("수정된 내용");
    CommentDto updated = new CommentDto(
        commentId,
        articleId,
        userId,
        "닉네임",
        request.content(),
        0,
        false,
        LocalDateTime.now()
    );

    Mockito.when(commentService.update(eq(commentId), eq(userId), any()))
        .thenReturn(updated);

    mockMvc.perform(patch("/api/comments/" + commentId)
            .header("Monew-Request-User-ID", userId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").value("수정된 내용"));
  }

  @Test
  @DisplayName("댓글 수정 실패 - 권한 없음")
  void updateComment_forbidden() throws Exception {
    UUID commentId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    CommentUpdateRequest request = new CommentUpdateRequest("수정");

    Mockito.when(commentService.update(eq(commentId), eq(userId), any()))
        .thenThrow(new MonewException(ErrorCode.COMMENT_FORBIDDEN));

    mockMvc.perform(patch("/api/comments/" + commentId)
            .header("Monew-Request-User-ID", userId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("댓글 소프트 삭제")
  void softDeleteComment() throws Exception {
    UUID commentId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    Mockito.doNothing().when(commentService).softDelete(commentId, userId);

    mockMvc.perform(delete("/api/comments/" + commentId)
            .header("Monew-Request-User-ID", userId))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("댓글 소프트 삭제 실패 - 권한 없음")
  void softDeleteComment_forbidden() throws Exception {
    UUID commentId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    Mockito.doThrow(new MonewException(ErrorCode.COMMENT_FORBIDDEN))
        .when(commentService).softDelete(commentId, userId);

    mockMvc.perform(delete("/api/comments/" + commentId)
            .header("Monew-Request-User-ID", userId))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("댓글 하드 삭제")
  void hardDeleteComment() throws Exception {
    UUID commentId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    Mockito.doNothing().when(commentService).hardDelete(commentId, userId);

    mockMvc.perform(delete("/api/comments/" + commentId + "/hard")
            .header("Monew-Request-User-ID", userId))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("댓글 하드 삭제 실패 - 존재하지 않음")
  void hardDeleteComment_notFound() throws Exception {
    UUID commentId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    Mockito.doThrow(new MonewException(ErrorCode.COMMENT_NOT_FOUND))
        .when(commentService).hardDelete(commentId, userId);

    mockMvc.perform(delete("/api/comments/" + commentId + "/hard")
            .header("Monew-Request-User-ID", userId))
        .andExpect(status().isNotFound());
  }
}
