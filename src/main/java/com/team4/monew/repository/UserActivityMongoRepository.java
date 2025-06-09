package com.team4.monew.repository;

import com.team4.monew.entity.UserActivityDocument;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserActivityMongoRepository extends MongoRepository<UserActivityDocument, String> {

  Optional<UserActivityDocument> findByUser_UserId(UUID userId);
}
