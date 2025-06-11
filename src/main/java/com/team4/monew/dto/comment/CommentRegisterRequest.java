package com.team4.monew.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CommentRegisterRequest(
    @NotNull UUID articleId,
    @NotNull UUID userId,
    @NotBlank String content
) {}
