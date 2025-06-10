package com.team4.monew.asynchronous.event.subscription;

import com.team4.monew.entity.Subscription;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SubscriptionUpdatedEvent {
  private final UUID userId;
  private final Subscription subscription;
}
