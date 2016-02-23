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

package com.android.jack.java8.staticmethod.test003.jack;

import org.junit.Assert;
import org.junit.Test;

enum Enum {
  ONE, TWO, THREE
}


interface I {
  static int getValue(Enum e) {
    switch (e) {
      case ONE: {
        return 1;
      }
      case TWO: {
        return 2;
      }
      case THREE: {
        return 3;
      }
    }

    return 0;
  }
}


/**
 * Static method with usage of enum fields into a switch statement.
 */
public class Tests {

  @Test
  public void test001() {
    Assert.assertEquals(1, I.getValue(Enum.ONE));
    Assert.assertEquals(2, I.getValue(Enum.TWO));
    Assert.assertEquals(3, I.getValue(Enum.THREE));
  }
}
