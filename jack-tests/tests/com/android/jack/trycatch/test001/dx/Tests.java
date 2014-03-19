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

package com.android.jack.trycatch.test001.dx;

import org.junit.Assert;
import org.junit.Test;

import com.android.jack.trycatch.test001.jack.TryCatch;

/**
 * Tests about try/catch.
 */
public class Tests {

  @Test
  public void testDiv() {
    Assert.assertEquals(0,  TryCatch.div(0));
  }

  @Test
  public void testNestedDiv() {
    Assert.assertEquals(0, TryCatch.nestedDiv(0, 0));
    Assert.assertEquals(-1, TryCatch.nestedDiv(1, 0));
    Assert.assertEquals(-2, TryCatch.nestedDiv(0, 1));
  }

  @Test
  public void emptyCatch() {
    Assert.assertEquals(0, TryCatch.emptyCatch(0));
  }

  @Test
  public void nestedTry() {
    Assert.assertEquals(1, TryCatch.nestedTry(0));
  }

  @Test
  public void nestedTry2() {
    Assert.assertEquals(5, TryCatch.nestedTry2(0));
  }

  @Test
  public void nestedTryWithFallThrough() {
    Assert.assertEquals(8, TryCatch.nestedTryWithFallThrough(0));
  }

  @Test
  public void similarCatches() {
    Assert.assertEquals(1, TryCatch.similarCatches());
  }
}
