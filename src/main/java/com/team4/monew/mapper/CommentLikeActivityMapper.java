package com.team4.monew.mapper;

import com.team4.monew.dto.UserActivity.CommentLikeActivityDto;
import com.team4.monew.entity.CommentLike;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentLikeActivityMapper {

  @Mapping(source = "id", target = "id")
  @Mapping(source = "createdAt", target = "createdAt")
  @Mapping(source = "comment.id", target = "commentId")
  @Mapping(source = "comment.article.id", target = "articleId")
  @Mapping(source = "comment.article.title", target = "articleTitle")
  @Mapping(source = "comment.user.id", target = "commentUserId")
  @Mapping(source = "comment.user.nickname", target = "commentUserNickname")
  @Mapping(source = "comment.content", target = "commentContent")
  @Mapping(source = "comment.likeCount", target = "commentLikeCount")
  @Mapping(source = "comment.createAt", target = "commentCreatedAt")
  CommentLikeActivityDto toDto(CommentLike commentLike);
}

