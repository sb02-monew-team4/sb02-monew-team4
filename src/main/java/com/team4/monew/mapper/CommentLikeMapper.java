package com.team4.monew.mapper;

import com.team4.monew.dto.comment.CommentLikeDto;
import com.team4.monew.entity.CommentLike;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentLikeMapper {

  @Mapping(target = "id", source = "id")
  @Mapping(target = "likedBy", source = "user.id")
  @Mapping(target = "createdAt", source = "createdAt")
  @Mapping(target = "commentId", source = "comment.id")
  @Mapping(target = "articleId", source = "comment.article.id")
  @Mapping(target = "commentUserId", source = "comment.user.id")
  @Mapping(target = "commentUserNickname", source = "comment.user.nickname")
  @Mapping(target = "commentContent", source = "comment.content")
  @Mapping(target = "commentLikeCount", expression = "java(like.getComment().getLikeCount() != null ? like.getComment().getLikeCount().intValue() : 0)")
  @Mapping(target = "commentCreatedAt", source = "comment.createdAt")
  CommentLikeDto toDto(CommentLike like);
}
