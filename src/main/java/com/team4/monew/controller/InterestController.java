package com.team4.monew.controller;

import com.team4.monew.dto.interest.CursorPageResponseInterestDto;
import com.team4.monew.dto.interest.InterestDto;
import com.team4.monew.dto.interest.InterestRegisterRequest;
import com.team4.monew.dto.interest.InterestUpdateRequest;
import com.team4.monew.dto.interest.SubscriptionDto;
import com.team4.monew.exception.ErrorCode;
import com.team4.monew.exception.MonewException;
import com.team4.monew.service.basic.BasicInterestService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/interests")
public class InterestController {

  private final BasicInterestService interestService;

  @PostMapping
  public ResponseEntity<InterestDto> register(
      @Valid @RequestBody InterestRegisterRequest request,
      HttpServletRequest servletRequest
  ) {
    UUID authenticatedUserId = (UUID) servletRequest.getAttribute("authenticatedUserId");

    InterestDto response = interestService.register(authenticatedUserId, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PatchMapping("/{interestId}")
  public ResponseEntity<InterestDto> update(
      @PathVariable UUID interestId,
      @Valid @RequestBody InterestUpdateRequest request
  ) {
    InterestDto response = interestService.update(interestId, request);
    return ResponseEntity.ok(response);
  }

  @GetMapping
  public ResponseEntity<CursorPageResponseInterestDto> getInterests(
      @RequestParam(required = false) String keyword,
      @RequestParam(defaultValue = "name") String orderBy,
      @RequestParam(defaultValue = "ASC") String direction,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) String after,
      @RequestParam(defaultValue = "10") int limit,
      @RequestHeader("Monew-Request-User-ID") UUID requesterId
  ) {

    if (!orderBy.equals("name") && !orderBy.equals("subscriberCount")) {
      throw new MonewException(ErrorCode.INVALID_ORDER_BY);
    }

    if (!direction.equalsIgnoreCase("ASC") && !direction.equalsIgnoreCase("DESC")) {
      throw new MonewException(ErrorCode.INVALID_SORT_DIRECTION);
    }

    if (limit < 1 || limit > 100) {
      throw new MonewException(ErrorCode.INVALID_LIMIT);
    }

    CursorPageResponseInterestDto response = interestService.getInterests(
        keyword, orderBy, direction, cursor, after, limit, requesterId
    );
    return ResponseEntity.ok(response);
  }

  @PostMapping("/{interestId}/subscriptions")
  public ResponseEntity<SubscriptionDto> subscribeInterest(
      @PathVariable UUID interestId,
      HttpServletRequest request
  ) {
    UUID authenticatedUserId = (UUID) request.getAttribute("authenticatedUserId");
    SubscriptionDto response = interestService.subscribeInterest(interestId, authenticatedUserId);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{interestId}/subscriptions")
  public ResponseEntity<Void> unsubscribeInterest(
      @PathVariable UUID interestId,
      HttpServletRequest request
  ) {
    UUID authenticatedUserId = (UUID) request.getAttribute("authenticatedUserId");
    interestService.unsubscribeInterest(interestId, authenticatedUserId);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{interestId}")
  public ResponseEntity<Void> hardDelete(
      @PathVariable UUID interestId,
      HttpServletRequest request
  ) {
    UUID authenticatedUserId = (UUID) request.getAttribute("authenticatedUserId");
    interestService.hardDelete(interestId, authenticatedUserId);
    return ResponseEntity.noContent().build();
  }
}
