package com.team4.monew.mapper;

import com.team4.monew.dto.article.ArticleViewDto;
import com.team4.monew.entity.ArticleView;
import com.team4.monew.repository.CommentRepository;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class ArticleViewMapper {

  @Autowired
  private CommentRepository commentRepository;

  public ArticleViewDto toDto(ArticleView articleView) {
    long commentCount = commentRepository.countByNewsId(articleView.getArticle().getId());

    return new ArticleViewDto(
        articleView.getId(),
        articleView.getUser().getId(),
        articleView.getViewedAt(),
        articleView.getArticle().getId(),
        articleView.getArticle().getSource(),
        articleView.getArticle().getOriginalLink(),
        articleView.getArticle().getPublishedDate(),
        articleView.getArticle().getSummary(),
        commentCount,
        articleView.getArticle().getViewCount()
    );
  }
}
