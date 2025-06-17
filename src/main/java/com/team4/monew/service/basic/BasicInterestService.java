package com.team4.monew.service.basic;

import com.team4.monew.asynchronous.event.subscription.InterestDeletedEvent;
import com.team4.monew.asynchronous.event.subscription.SubscriptionCreatedEvent;
import com.team4.monew.asynchronous.event.subscription.SubscriptionDeletedEvent;
import com.team4.monew.asynchronous.event.subscription.SubscriptionUpdatedEvent;
import com.team4.monew.dto.interest.CursorPageResponseInterestDto;
import com.team4.monew.dto.interest.InterestDto;
import com.team4.monew.dto.interest.InterestRegisterRequest;
import com.team4.monew.dto.interest.InterestUpdateRequest;
import com.team4.monew.dto.interest.SubscriptionDto;
import com.team4.monew.entity.Article;
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
import com.team4.monew.service.InterestService;
import com.team4.monew.util.DateTimeUtils;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicInterestService implements InterestService {

  private final InterestRepository interestRepository;
  private final UserRepository userRepository;
  private final InterestMapper interestMapper;
  private final SubscriptionRepository subscriptionRepository;
  private final SubscriptionMapper subscriptionMapper;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  @Override
  public InterestDto register(UUID authenticatedUserId, InterestRegisterRequest request) {

    String newInterestName = request.name();

    List<Interest> existing = interestRepository.findAll();

    for (Interest interest : existing) {
      if (isSimilar(interest.getName(), newInterestName)) {
        throw new MonewException(ErrorCode.INTEREST_ALREADY_EXISTS);
      }
    }

    List<String> keywords = request.keywords();
    if (keywords == null || keywords.isEmpty()) {
      throw new MonewException(ErrorCode.KEYWORDS_REQUIRED);
    }

    if (!userRepository.existsById(authenticatedUserId)) {
      throw new MonewException(ErrorCode.USER_NOT_FOUND);
    }

    Interest interest = interestMapper.toEntity(request);
    interestRepository.save(interest);

    return interestMapper.toDto(interest, authenticatedUserId);
  }

  private boolean isSimilar(String s1, String s2) {
    LevenshteinDistance distance = new LevenshteinDistance();
    int editDistance = distance.apply(s1, s2);

    int maxLength = Math.max(s1.length(), s2.length());
    if (maxLength == 0) {
      return true;
    }

    double similarity = 1.0 - ((double) editDistance / maxLength);
    return similarity >= 0.8;
  }

  @Override
  public CursorPageResponseInterestDto getInterests(
      String keyword,
      String orderBy,
      String direction,
      String cursor,
      String after,
      int limit,
      UUID requestUserId
  ) {
    LocalDateTime afterDateTime = DateTimeUtils.parseToLocalDateTime(after);
    List<Interest> interests = interestRepository.findInterestsWithCursorPaging(
        keyword, orderBy, direction, cursor, afterDateTime, limit
    );

    List<InterestDto> dtoList = interests.stream()
        .map(interest -> interestMapper.toDto(interest, requestUserId))
        .toList();

    String nextCursor = null;
    String nextAfter = null;
    if (!interests.isEmpty()) {
      Interest last = interests.get(interests.size() - 1);
      nextCursor = switch (orderBy) {
        case "subscriberCount" -> String.valueOf(last.getSubscriberCount());
        case "name" -> last.getName();
        default -> null;
      };
      nextAfter = last.getCreatedAt().toString();
    }

    Long total = interestRepository.countByKeyword(keyword);
    boolean hasNext = interests.size() == limit;

    return new CursorPageResponseInterestDto(
        dtoList,
        nextCursor,
        nextAfter,
        dtoList.size(),
        total,
        hasNext
    );
  }

  @Transactional
  @Override
  public InterestDto update(UUID interestId, InterestUpdateRequest request) {
    Interest interest = interestRepository.findById(interestId)
        .orElseThrow(() -> new MonewException(ErrorCode.INTEREST_NOT_FOUND));

    List<String> keywords = Optional.ofNullable(request.keywords())
        .orElseThrow(() -> new MonewException(ErrorCode.KEYWORDS_REQUIRED));

    List<String> distinctKeywords = keywords.stream()
        .map(String::trim)
        .filter(k -> !k.isEmpty())
        .distinct()
        .toList();

    if (distinctKeywords.isEmpty()) {
      throw new MonewException(ErrorCode.KEYWORDS_REQUIRED);
    }

    if (distinctKeywords.size() > 10) {
      throw new MonewException(ErrorCode.KEYWORDS_TOO_MANY);
    }

    if (distinctKeywords.stream().anyMatch(k -> k.length() > 20)) {
      throw new MonewException(ErrorCode.KEYWORD_TOO_LONG);
    }

    interest.updateKeywords(distinctKeywords);

    eventPublisher.publishEvent(new SubscriptionUpdatedEvent(interestId, distinctKeywords));
    return interestMapper.toDto(interest, null);
  }

  @Transactional
  @Override
  public void hardDelete(UUID interestId, UUID userId) {

    Interest interest = interestRepository.findById(interestId)
        .orElseThrow(() -> new MonewException(ErrorCode.INTEREST_NOT_FOUND));

    eventPublisher.publishEvent(new InterestDeletedEvent(userId, interestId));

    for (Article article : new HashSet<>(interest.getArticle())) {
      article.removeInterest(interest);
    }
    interestRepository.delete(interest);
  }

  @Transactional
  @Override
  public SubscriptionDto subscribeInterest(UUID interestId, UUID userId) {
    Interest interest = interestRepository.findById(interestId)
        .orElseThrow(() -> new MonewException(ErrorCode.INTEREST_NOT_FOUND));

    Optional<Subscription> existing = subscriptionRepository.findByUserIdAndInterestId(userId,
        interestId);
    if (existing.isPresent()) {
      Subscription subscription = existing.get();
      eventPublisher.publishEvent(new SubscriptionCreatedEvent(userId, subscription));
      return subscriptionMapper.toDto(subscription);
    }

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new MonewException(ErrorCode.USER_NOT_FOUND));

    Subscription subscription = new Subscription(user, interest);
    subscriptionRepository.save(subscription);

    eventPublisher.publishEvent(new SubscriptionCreatedEvent(userId, subscription));

    interest.increaseSubscriberCount();

    return subscriptionMapper.toDto(subscription);
  }

  @Transactional
  @Override
  public void unsubscribeInterest(UUID interestId, UUID userId) {
    Interest interest = interestRepository.findById(interestId)
        .orElseThrow(() -> new MonewException(ErrorCode.INTEREST_NOT_FOUND));

    Subscription subscription = subscriptionRepository.findByUserIdAndInterestId(userId, interestId)
        .orElseThrow(() -> new MonewException(ErrorCode.SUBSCRIPTION_NOT_FOUND));

    eventPublisher.publishEvent(new SubscriptionDeletedEvent(userId, subscription.getId()));

    subscriptionRepository.delete(subscription);

    interest.decreaseSubscriberCount();
  }
}