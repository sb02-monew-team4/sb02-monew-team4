package com.team4.monew.service;

import com.team4.monew.dto.interest.CursorPageResponseInterestDto;
import com.team4.monew.dto.interest.InterestDto;
import com.team4.monew.dto.interest.InterestRegisterRequest;
import com.team4.monew.dto.interest.InterestUpdateRequest;
import java.time.Instant;
import java.util.UUID;

public interface InterestService {
  InterestDto register(InterestRegisterRequest request);
  CursorPageResponseInterestDto getInterests(
      String keyword,
      String orderBy,
      String direction,
      String cursor,
      Instant after,
      int limit,
      UUID requestUserId);
  InterestDto update(UUID interestId, InterestUpdateRequest request);
  void softDelete(UUID interestId, UUID userId);
  void hardDelete(UUID interestId, UUID userId);
}
