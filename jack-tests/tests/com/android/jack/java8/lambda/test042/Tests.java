/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an AS IS BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.jack.java8.lambda.test042;

import junit.framework.Assert;

import org.junit.Test;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

interface I {
  int getValue();
}


public class Tests {

  public static int move(I i) {
    return i.getValue();
  }

  final public static int getValue(int arg1) {
    class LocalClass {
      public int getValue() {
        String checkCaptureOder = "str";
        I i = () -> move(() -> {return checkCaptureOder.length() + arg1;});
        return i.getValue();
      }
    }
    return new LocalClass().getValue();
  }

  @Test
  public void test001() {
    Assert.assertEquals(13, Tests.getValue(10));
  }
}
