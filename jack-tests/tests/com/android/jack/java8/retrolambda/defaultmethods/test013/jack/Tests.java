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

package com.android.jack.java8.retrolambda.defaultmethods.test013.jack;

import org.junit.Assert;
import org.junit.Test;

public class Tests {

  private interface Primitives {
    default boolean getBoolean() {
        return true;
    }

    default byte getByte() {
        return 2;
    }

    default short getShort() {
        return 3;
    }

    default int getInt() {
        return 4;
    }

    default long getLong() {
        return 5L;
    }

    default float getFloat() {
        return 6.0f;
    }

    default double getDouble() {
        return 7.0;
    }

    default char getChar() {
        return 'a';
    }
}

  @Test
  public void default_methods_of_primitive_type() {
      Primitives p = new Primitives() {
      };
      Assert.assertEquals("boolean", p.getBoolean(), true);
      Assert.assertEquals("byte", p.getByte(), (byte) 2);
      Assert.assertEquals("short", p.getShort(), (short) 3);
      Assert.assertEquals("int", p.getInt(), 4);
      Assert.assertEquals("long", p.getLong(), 5L);
      Assert.assertEquals("float", p.getFloat(), 6.0f, 0);
      Assert.assertEquals("double", p.getDouble(), 7.0, 0);
      Assert.assertEquals("char", p.getChar(), 'a');
  }

}

