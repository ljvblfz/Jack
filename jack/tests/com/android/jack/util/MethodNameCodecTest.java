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

package com.android.jack.util;

import com.android.sched.util.codec.CodecContext;
import com.android.sched.util.codec.ParsingException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nonnull;

public class MethodNameCodecTest {
  private MethodNameCodec codec;
  private CodecContext context;

  @Before
  public void setUp() {
    codec = new MethodNameCodec();
    context = new CodecContext();
  }

  @Test
  public void test() {
    checkSuccess("a.b.c#d");
    checkSuccess("c#d");
    checkSuccess(" a.b.c#d");
    checkSuccess("a.b.c#d ");
    checkSuccess(" a.b.c#d ");

    checkFailure("a", "has no '#'");
    checkFailure("#", "class name is empty");
    checkFailure("#d", "class name is empty");
    checkFailure("a#", "method name is empty");
    checkFailure("a##", "has several '#'");
    checkFailure("a#d#e", "has several '#'");
    checkFailure("##d", "class name is empty");
    checkFailure("0#d", "class name '0' is not a valid Java identifier");
    checkFailure("a.0#d", "class name '0' is not a valid Java identifier");
    checkFailure("0.b#d", "package #1 name '0' is not a valid Java identifier");
    checkFailure("a.0.c#d", "package #2 name '0' is not a valid Java identifier");
    checkFailure("a..c#d", "package #2 name is empty");
    checkFailure("a.b.#d", "class name is empty");
    checkFailure("a#0", "method name '0' is not a valid Java identifier");
    checkFailure("c#d()", "method name 'd()' is not a valid Java identifier");
    checkFailure("c#d.e", "method name 'd.e' is not a valid Java identifier");
    checkFailure("c #d", "class name 'c ' is not a valid Java identifier");
    checkFailure("c# d", "method name ' d' is not a valid Java identifier");
  }

  private void checkSuccess(@Nonnull String string) {
    try {
      codec.checkString(context, string);
    } catch (ParsingException e) {
      Assert.fail(e.getMessage());
    }
  }

  private void checkFailure(@Nonnull String string, @Nonnull String expected) {
    try {
      codec.checkString(context, string);
      Assert.fail();
    } catch (ParsingException e) {
      Assert.assertEquals(
          "The value must be a fully qualified java method name (e.g. java.lang.Object#toString) but "
              + expected,
          e.getMessage());
    }
  }
}
