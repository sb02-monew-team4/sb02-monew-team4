package com.team4.monew.service.basic;

import com.team4.monew.dto.comment.CommentDto;
import com.team4.monew.dto.comment.CommentLikeDto;
import com.team4.monew.dto.comment.CommentRegisterRequest;
import com.team4.monew.dto.comment.CommentUpdateRequest;
import com.team4.monew.dto.comment.CursorPageResponseCommentDto;
import com.team4.monew.entity.Comment;
import com.team4.monew.entity.CommentLike;
import com.team4.monew.entity.Article;
import com.team4.monew.entity.User;
import com.team4.monew.exception.MonewException;
import com.team4.monew.exception.ErrorCode;
import com.team4.monew.mapper.CommentMapper;
import com.team4.monew.repository.CommentLikeRepository;
import com.team4.monew.repository.CommentRepository;
import com.team4.monew.repository.ArticleRepository;
import com.team4.monew.repository.UserRepository;
import com.team4.monew.service.CommentService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicCommentService implements CommentService {

  private final UserRepository userRepository;
  private final ArticleRepository newsRepository;
  private final CommentRepository commentRepository;
  private final CommentLikeRepository commentLikeRepository;
  private final CommentMapper commentMapper;

  @Transactional
  @Override
  public CommentDto register(CommentRegisterRequest request) {
    User user = userRepository.findById(request.userId())
        .orElseThrow(() -> new MonewException(ErrorCode.USER_NOT_FOUND));

    Article news = newsRepository.findById(request.articleId())
        .orElseThrow(() -> new MonewException(ErrorCode.NEWS_NOT_FOUND));

    Comment comment = new Comment(user, news, request.content());
    Comment saved = commentRepository.save(comment);

    return CommentDto.from(saved);
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

    Long total = commentRepository.countByNewsId(articleId);
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
    return comments.map(CommentDto::from);
  }

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

    return CommentLikeDto.from(like);
  }

  @Override
  public void unlikeComment(UUID commentId, UUID userId) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new MonewException(ErrorCode.COMMENT_NOT_FOUND));
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new MonewException(ErrorCode.USER_NOT_FOUND));

    CommentLike like = commentLikeRepository.findByCommentAndUser(comment, user)
        .orElseThrow(() -> new MonewException(ErrorCode.COMMENT_LIKE_NOT_ALLOWED));

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

    return CommentDto.from(saved);
  }

  @Override
  public void softDelete(UUID commentId, UUID userId) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new MonewException(ErrorCode.COMMENT_NOT_FOUND));

    if (!comment.getUser().getId().equals(userId)) {
      throw new MonewException(ErrorCode.COMMENT_FORBIDDEN);
    }

    comment.markDeleted();
    commentRepository.save(comment);
  }

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
  }
}
