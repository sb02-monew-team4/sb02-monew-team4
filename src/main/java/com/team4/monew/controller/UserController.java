package com.team4.monew.controller;

import com.team4.monew.auth.OwnerCheck;
import com.team4.monew.dto.user.UserDto;
import com.team4.monew.dto.user.UserRegisterRequest;
import com.team4.monew.dto.user.UserUpdateRequest;
import com.team4.monew.service.UserService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {

  private final UserService userService;

  @PostMapping
  public ResponseEntity<UserDto> register(
      @RequestBody @Valid UserRegisterRequest request
  ) {
    UserDto registeredUser = userService.register(request);
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(registeredUser);
  }

  @OwnerCheck
  @PatchMapping("/{userId}")
  public ResponseEntity<UserDto> update(
      @PathVariable("userId") UUID userId,
      @RequestBody @Valid UserUpdateRequest request
  ) {
    UserDto updatedUser = userService.update(userId, request);
    return ResponseEntity.ok(updatedUser);
  }

  @OwnerCheck
  @DeleteMapping("/{userId}")
  public ResponseEntity<Void> softDelete(
      @PathVariable("userId") UUID userId
  ) {
    userService.softDelete(userId);
    return ResponseEntity.noContent().build();
  }

  @OwnerCheck
  @DeleteMapping("/{userId}/hard")
  public ResponseEntity<Void> hardDelete(
      @PathVariable("userId") UUID userId
  ) {
    userService.hardDelete(userId);
    return ResponseEntity.noContent().build();
  }
}
