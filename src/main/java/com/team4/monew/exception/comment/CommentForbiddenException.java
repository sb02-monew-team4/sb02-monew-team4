package com.team4.monew.exception.comment;

import com.team4.monew.exception.ErrorCode;
import com.team4.monew.exception.MonewException;

public class CommentForbiddenException extends MonewException {
  public CommentForbiddenException() {
    super(ErrorCode.COMMENT_FORBIDDEN);
  }
}
