package com.team4.monew.repository;

import com.team4.monew.entity.User;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID>{
  boolean existsByEmail(String email);
}
