package com.team4.monew.dto.interest;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record InterestUpdateRequest(
    @NotEmpty(message = "최소 1개의 키워드를 입력해야 합니다.")
    @Size(min = 1, max = 10, message = "키워드는 최소 1개, 최대 10개까지 입력할 수 있습니다.")
    List<String> keywords
) {
}
