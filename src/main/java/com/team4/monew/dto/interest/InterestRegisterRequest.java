package com.team4.monew.dto.interest;

import java.util.List;

public record InterestRegisterRequest(
    String name,
    List<String> keywords
) {
}
