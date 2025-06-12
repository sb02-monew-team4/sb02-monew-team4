package com.team4.monew.service;

import com.team4.monew.dto.interest.CursorPageResponseInterestDto;
import com.team4.monew.dto.interest.InterestDto;
import com.team4.monew.dto.interest.InterestRegisterRequest;
import com.team4.monew.dto.interest.InterestUpdateRequest;
import com.team4.monew.dto.interest.SubscriptionDto;
import java.util.UUID;

public interface InterestService {
  InterestDto register(UUID requestUserId, InterestRegisterRequest request);
  CursorPageResponseInterestDto getInterests(
      String keyword,
      String orderBy,
      String direction,
      String cursor,
      String after,
      int limit,
      UUID requestUserId);
  InterestDto update(UUID interestId, InterestUpdateRequest request);
  void hardDelete(UUID interestId, UUID userId);
  SubscriptionDto subscribeInterest(UUID interestId, UUID userId);
  void unsubscribeInterest(UUID interestId, UUID userId);
}
