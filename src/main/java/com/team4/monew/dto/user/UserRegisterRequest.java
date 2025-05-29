package com.team4.monew.dto.user;

public record UserRegisterRequest(
    String email,
    String nickname,
    String password
) {
}
