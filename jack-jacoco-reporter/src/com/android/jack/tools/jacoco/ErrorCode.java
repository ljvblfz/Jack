package com.android.jack.tools.jacoco;

import javax.annotation.Nonnegative;

/**
 * An enumeration of possible error codes.
 */
public enum ErrorCode {
  USAGE_ERROR(1),
  INERNAL_ERROR(2);

  @Nonnegative
  private final int errorCode;

  private ErrorCode(@Nonnegative int errorCode) {
    assert errorCode > 0;
    this.errorCode = errorCode;
  }

  @Nonnegative
  public int getErrorCode() {
    return errorCode;
  }
}

