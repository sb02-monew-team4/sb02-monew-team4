package com.team4.monew.mapper;

import com.team4.monew.dto.comment.CommentDto;
import com.team4.monew.entity.Comment;
import java.util.UUID;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {

  @Mapping(target = "articleId", source = "news.id")
  @Mapping(target = "userId", source = "user.id")
  @Mapping(target = "userNickname", source = "user.nickname")
  @Mapping(target = "likeCount", expression = "java(comment.getLikeCount() != null ? comment.getLikeCount().intValue() : 0)")
  @Mapping(target = "likeByMe", constant = "false")
  CommentDto toDto(Comment comment, @Context UUID requesterId);
}

