package com.team4.monew.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.team4.monew.dto.interest.CursorPageResponseInterestDto;
import com.team4.monew.dto.interest.InterestDto;
import com.team4.monew.dto.interest.InterestRegisterRequest;
import com.team4.monew.dto.interest.InterestUpdateRequest;
import com.team4.monew.dto.interest.SubscriptionDto;
import com.team4.monew.entity.Interest;
import com.team4.monew.entity.Subscription;
import com.team4.monew.entity.User;
import com.team4.monew.exception.ErrorCode;
import com.team4.monew.exception.MonewException;
import com.team4.monew.mapper.InterestMapper;
import com.team4.monew.mapper.SubscriptionMapper;
import com.team4.monew.repository.InterestRepository;
import com.team4.monew.repository.SubscriptionRepository;
import com.team4.monew.repository.UserRepository;
import com.team4.monew.service.basic.BasicInterestService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class InterestServiceTest {

  private final UUID userId = UUID.randomUUID();
  @InjectMocks
  private BasicInterestService basicInterestService;
  @Mock
  private InterestRepository interestRepository;
  @Mock
  private SubscriptionRepository subscriptionRepository;
  @Mock
  private ApplicationEventPublisher eventPublisher;
  @Mock
  private UserRepository userRepository;
  @Mock
  private InterestMapper interestMapper;
  @Mock
  private SubscriptionMapper subscriptionMapper;

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

    InterestDto expectedDto = new InterestDto(UUID.randomUUID(), "경제", List.of("주식", "금리"), 0,
        false);
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

  @Test
  @DisplayName("관심사 목록 조회 성공")
  void getInterests_success() {
    UUID userId = UUID.randomUUID();

    Interest interest = new Interest(
        UUID.randomUUID(),
        "금융",
        0L,
        LocalDateTime.now(),
        LocalDateTime.now(),
        new ArrayList<>(),
        new ArrayList<>(),
        new HashSet<>()
    );

    InterestDto dto = mock(InterestDto.class);

    when(interestRepository.findInterestsWithCursorPaging("금융", "name", "asc", null, null, 10))
        .thenReturn(List.of(interest));
    when(interestMapper.toDto(interest, userId)).thenReturn(dto);
    when(interestRepository.countByKeyword("금융")).thenReturn(1L);

    CursorPageResponseInterestDto response = basicInterestService.getInterests(
        "금융", "name", "asc", null, null, 10, userId
    );

    assertEquals(1, response.size());
    assertEquals(1L, response.totalElements());
  }

  @Test
  @DisplayName("관심사 수정 성공 - 유효한 키워드 리스트")
  void update_success() {
    UUID interestId = UUID.randomUUID();
    Interest interest = mock(Interest.class);

    InterestUpdateRequest request = new InterestUpdateRequest(List.of("기술", "코딩"));

    when(interestRepository.findById(interestId)).thenReturn(Optional.of(interest));

    InterestDto expectedDto = new InterestDto(interestId, "IT", List.of("기술", "코딩"), 0, false);
    when(interestMapper.toDto(interest, null)).thenReturn(expectedDto);

    InterestDto actual = basicInterestService.update(interestId, request);

    assertEquals(expectedDto.keywords(), actual.keywords());
    verify(interest).updateKeywords(List.of("기술", "코딩"));
  }

  @Test
  @DisplayName("관심사 수정 실패 - 관심사 ID 존재하지 않음")
  void update_fail_interestNotFound() {
    UUID interestId = UUID.randomUUID();
    InterestUpdateRequest request = new InterestUpdateRequest(List.of("뉴스"));

    when(interestRepository.findById(interestId)).thenReturn(Optional.empty());

    MonewException ex = assertThrows(
        MonewException.class,
        () -> basicInterestService.update(interestId, request)
    );

    assertEquals(ErrorCode.INTEREST_NOT_FOUND, ex.getErrorCode());
  }

  @Test
  @DisplayName("관심사 수정 실패 - 키워드가 null")
  void update_fail_keywordsNull() {
    UUID interestId = UUID.randomUUID();
    Interest interest = mock(Interest.class);

    InterestUpdateRequest request = new InterestUpdateRequest(null);

    when(interestRepository.findById(interestId)).thenReturn(Optional.of(interest));

    MonewException ex = assertThrows(
        MonewException.class,
        () -> basicInterestService.update(interestId, request)
    );

    assertEquals(ErrorCode.KEYWORDS_REQUIRED, ex.getErrorCode());
  }

  @Test
  @DisplayName("관심사 수정 실패 - 중복 제거 후 키워드 없음")
  void update_fail_keywordsEmptyAfterDistinct() {
    UUID interestId = UUID.randomUUID();
    Interest interest = mock(Interest.class);

    InterestUpdateRequest request = new InterestUpdateRequest(List.of());

    when(interestRepository.findById(interestId)).thenReturn(Optional.of(interest));

    MonewException ex = assertThrows(
        MonewException.class,
        () -> basicInterestService.update(interestId, request)
    );

    assertEquals(ErrorCode.KEYWORDS_REQUIRED, ex.getErrorCode());
  }

  @Test
  @DisplayName("관심사 삭제 성공")
  void hardDelete_success() {
    UUID interestId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    Interest interest = mock(Interest.class);
    when(interestRepository.findById(interestId)).thenReturn(Optional.of(interest));

    basicInterestService.hardDelete(interestId, userId);

    verify(interestRepository).delete(interest);
  }

  @Test
  @DisplayName("관심사 삭제 실패 - 관심사 ID 없음")
  void hardDelete_fail_interestNotFound() {
    UUID interestId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    when(interestRepository.findById(interestId)).thenReturn(Optional.empty());

    MonewException ex = assertThrows(
        MonewException.class,
        () -> basicInterestService.hardDelete(interestId, userId)
    );

    assertEquals(ErrorCode.INTEREST_NOT_FOUND, ex.getErrorCode());
  }

  @Test
  @DisplayName("관심사 구독 성공 - 기존 구독 없음")
  void subscribeInterest_success() {
    UUID interestId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    Interest interest = mock(Interest.class);
    User user = mock(User.class);
    Subscription subscription = mock(Subscription.class);
    SubscriptionDto expectedDto = mock(SubscriptionDto.class);

    when(interestRepository.findById(interestId)).thenReturn(Optional.of(interest));
    when(subscriptionRepository.findByUserIdAndInterestId(userId, interestId)).thenReturn(
        Optional.empty());
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    when(subscriptionMapper.toDto(any(Subscription.class))).thenReturn(expectedDto);

    SubscriptionDto actualDto = basicInterestService.subscribeInterest(interestId, userId);

    verify(interest).increaseSubscriberCount();
  }

  @Test
  @DisplayName("관심사 구독 실패 - 관심사 ID 없음")
  void subscribeInterest_fail_interestNotFound() {
    UUID interestId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    when(interestRepository.findById(interestId)).thenReturn(Optional.empty());

    MonewException ex = assertThrows(
        MonewException.class,
        () -> basicInterestService.subscribeInterest(interestId, userId)
    );

    assertEquals(ErrorCode.INTEREST_NOT_FOUND, ex.getErrorCode());
  }

  @Test
  @DisplayName("관심사 구독 실패 - 사용자 없음")
  void subscribeInterest_fail_userNotFound() {
    UUID interestId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    Interest interest = mock(Interest.class);
    when(interestRepository.findById(interestId)).thenReturn(Optional.of(interest));
    when(subscriptionRepository.findByUserIdAndInterestId(userId, interestId)).thenReturn(
        Optional.empty());
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    MonewException ex = assertThrows(
        MonewException.class,
        () -> basicInterestService.subscribeInterest(interestId, userId)
    );

    assertEquals(ErrorCode.USER_NOT_FOUND, ex.getErrorCode());
  }

  @Test
  @DisplayName("관심사 구독 취소 성공")
  void unsubscribeInterest_success() {
    UUID interestId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    Interest interest = mock(Interest.class);
    Subscription subscription = mock(Subscription.class);

    when(interestRepository.findById(interestId)).thenReturn(Optional.of(interest));
    when(subscriptionRepository.findByUserIdAndInterestId(userId, interestId)).thenReturn(
        Optional.of(subscription));

    basicInterestService.unsubscribeInterest(interestId, userId);

    verify(subscriptionRepository).delete(subscription);
    verify(interest).decreaseSubscriberCount();
  }

  @Test
  @DisplayName("관심사 구독 취소 실패 - 구독 없음")
  void unsubscribeInterest_fail_subscriptionNotFound() {
    UUID interestId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    Interest interest = mock(Interest.class);
    when(interestRepository.findById(interestId)).thenReturn(Optional.of(interest));
    when(subscriptionRepository.findByUserIdAndInterestId(userId, interestId)).thenReturn(
        Optional.empty());

    MonewException ex = assertThrows(
        MonewException.class,
        () -> basicInterestService.unsubscribeInterest(interestId, userId)
    );

    assertEquals(ErrorCode.SUBSCRIPTION_NOT_FOUND, ex.getErrorCode());
  }
}