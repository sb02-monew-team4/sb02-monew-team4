package com.team4.monew.asynchronous.event.subscription;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SubscriptionDeletedEvent {

  private final UUID userId;
  private final UUID subscriptionId;
}
