package com.team4.monew.exception.comment;

import com.team4.monew.exception.ErrorCode;
import com.team4.monew.exception.MonewException;

public class CommentNotFoundException extends MonewException {
  public CommentNotFoundException() {
    super(ErrorCode.COMMENT_NOT_FOUND);
  }
}
