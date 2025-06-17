package com.team4.monew.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.team4.monew.entity.Interest;
import com.team4.monew.entity.InterestKeyword;
import com.team4.monew.repository.InterestRepository;
import com.team4.monew.service.collector.NaverApiCollectorService;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class NaverApiCollectorServiceTest {

  @Mock
  private InterestRepository interestRepository;

  @InjectMocks
  private NaverApiCollectorService naverApiCollectorService;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(naverApiCollectorService,
        "clientId", "test-client-id");
    ReflectionTestUtils.setField(naverApiCollectorService,
        "clientSecret", "test-client-secret");
    ReflectionTestUtils.setField(naverApiCollectorService,
        "apiURL", "http://test-api.com");
  }

  @Test
  @DisplayName("Interest의 고유 키워드들을 추출하여 API 호출 대상을 결정해야 한다.")
  void shouldExtractUniqueKeywordsForApiCalls() {
    // given 스프링 키워드 중복
    Interest interest1 = createInterestWithKeyword("관심사1", List.of("스프링", "자바", "개발"));
    Interest interest2 = createInterestWithKeyword("관심사2", List.of("스프링", "리액트", "자바스크립트"));
    Interest interest3 = createInterestWithKeyword("관심사3", List.of("자바", "파이썬", "C++"));

    when(interestRepository.findAll()).thenReturn(List.of(interest1, interest2, interest3));

    // when
    Set<String> uniqueKeywords = naverApiCollectorService.extractUniqueKeywords();

    // then
    assertThat(uniqueKeywords).hasSize(7);
    assertThat(uniqueKeywords).containsExactlyInAnyOrder(
        "스프링", "자바", "개발", "리액트", "자바스크립트", "파이썬", "C++"
    );

    assertThat(uniqueKeywords).doesNotHaveDuplicates();
  }

  @Test
  @DisplayName("빈 Interest 목록일 때 빈 키워드 Set을 반환해야 한다.")
  void shouldReturnEmptySetWhenNoInterests() {
    // given
    when(interestRepository.findAll()).thenReturn(Collections.emptyList());

    // when
    Set<String> uniqueKeywords =
        naverApiCollectorService.extractUniqueKeywords();

    // then
    assertThat(uniqueKeywords).isEmpty();
  }
  
  @Test
  @DisplayName("대소문자가 다른 키워드는 별개 키워드로 저장되어야 한다")
  void shouldTreatKeywordsWithDifferentCasesAsSeparate() {
    // given
    Interest interest1 = createInterestWithKeyword("관심사1", List.of("Spring", "java"));
    Interest interest2 = createInterestWithKeyword("관심사2", List.of("spring", "JAVA"));

    when(interestRepository.findAll()).thenReturn(List.of(interest1, interest2));

    // when
    Set<String> uniqueKeywords = naverApiCollectorService.extractUniqueKeywords();

    // then
    assertThat(uniqueKeywords).hasSize(4);
    assertThat(uniqueKeywords).containsExactlyInAnyOrder(
        "Spring", "java", "spring", "JAVA"
    );
  }

  private Interest createInterestWithKeyword(String name, List<String> keywords) {
    Interest interest = new Interest();
    ReflectionTestUtils.setField(interest, "name", name);

    List<InterestKeyword> keywordEntities = keywords.stream()
        .map(keyword -> {
          InterestKeyword interestKeyword = new InterestKeyword();
          ReflectionTestUtils.setField(interestKeyword, "keyword", keyword);
          ReflectionTestUtils.setField(interestKeyword, "interest", interest);
          return interestKeyword;
        })
        .toList();

    ReflectionTestUtils.setField(interest, "keywords", keywordEntities);

    return interest;
  }

}
