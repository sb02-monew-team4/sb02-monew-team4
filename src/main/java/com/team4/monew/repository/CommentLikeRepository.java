package com.team4.monew.repository;

import com.team4.monew.entity.CommentLike;
import com.team4.monew.entity.Comment;
import com.team4.monew.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentLikeRepository extends JpaRepository<CommentLike, UUID> {
  Optional<CommentLike> findByCommentAndUser(Comment comment, User user);
  boolean existsByCommentAndUser(Comment comment, User user);
  void deleteByCommentAndUser(Comment comment, User user);
}
