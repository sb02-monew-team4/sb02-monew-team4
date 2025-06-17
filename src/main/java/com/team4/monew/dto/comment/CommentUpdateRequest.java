package com.team4.monew.dto.comment;

import jakarta.validation.constraints.NotBlank;

public record CommentUpdateRequest(
    @NotBlank String content
) {}
