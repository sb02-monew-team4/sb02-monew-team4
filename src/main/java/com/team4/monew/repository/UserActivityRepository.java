package com.team4.monew.repository;

import com.team4.monew.entity.UserActivity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserActivityRepository extends MongoRepository<UserActivity, String> {

  Optional<UserActivity> findByUser_UserId(UUID userId);
}
