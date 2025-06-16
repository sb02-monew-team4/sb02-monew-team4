package com.team4.monew.service.basic;

import com.team4.monew.asynchronous.event.comment.CommentCreatedEvent;
import com.team4.monew.asynchronous.event.comment.CommentDeletedEvent;
import com.team4.monew.asynchronous.event.comment.CommentUpdatedEvent;
import com.team4.monew.asynchronous.event.commentlike.CommentLikeCreatedEvent;
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
import com.team4.monew.service.CommentService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicCommentService implements CommentService {

  private final UserRepository userRepository;
  private final ArticleRepository articleRepository;
  private final CommentRepository commentRepository;
  private final CommentLikeRepository commentLikeRepository;
  private final CommentMapper commentMapper;
  private final CommentLikeMapper commentLikeMapper;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  @Override
  public CommentDto register(UUID authenticatedUserId, CommentRegisterRequest request) {

    if (!authenticatedUserId.equals(request.userId())) {
      throw new MonewException(ErrorCode.UNAUTHORIZED_ACCESS);
    }

    User user = userRepository.findById(request.userId())
        .orElseThrow(() -> new MonewException(ErrorCode.USER_NOT_FOUND));

    Article article = articleRepository.findById(request.articleId())
        .orElseThrow(() -> new MonewException(ErrorCode.ARTICLE_NOT_FOUND));

    Comment comment = new Comment(user, article, request.content());
    Comment saved = commentRepository.save(comment);

    article.incrementCommentCount();

    eventPublisher.publishEvent(new CommentCreatedEvent(user.getId(), comment));

    return commentMapper.toDto(saved, request.userId());
  }

  @Override
  public CursorPageResponseCommentDto getCommentsByArticleWithCursor(
      UUID articleId,
      String orderBy,
      String direction,
      String cursor,
      String after,
      int limit,
      UUID requesterId
  ) {
    if (limit <= 0) {
      log.warn("잘못된 댓글 요청: limit={}", limit);
      throw new MonewException(ErrorCode.INVALID_LIMIT);
    }

    List<Comment> comments = commentRepository.findCommentsByArticleWithCursorPaging(
        articleId, orderBy, direction, cursor, after, limit
    );

    List<CommentDto> dtoList = comments.stream()
        .map(comment -> commentMapper.toDto(comment, requesterId))
        .toList();

    String nextCursor = null;
    String nextAfter = null;
    if (!comments.isEmpty()) {
      Comment last = comments.get(comments.size() - 1);
      nextCursor = last.getId().toString();
      nextAfter = last.getCreatedAt().toString();
    }

    Long total = commentRepository.countByArticleId(articleId);
    boolean hasNext = comments.size() == limit;

    return new CursorPageResponseCommentDto(
        dtoList,
        nextCursor,
        nextAfter,
        dtoList.size(),
        total,
        hasNext
    );
  }

  @Override
  public Page<CommentDto> getMyComments(UUID userId, Pageable pageable) {
    Page<Comment> comments = commentRepository.findByUserIdAndIsDeletedFalse(userId, pageable);
    return comments.map(comment -> commentMapper.toDto(comment, userId));
  }

  @Transactional
  @Override
  public CommentLikeDto likeComment(UUID commentId, UUID userId) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new MonewException(ErrorCode.COMMENT_NOT_FOUND));
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new MonewException(ErrorCode.USER_NOT_FOUND));

    if (commentLikeRepository.existsByCommentAndUser(comment, user)) {
      throw new MonewException(ErrorCode.COMMENT_ALREADY_LIKED);
    }

    CommentLike like = new CommentLike(null, comment, user, Instant.now());
    commentLikeRepository.save(like);

    comment.increaseLikeCount();
    commentRepository.save(comment);

    eventPublisher.publishEvent(new CommentLikeCreatedEventForNotification(
        commentId,
        userId,
        comment.getUser().getId()
    ));

    eventPublisher.publishEvent(new CommentLikeCreatedEvent(userId, like));

    return commentLikeMapper.toDto(like);
  }

  @Transactional
  @Override
  public void unlikeComment(UUID commentId, UUID userId) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new MonewException(ErrorCode.COMMENT_NOT_FOUND));
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new MonewException(ErrorCode.USER_NOT_FOUND));

    CommentLike like = commentLikeRepository.findByCommentAndUser(comment, user)
        .orElseThrow(() -> new MonewException(ErrorCode.COMMENT_LIKE_NOT_ALLOWED));

    eventPublisher.publishEvent(new CommentDeletedEvent(userId, like.getId()));

    commentLikeRepository.delete(like);

    comment.decreaseLikeCount();
    commentRepository.save(comment);

  }

  @Override
  public CommentDto update(UUID commentId, UUID userId, CommentUpdateRequest request) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new MonewException(ErrorCode.COMMENT_NOT_FOUND));

    if (!comment.getUser().getId().equals(userId)) {
      throw new MonewException(ErrorCode.COMMENT_FORBIDDEN);
    }

    comment.updateContent(request.content());
    Comment saved = commentRepository.save(comment);

    eventPublisher.publishEvent(new CommentUpdatedEvent(userId, comment));

    return commentMapper.toDto(saved, userId);
  }

  @Transactional
  @Override
  public void softDelete(UUID commentId, UUID userId) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new MonewException(ErrorCode.COMMENT_NOT_FOUND));

    if (!comment.getUser().getId().equals(userId)) {
      throw new MonewException(ErrorCode.COMMENT_FORBIDDEN);
    }

    comment.getArticle().decrementCommentCount();

    comment.markDeleted();
    commentRepository.save(comment);
  }

  @Transactional
  @Override
  public void hardDelete(UUID commentId, UUID userId) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> {
          log.warn("댓글 삭제 실패 - 존재하지 않음: commentId={}", commentId);
          return new MonewException(ErrorCode.COMMENT_NOT_FOUND);
        });

    if (!comment.getUser().getId().equals(userId)) {
      log.warn("댓글 삭제 권한 없음: userId={}, commentOwnerId={}", userId, comment.getUser().getId());
      throw new MonewException(ErrorCode.COMMENT_FORBIDDEN);
    }

    commentRepository.deleteById(commentId);
    log.info("댓글 삭제 완료: commentId={}, deletedBy={}", commentId, userId);

    eventPublisher.publishEvent(new CommentDeletedEvent(userId, commentId));
  }
}