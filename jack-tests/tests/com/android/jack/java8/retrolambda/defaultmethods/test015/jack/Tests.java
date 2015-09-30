/*
 * Copyright (C) 2015 The Android Open Source Project
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
// Copyright © 2013-2015 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package com.android.jack.java8.retrolambda.defaultmethods.test015.jack;

import org.junit.Assert;
import org.junit.Test;

public class Tests {

  private interface PrimitiveArgs {
    default int sum(boolean bool, byte b, short s, int i, long l, float f, double d, char c) {
        return (int) ((bool ? 1 : 0) + b + s + i + l + f + d + c);
    }
}

  @Test
  public void default_methods_with_primitive_arguments() {
      PrimitiveArgs p = new PrimitiveArgs() {
      };
      Assert.assertEquals(p.sum(true, (byte) 2, (short) 3, 4, 5, 6, 7, (char) 8), 36);
  }

}

