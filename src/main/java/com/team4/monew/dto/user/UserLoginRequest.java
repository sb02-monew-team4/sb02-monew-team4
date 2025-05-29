package com.team4.monew.dto.user;

public record UserLoginRequest(
    String email,
    String password
) {
}
