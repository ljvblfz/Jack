/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.jack.throwstatement.test001.dx;

import org.junit.Assert;
import org.junit.Test;

import com.android.jack.throwstatement.test001.jack.ThrowStmt;

/**
 * Tests about throw.
 */
public class Tests {

  private class MyException extends RuntimeException {

    private static final long serialVersionUID = 1L;
  }

  private class MySecondException extends RuntimeException {

    private static final long serialVersionUID = 1L;
  }

  @Test
  public void throwException() {
    try {
      ThrowStmt.throwException(new MyException());
      Assert.fail();
    } catch (MyException e) {
      // Ok
    }
  }

  @Test
  public void reThrowException() {
    try {
      ThrowStmt.reThrowException(new MyException());
      Assert.fail();
    } catch (MyException e) {
      // Ok
    }
  }

  @Test
  public void reThrowSecondException() {
    try {
      ThrowStmt.reThrowException(new MyException(), new MySecondException());
      Assert.fail();
    } catch (MySecondException e) {
      // Ok
    }
  }
}
