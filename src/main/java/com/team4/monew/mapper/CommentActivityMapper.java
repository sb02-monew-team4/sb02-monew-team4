package com.team4.monew.mapper;

import com.team4.monew.dto.UserActivity.CommentActivityDto;
import com.team4.monew.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentActivityMapper {

  @Mapping(source = "article.id", target = "articleId")
  @Mapping(source = "article.title", target = "articleTitle")
  @Mapping(source = "user.id", target = "userId")
  @Mapping(source = "user.nickname", target = "userNickname")
  @Mapping(source = "content", target = "content")
  @Mapping(source = "likeCount", target = "likeCount")
  @Mapping(source = "createdAt", target = "createdAt")
  CommentActivityDto toDto(Comment comment);
}

