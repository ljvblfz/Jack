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

package com.android.jack.unary.test002.dx;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

import com.android.jack.unary.test002.jack.Unary;

/**
 * Tests about unary.
 */
public class Tests {
  @Test
  public void postfixDec() {
    Assert.assertEquals(5,  Unary.postfixDec(5));
  }

  @Test
  public void prefixDec() {
    Assert.assertEquals(4,  Unary.prefixDec(5));
  }

  @Test
  public void postfixInc() {
    Assert.assertEquals(5,  Unary.postfixInc(5));
  }

  @Test
  public void prefixInc() {
    Assert.assertEquals(7,  Unary.prefixInc(6));
  }

  @Test
  public void postfixInc2() {
    Assert.assertEquals(6,  Unary.postfixInc2(5));
  }

  @Test
  public void postfixInc3() {
    Assert.assertEquals(4,  Unary.postfixInc3());
  }

  @Test
  public void postfixInc4() {
    Assert.assertEquals(5,  Unary.postfixInc4(new Unary()));
  }

  @Test
  public void postfixIncArray() {
    Assert.assertTrue(Arrays.equals(new int[]{1,3,3}, Unary.postfixIncArray(new int[]{1,2,3})));
  }

  @Test
  public void postfixIncArray2() {
    Unary.tab = new int[]{1,2,3};
    Assert.assertTrue(Arrays.equals(new int[]{1,3,3}, Unary.postfixIncArray2()));
    Assert.assertEquals(12, Unary.sfield2);
  }

  @Test
  public void notIntoMethodCall() {
    Assert.assertTrue(Unary.notIntoMethodCall(false, false));
  }

  @Test
  public void prefixIncIntoMethodCall() {
    Assert.assertEquals('c',  Unary.next());
  }
}
