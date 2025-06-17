package com.team4.monew.exception.comment;

import com.team4.monew.exception.ErrorCode;
import com.team4.monew.exception.MonewException;

public class CommentLikeNotAllowedException extends MonewException {
  public CommentLikeNotAllowedException() {
    super(ErrorCode.COMMENT_LIKE_NOT_ALLOWED);
  }
}
