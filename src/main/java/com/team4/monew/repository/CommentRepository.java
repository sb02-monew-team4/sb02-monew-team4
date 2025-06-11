package com.team4.monew.repository;

import com.team4.monew.entity.Comment;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

  long countByNewsId(UUID newId);
}