package com.team4.monew.repository;

import com.team4.monew.entity.Comment;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, UUID>, CommentRepositoryCustom {
  Page<Comment> findByUserIdAndIsDeletedFalse(UUID userId, Pageable pageable);
  List<Comment> findCommentsByArticleWithCursorPaging(
      UUID newsId,
      String orderBy,
      String direction,
      String cursor,
      String after,
      int limit
  );

  long countByNewsId(UUID newsId);
}
