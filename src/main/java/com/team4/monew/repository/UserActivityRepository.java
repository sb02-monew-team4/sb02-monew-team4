package com.team4.monew.repository;

import com.team4.monew.entity.UserActivity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserActivityRepository extends JpaRepository<UserActivity, UUID> {

  Optional<UserActivity> findByUserId(UUID userId);
}
