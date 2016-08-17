/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.jack.tryfinally.finally007.jack;

public class Finally007 {

  public void canThrow() throws NullPointerException, AssertionError {
    throw new AssertionError();
  }

  class Result {
    int f = 0;
  }

  @SuppressWarnings("finally")
  public int get(int value) {
    Result result = new Result();
    try {
      try {
        canThrow();
      } catch (NullPointerException e) {
        result.f = 1;
      } catch (AssertionError e) {
        result.f = 2;
      } finally {
      }
    } finally {
      return result.f;
    }
  }
}
