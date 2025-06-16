package com.team4.monew.repository;

import com.team4.monew.entity.UserActivity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface UserActivityRepository extends MongoRepository<UserActivity, String> {

  Optional<UserActivity> findByUser_Id(UUID userId);

  boolean existsByUser_Id(UUID userId);

  void deleteByUser_Id(UUID userId);

  @Query("{ 'subscriptionDtos.interestId': ?0 }")
  List<UserActivity> findBySubscriptionDtosInterestId(UUID interestId);
}
