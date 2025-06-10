package com.team4.monew.service.basic;

import com.team4.monew.dto.interest.CursorPageResponseInterestDto;
import com.team4.monew.dto.interest.InterestDto;
import com.team4.monew.dto.interest.InterestRegisterRequest;
import com.team4.monew.dto.interest.InterestUpdateRequest;
import com.team4.monew.entity.Interest;
import com.team4.monew.exception.ErrorCode;
import com.team4.monew.exception.MonewException;
import com.team4.monew.repository.InterestRepository;
import com.team4.monew.repository.UserRepository;
import com.team4.monew.service.InterestService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicInterestService implements InterestService {

  private final InterestRepository interestRepository;
  private final UserRepository userRepository;

  @Override
  public InterestDto register(InterestRegisterRequest request) {

    return null;
  }

  @Override
  public CursorPageResponseInterestDto getInterests(String keyword, String orderBy,
      String direction, String cursor, Instant after, int limit, UUID requestUserId) {
    return null;
  }

  @Override
  public InterestDto update(UUID interestId, InterestUpdateRequest request) {
    return null;
  }

  @Override
  public void softDelete(UUID interestId, UUID userId) {

  }

  @Override
  public void hardDelete(UUID interestId, UUID userId) {

  }
}
