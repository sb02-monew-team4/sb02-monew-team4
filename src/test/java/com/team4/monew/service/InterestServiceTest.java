package com.team4.monew.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.team4.monew.dto.interest.InterestDto;
import com.team4.monew.dto.interest.InterestRegisterRequest;
import com.team4.monew.entity.Interest;
import com.team4.monew.entity.User;
import com.team4.monew.exception.ErrorCode;
import com.team4.monew.exception.MonewException;
import com.team4.monew.mapper.InterestMapper;
import com.team4.monew.repository.InterestRepository;
import com.team4.monew.repository.UserRepository;
import com.team4.monew.service.basic.BasicInterestService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InterestServiceTest {

  @InjectMocks
  private BasicInterestService basicInterestService;

  @Mock
  private InterestRepository interestRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private InterestMapper interestMapper;

  private final UUID userId = UUID.randomUUID();

  @Test
  @DisplayName("관심사 등록 성공 - 유사 이름 없음, 키워드 1개 이상")
  void register_success() {
    // given
    InterestRegisterRequest request = new InterestRegisterRequest("경제", List.of("주식", "금리"));

    when(interestRepository.findAll()).thenReturn(List.of());

    User mockUser = mock(User.class);
    when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

    Interest interestEntity = mock(Interest.class);
    when(interestMapper.toEntity(request)).thenReturn(interestEntity);

    InterestDto expectedDto = new InterestDto(UUID.randomUUID(), "경제", List.of("주식", "금리"), 0, false);
    when(interestMapper.toDto(interestEntity, userId)).thenReturn(expectedDto);

    // when
    InterestDto actual = basicInterestService.register(userId, request);

    // then
    assertEquals(expectedDto.name(), actual.name());
    assertEquals(expectedDto.keywords(), actual.keywords());

    verify(interestRepository).save(interestEntity);
    verify(userRepository).findById(userId);
    verify(interestMapper).toDto(interestEntity, userId);
  }

  @Test
  @DisplayName("관심사 등록 실패 - 80% 유사한 이름 있음")
  void register_fail_whenSimilarNameExists() {
    UUID userId = UUID.randomUUID();
    InterestRegisterRequest request = new InterestRegisterRequest("가나다라", List.of("ㄱㄴㄷ"));

    Interest existing = new Interest("가나다라마", List.of());

    when(interestRepository.findAll()).thenReturn(List.of(existing));

    MonewException ex = assertThrows(
        MonewException.class,
        () -> basicInterestService.register(userId, request)
    );

    assertEquals(ErrorCode.INTEREST_ALREADY_EXISTS, ex.getErrorCode());
  }

  @Test
  @DisplayName("관심사 등록 실패 - 키워드 비어있음")
  void register_fail_whenNoKeywords() {
    UUID userId = UUID.randomUUID();
    InterestRegisterRequest request = new InterestRegisterRequest("헬스", List.of());

    when(interestRepository.findAll()).thenReturn(List.of());

    MonewException ex = assertThrows(
        MonewException.class,
        () -> basicInterestService.register(userId, request)
    );

    assertEquals(ErrorCode.KEYWORDS_REQUIRED, ex.getErrorCode());
  }

  @Test
  @DisplayName("관심사 등록 실패 - 사용자 없음")
  void register_fail_whenUserNotFound() {
    UUID userId = UUID.randomUUID();
    InterestRegisterRequest request = new InterestRegisterRequest("헬스", List.of("운동"));

    when(interestRepository.findAll()).thenReturn(List.of());
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    MonewException ex = assertThrows(
        MonewException.class,
        () -> basicInterestService.register(userId, request)
    );

    assertEquals(ErrorCode.USER_NOT_FOUND, ex.getErrorCode());
  }

}