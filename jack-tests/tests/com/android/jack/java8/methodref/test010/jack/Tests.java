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

package com.android.jack.java8.methodref.test010.jack;

import org.junit.Assert;
import org.junit.Test;

interface IntOp {
  int apply(int i);
}

interface I {
  char charAt0(String str);
}

/**
 * Check that cast it correctly generated for the lambda representing string::charAt
 */
public class Tests {

  public char test(IntOp op) {
    return (char) op.apply(0);
  }

  @Test
  public void test001() {
    I i = string -> {
      return test(string::charAt);
    };
    Assert.assertEquals('A', i.charAt0("ABC"));
  }
}
