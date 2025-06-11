package com.team4.monew.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team4.monew.config.WebConfig;
import com.team4.monew.dto.article.ArticleViewDto;
import com.team4.monew.interceptor.AuthInterceptor;
import com.team4.monew.service.ArticleService;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


@WebMvcTest(controllers = ArticleController.class,
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {AuthInterceptor.class,
            WebConfig.class})
    }
)
@ActiveProfiles("test")
public class ArticleControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private ArticleService articleService;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  @DisplayName("기사 뷰 등록 성공")
  void registerNewsView() throws Exception {
    // given
    UUID newsId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    ArticleViewDto responseDto = new ArticleViewDto(
        UUID.randomUUID(),
        userId,
        Instant.now(),
        newsId,
        "NAVER",
        "https://news.naver.com/",
        Instant.now(),
        "기사 요약",
        12L,
        100L
    );
    BDDMockito.given(articleService.registerNewsView(newsId, userId))
        .willReturn(responseDto);

    // when & then
    mockMvc.perform(post("/api/articles/{articleId}/article-views", newsId)
            .header("Monew-Request-User-ID", userId.toString())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(responseDto.id().toString()))
        .andExpect(jsonPath("$.viewedBy").value(userId.toString()))
        .andExpect(jsonPath("$.articleId").value(newsId.toString()))
        .andExpect(jsonPath("$.source").value("NAVER"))
        .andExpect(jsonPath("$.sourceUrl").value("https://news.naver.com/"))
        .andExpect(jsonPath("$.articleSummary").value("기사 요약"))
        .andExpect(jsonPath("$.articleCommentCount").value(12))
        .andExpect(jsonPath("$.articleViewCount").value(100));
  }

}
