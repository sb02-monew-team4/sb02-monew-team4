package com.team4.monew.mapper;

import com.team4.monew.dto.UserActivity.UserActivityDto;
import com.team4.monew.entity.UserActivity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserActivityMapper {

  @Mapping(source = "id", target = "id")
  @Mapping(source = "user.email", target = "email")
  @Mapping(source = "user.nickname", target = "nickname")
  @Mapping(source = "user.createdAt", target = "createdAt")
  @Mapping(source = "subscriptionDtos", target = "subscriptions")
  @Mapping(source = "recentCommentActivityDtos", target = "comments")
  @Mapping(source = "recentCommentLikeActivityDtos", target = "commentLikes")
  @Mapping(source = "recentArticleViewDtos", target = "articleViews")
  UserActivityDto toDto(UserActivity document);

}
