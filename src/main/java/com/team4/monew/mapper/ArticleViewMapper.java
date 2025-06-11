package com.team4.monew.mapper;

import com.team4.monew.dto.article.ArticleViewDto;
import com.team4.monew.entity.ArticleView;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ArticleViewMapper {

  @Mapping(target = "viewedBy", source = "user.id")
  @Mapping(target = "createdAt", source = "viewedAt")
  @Mapping(target = "articleId", source = "article.id")
  @Mapping(target = "source", source = "article.source")
  @Mapping(target = "sourceUrl", source = "article.originalLink")
  @Mapping(target = "articlePublishedDate", source = "article.publishedDate")
  @Mapping(target = "articleSummary", source = "article.summary")
  @Mapping(target = "articleCommentCount", source = "article.commentCount")
  @Mapping(target = "articleViewCount", source = "article.viewCount")
  ArticleViewDto toDto(ArticleView articleView);
}
