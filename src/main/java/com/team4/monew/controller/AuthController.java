package com.team4.monew.controller;

import com.team4.monew.dto.user.UserDto;
import com.team4.monew.dto.user.UserLoginRequest;
import com.team4.monew.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class AuthController {

  private final AuthService authService;

  @PostMapping
  public ResponseEntity<UserDto> login(
      @RequestBody @Valid UserLoginRequest request
  ) {
    UserDto response = authService.login(request);
    return ResponseEntity.ok()
        .header("MoNew-Request-User-ID", response.id().toString())
        .body(response);
  }

}
