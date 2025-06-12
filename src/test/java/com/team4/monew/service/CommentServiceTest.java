package com.team4.monew.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.team4.monew.asynchronous.event.commentlike.CommentLikeCreatedEventForNotification;
import com.team4.monew.dto.comment.CommentDto;
import com.team4.monew.dto.comment.CommentLikeDto;
import com.team4.monew.dto.comment.CommentRegisterRequest;
import com.team4.monew.dto.comment.CommentUpdateRequest;
import com.team4.monew.dto.comment.CursorPageResponseCommentDto;
import com.team4.monew.entity.Article;
import com.team4.monew.entity.Comment;
import com.team4.monew.entity.CommentLike;
import com.team4.monew.entity.User;
import com.team4.monew.exception.ErrorCode;
import com.team4.monew.exception.MonewException;
import com.team4.monew.mapper.CommentLikeMapper;
import com.team4.monew.mapper.CommentMapper;
import com.team4.monew.repository.ArticleRepository;
import com.team4.monew.repository.CommentLikeRepository;
import com.team4.monew.repository.CommentRepository;
import com.team4.monew.repository.UserRepository;
import com.team4.monew.service.basic.BasicCommentService;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

  @Mock
  private CommentRepository commentRepository;

  @Mock
  private CommentLikeRepository commentLikeRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private ArticleRepository articleRepository;

  @Mock
  private CommentMapper commentMapper;

  @Mock
  private CommentLikeMapper commentLikeMapper;

  @Mock
  private ApplicationEventPublisher eventPublisher;

  @Captor
  private ArgumentCaptor<CommentLikeCreatedEventForNotification> eventCaptor;

  @InjectMocks
  private BasicCommentService basicCommentService;

  private UUID userId;
  private UUID articleId;
  private User user;
  private Article article;
  private Comment comment;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    articleId = UUID.randomUUID();

    user = User.create("test@test.com", "test", "123");
    ReflectionTestUtils.setField(user, "id", userId);
    ReflectionTestUtils.setField(user, "createdAt", Instant.now());

    article = new Article(
        articleId,
        "출처",
        "http://original.link",
        "뉴스 제목",
        Instant.now(),
        "뉴스 요약",
        0L,
        0L,
        false,
        Instant.now(),
        new HashSet<>()
    );
  }

  @Test
  @DisplayName("댓글 등록 성공")
  void registerComment() {
    String content = "댓글 내용";
    CommentRegisterRequest request = new CommentRegisterRequest(articleId, userId, content);

    comment = new Comment(user, article, content);
    ReflectionTestUtils.setField(comment, "id", UUID.randomUUID());

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));
    when(commentRepository.save(any(Comment.class))).thenReturn(comment);

    CommentDto result = basicCommentService.register(request);

    assertNotNull(result);
    assertEquals(content, result.content());
    assertEquals(userId, result.userId());
    assertEquals(articleId, result.articleId());
  }

  @Test
  @DisplayName("댓글 등록 실패 - 사용자 없음")
  void registerComment_fail_userNotFound() {
    CommentRegisterRequest request = new CommentRegisterRequest(articleId, userId, "댓글 내용");

    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    MonewException exception = assertThrows(MonewException.class, () -> {
      basicCommentService.register(request);
    });

    assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  @DisplayName("댓글 등록 실패 - 뉴스 없음")
  void registerComment_fail_articleNotFound() {
    String content = "댓글 내용";
    CommentRegisterRequest request = new CommentRegisterRequest(articleId, userId, content);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(articleRepository.findById(articleId)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class, () -> {
      basicCommentService.register(request);
    });
  }

  @Test
  @DisplayName("댓글 목록 조회 - createdAt")
  void testFindCommentsByCreatedAtAsc_FirstPage() {
    UUID articleId = UUID.randomUUID();
    String orderBy = "createdAt";
    String direction = "ASC";
    String cursor = null;
    String after = null;
    int limit = 3;
    UUID requesterId = UUID.randomUUID();

    User mockUser = mock(User.class);
    when(mockUser.getId()).thenReturn(UUID.randomUUID());
    when(mockUser.getNickname()).thenReturn("nickname");

    Article mockArticle = mock(Article.class);

    Comment comment1 = Comment.createWithCreatedAt(UUID.randomUUID(), mockUser, mockArticle, "댓글1", Instant.parse("2025-05-01T10:00:00Z"));
    Comment comment2 = Comment.createWithCreatedAt(UUID.randomUUID(), mockUser, mockArticle, "댓글2", Instant.parse("2025-05-01T10:05:00Z"));
    Comment comment3 = Comment.createWithCreatedAt(UUID.randomUUID(), mockUser, mockArticle, "댓글3", Instant.parse("2025-05-01T10:10:00Z"));

    List<Comment> mockComments = List.of(comment1, comment2, comment3);

    when(commentRepository.findCommentsByArticleWithCursorPaging(articleId, orderBy, direction, cursor, after, limit))
        .thenReturn(mockComments);

    when(commentRepository.countByArticleId(articleId)).thenReturn(10L);

    when(commentMapper.toDto(any(Comment.class), any(UUID.class)))
        .thenAnswer(invocation -> {
          Comment c = invocation.getArgument(0);
          return new CommentDto(
              c.getId(),
              articleId,
              c.getUser().getId(),
              c.getUser().getNickname(),
              c.getContent(),
              c.getLikeCount().intValue(),
              Boolean.TRUE.equals(c.getIsDeleted()),
              c.getCreatedAt()
          );
        });

    CursorPageResponseCommentDto result = basicCommentService.getCommentsByArticleWithCursor(
        articleId, orderBy, direction, cursor, after, limit, requesterId
    );

    assertEquals(3, result.content().size());
    assertEquals(comment3.getId().toString(), result.nextCursor());
    assertEquals(comment3.getCreatedAt().toString(), result.nextAfter());
    assertEquals(10L, result.totalElement());
    assertTrue(result.hasNext());
  }

  @Test
  @DisplayName("댓글 목록 조회 - likeCount")
  void testFindCommentsByLikeCountDesc_FirstPage() {

    String orderBy = "likeCount";
    String direction = "DESC";
    String cursor = null;
    String after = null;
    int limit = 3;
    UUID requesterId = UUID.randomUUID();

    User mockUser = mock(User.class);
    when(mockUser.getId()).thenReturn(UUID.randomUUID());
    when(mockUser.getNickname()).thenReturn("user1");

    Article mockArticle = mock(Article.class);
    UUID articleId = UUID.randomUUID();
    when(mockArticle.getId()).thenReturn(articleId);

    Comment comment1 = Comment.createWithLikeCount(UUID.randomUUID(), mockUser, mockArticle, "좋아요 많은 댓글", 10L, Instant.parse("2025-05-01T10:00:00Z"));
    Comment comment2 = Comment.createWithLikeCount(UUID.randomUUID(), mockUser, mockArticle, "중간 댓글", 5L, Instant.parse("2025-05-01T10:05:00Z"));
    Comment comment3 = Comment.createWithLikeCount(UUID.randomUUID(), mockUser, mockArticle, "좋아요 적은 댓글", 1L, Instant.parse("2025-05-01T10:10:00Z"));

    List<Comment> mockComments = List.of(comment1, comment2, comment3);

    when(commentRepository.findCommentsByArticleWithCursorPaging(eq(articleId), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit)))
        .thenReturn(mockComments);

    when(commentRepository.countByArticleId(articleId)).thenReturn(20L);

    when(commentMapper.toDto(any(Comment.class), eq(requesterId)))
        .thenAnswer(invocation -> {
          Comment c = invocation.getArgument(0);
          return new CommentDto(
              c.getId(),
              articleId,
              c.getUser().getId(),
              c.getUser().getNickname(),
              c.getContent(),
              c.getLikeCount() != null ? c.getLikeCount().intValue() : 0,
              Boolean.TRUE.equals(c.getIsDeleted()),
              c.getCreatedAt()
          );
        });

    CursorPageResponseCommentDto actualResponse =
        basicCommentService.getCommentsByArticleWithCursor(articleId, orderBy, direction, cursor, after, limit, requesterId);

    assertEquals(3, actualResponse.content().size());
    assertEquals(comment3.getId().toString(), actualResponse.nextCursor());
    assertEquals(comment3.getCreatedAt().toString(), actualResponse.nextAfter());
    assertEquals(20L, actualResponse.totalElement());
    assertTrue(actualResponse.hasNext());
  }

  @Test
  @DisplayName("댓글 목록 조회 실패 - 결과 없음")
  void testFindCommentsByLikeCount_EmptyResult() {
    String orderBy = "likeCount";
    String direction = "DESC";
    String cursor = null;
    String after = null;
    int limit = 3;
    UUID articleId = UUID.randomUUID();
    UUID requesterId = UUID.randomUUID();

    when(commentRepository.findCommentsByArticleWithCursorPaging(
        eq(articleId), eq(orderBy), eq(direction), eq(cursor), eq(after), eq(limit)))
        .thenReturn(List.of());

    when(commentRepository.countByArticleId(articleId)).thenReturn(0L);

    CursorPageResponseCommentDto actualResponse =
        basicCommentService.getCommentsByArticleWithCursor(articleId, orderBy, direction, cursor, after, limit, requesterId);

    assertTrue(actualResponse.content().isEmpty());
    assertEquals(0, actualResponse.size());
    assertEquals(0L, actualResponse.totalElement());
    assertFalse(actualResponse.hasNext());
  }


  @Test
  @DisplayName("댓글 좋아요 성공")
  void likeComment_success() {
    UUID commentId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    UUID expectedLikeId = UUID.fromString("54b3da15-a31e-41db-ad20-081033298b72");
    Instant now = Instant.now();

    User user = User.create("test@example.com", "tester", "password123");
    ReflectionTestUtils.setField(user, "id", userId);

    Comment comment = new Comment();
    ReflectionTestUtils.setField(comment, "id", commentId);
    ReflectionTestUtils.setField(comment, "likeCount", 0L);
    ReflectionTestUtils.setField(comment, "user", user);
    ReflectionTestUtils.setField(comment, "article", new Article());

    when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(commentLikeRepository.existsByCommentAndUser(comment, user)).thenReturn(false);

    when(commentLikeRepository.save(any(CommentLike.class)))
        .thenAnswer(invocation -> {
          CommentLike savedLike = invocation.getArgument(0);
          ReflectionTestUtils.setField(savedLike, "id", expectedLikeId);
          ReflectionTestUtils.setField(savedLike, "createdAt", now);
          return savedLike;
        });

    when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

    CommentLikeDto expectedDto = new CommentLikeDto(
        expectedLikeId,
        userId,
        now,
        commentId,
        articleId,
        userId,
        user.getNickname(),
        comment.getContent(),
        comment.getLikeCount().intValue(),
        comment.getCreatedAt()
    );
    when(commentLikeMapper.toDto(any(CommentLike.class))).thenReturn(expectedDto);

    CommentLikeDto result = basicCommentService.likeComment(commentId, userId);

    assertNotNull(result);
    assertEquals(expectedLikeId, result.id());
    assertEquals(commentId, result.commentId());
    assertEquals(userId, result.likedBy());
    assertEquals(1, comment.getLikeCount());

    // 이벤트 발행 검증
    verify(eventPublisher).publishEvent(eventCaptor.capture());

    CommentLikeCreatedEventForNotification event = eventCaptor.getValue();
    assertEquals(commentId, event.commentId());
    assertEquals(userId, event.likerId());
    assertEquals(user.getId(), event.commentOwnerId());
  }

  @Test
  @DisplayName("댓글 좋아요 실패 - 이미 좋아요 눌렀음")
  void likeComment_fail_alreadyLiked() {
    UUID commentId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    User user = User.create("test@example.com", "tester", "password123");
    ReflectionTestUtils.setField(user, "id", userId);

    Comment comment = new Comment(user, new Article(), "댓글 내용");
    ReflectionTestUtils.setField(comment, "id", commentId);

    when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(commentLikeRepository.existsByCommentAndUser(comment, user)).thenReturn(true);

    MonewException exception = assertThrows(MonewException.class, () -> {
      basicCommentService.likeComment(commentId, userId);
    });

    assertEquals(ErrorCode.COMMENT_ALREADY_LIKED, exception.getErrorCode());

    // 이벤트 발행하지 않음
    verify(eventPublisher, never()).publishEvent(eventCaptor.capture());
  }

  @Test
  @DisplayName("댓글 좋아요 실패 - 댓글 없음")
  void likeComment_fail_commentNotFound() {
    UUID commentId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

    MonewException exception = assertThrows(MonewException.class, () -> {
      basicCommentService.likeComment(commentId, userId);
    });

    assertEquals(ErrorCode.COMMENT_NOT_FOUND, exception.getErrorCode());

    // 이벤트 발행하지 않음
    verify(eventPublisher, never()).publishEvent(eventCaptor.capture());
  }

  @Test
  @DisplayName("댓글 좋아요 취소 성공")
  void unlikeComment_success() {
    UUID commentId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    User user = User.create("test@example.com", "tester", "password123");
    ReflectionTestUtils.setField(user, "id", userId);

    Article article = new Article();
    ReflectionTestUtils.setField(article, "id", UUID.randomUUID());

    Comment comment = new Comment(user, article, "댓글 내용");
    ReflectionTestUtils.setField(comment, "id", commentId);
    ReflectionTestUtils.setField(comment, "likeCount", 1L);

    CommentLike like = new CommentLike(UUID.randomUUID(), comment, user, Instant.now());

    when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(commentLikeRepository.findByCommentAndUser(comment, user)).thenReturn(Optional.of(like));
    doNothing().when(commentLikeRepository).delete(like);
    when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

    basicCommentService.unlikeComment(commentId, userId);

    assertEquals(0, comment.getLikeCount());
  }

  @Test
  @DisplayName("댓글 좋아요 취소 실패 - 좋아요 내역 없음")
  void unlikeComment_fail_noLike() {
    UUID commentId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    User user = User.create("test@example.com", "tester", "password123");
    ReflectionTestUtils.setField(user, "id", userId);

    Article article = new Article();
    ReflectionTestUtils.setField(article, "id", UUID.randomUUID());

    Comment comment = new Comment(user, article, "댓글 내용");
    ReflectionTestUtils.setField(comment, "id", commentId);

    when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(commentLikeRepository.findByCommentAndUser(comment, user)).thenReturn(Optional.empty());

    MonewException exception = assertThrows(MonewException.class, () -> {
      basicCommentService.unlikeComment(commentId, userId);
    });
    assertEquals(ErrorCode.COMMENT_LIKE_NOT_ALLOWED, exception.getErrorCode());
  }

  @Test
  @DisplayName("댓글 수정 성공 - 본인 댓글")
  void updateComment_success() {
    UUID commentId = UUID.randomUUID();
    String updatedContent = "수정된 댓글 내용";
    CommentUpdateRequest request = new CommentUpdateRequest(updatedContent);

    comment = new Comment(user, article, "기존 댓글 내용");
    ReflectionTestUtils.setField(comment, "id", commentId);

    when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
    when(commentRepository.save(any(Comment.class))).thenReturn(comment);

    CommentDto result = basicCommentService.update(commentId, userId, request);

    assertNotNull(result);
    assertEquals(updatedContent, result.content());
  }

  @Test
  @DisplayName("댓글 수정 실패 - 댓글이 존재하지 않음")
  void updateCommentFailsWhenCommentNotFound() {
    UUID commentId = UUID.randomUUID();
    String updatedContent = "수정된 댓글 내용";
    CommentUpdateRequest request = new CommentUpdateRequest(updatedContent);

    when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

    MonewException exception = assertThrows(MonewException.class, () -> {
      basicCommentService.update(commentId, userId, request);
    });

    assertEquals(ErrorCode.COMMENT_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  @DisplayName("댓글 수정 실패 - 작성자가 아님")
  void updateCommentFailsWhenUserIsNotAuthor() {
    UUID commentId = UUID.randomUUID();
    String updatedContent = "수정된 댓글 내용";
    CommentUpdateRequest request = new CommentUpdateRequest(updatedContent);

    UUID otherUserId = UUID.randomUUID();

    comment = new Comment(user, article, "기존 댓글 내용");
    ReflectionTestUtils.setField(comment, "id", commentId);

    when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

    MonewException exception = assertThrows(MonewException.class, () -> {
      basicCommentService.update(commentId, otherUserId, request);
    });

    assertEquals(ErrorCode.COMMENT_FORBIDDEN, exception.getErrorCode());
  }

  @Test
  @DisplayName("댓글 삭제 성공 - 본인 댓글")
  void deleteComment_success() {
    UUID commentId = UUID.randomUUID();

    comment = new Comment(user, article, "삭제할 댓글");
    ReflectionTestUtils.setField(comment, "id", commentId);
    ReflectionTestUtils.setField(comment, "isDeleted", false);

    when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
    when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

    basicCommentService.softDelete(commentId, userId);

    assertEquals(true, comment.getIsDeleted());
  }

  @Test
  @DisplayName("댓글 삭제 실패 - 댓글이 존재하지 않음")
  void deleteComment_fail_commentNotFound() {
    UUID commentId = UUID.randomUUID();

    when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

    MonewException exception = assertThrows(MonewException.class, () -> {
      basicCommentService.softDelete(commentId, userId);
    });

    assertEquals(ErrorCode.COMMENT_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  @DisplayName("댓글 삭제 실패 - 작성자가 아님")
  void deleteComment_fail_notAuthor() {
    UUID commentId = UUID.randomUUID();

    User author = User.create("author@example.com", "author", "password");
    UUID authorId = UUID.randomUUID();
    ReflectionTestUtils.setField(author, "id", authorId);

    Article article = new Article();
    ReflectionTestUtils.setField(article, "id", UUID.randomUUID());

    Comment comment = new Comment(author, article, "삭제할 댓글");
    ReflectionTestUtils.setField(comment, "id", commentId);

    UUID otherUserId = UUID.randomUUID();

    when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

    MonewException exception = assertThrows(MonewException.class, () -> {
      basicCommentService.softDelete(commentId, otherUserId);
    });

    assertEquals(ErrorCode.COMMENT_FORBIDDEN, exception.getErrorCode());
  }
}
