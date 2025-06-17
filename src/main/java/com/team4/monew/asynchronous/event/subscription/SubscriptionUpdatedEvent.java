package com.team4.monew.asynchronous.event.subscription;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SubscriptionUpdatedEvent {

  private final UUID interestId;
  private final List<String> newKeywords;
}