package com.team4.monew.mapper;

import com.team4.monew.dto.article.ArticleViewDto;
import com.team4.monew.entity.ArticleView;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ArticleViewMapper {

  @Mapping(target = "userId", source = "user.id")
  @Mapping(target = "articleId", source = "article.id")
  @Mapping(target = "source", source = "article.source")
  @Mapping(target = "originalLink", source = "article.originalLink")
  @Mapping(target = "publishedDate", source = "article.publishedDate")
  @Mapping(target = "summary", source = "article.summary")
  @Mapping(target = "commentCount", source = "article.commentCount")
  @Mapping(target = "viewCount", source = "article.viewCount")
  public ArticleViewDto toDto(ArticleView articleView);
}
