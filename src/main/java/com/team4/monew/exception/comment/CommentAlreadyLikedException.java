package com.team4.monew.exception.comment;

import com.team4.monew.exception.ErrorCode;
import com.team4.monew.exception.MonewException;

public class CommentAlreadyLikedException extends MonewException {
  public CommentAlreadyLikedException() {
    super(ErrorCode.COMMENT_ALREADY_LIKED);
  }
}
