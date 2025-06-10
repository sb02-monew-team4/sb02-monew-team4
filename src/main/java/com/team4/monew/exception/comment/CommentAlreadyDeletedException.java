package com.team4.monew.exception.comment;

import com.team4.monew.exception.ErrorCode;
import com.team4.monew.exception.MonewException;

public class CommentAlreadyDeletedException extends MonewException {
  public CommentAlreadyDeletedException() {
    super(ErrorCode.COMMENT_ALREADY_DELETED);
  }
}
