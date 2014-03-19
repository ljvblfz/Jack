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

package com.android.jack.tryfinally.finallyblock.dx;

import org.junit.Assert;
import org.junit.Test;

import com.android.jack.tryfinally.finallyblock.jack.B;
import com.android.jack.tryfinally.finallyblock.jack.Branching;
import com.android.jack.tryfinally.finallyblock.jack.Catch;
import com.android.jack.tryfinally.finallyblock.jack.EmptyBlocks;
import com.android.jack.tryfinally.finallyblock.jack.Nested;

/**
 * Tests about finally.
 */
public class Tests {

  @Test
  public void branching1() {
    Assert.assertEquals(2 * 5, Branching.returnInFinally1());
    Assert.assertEquals(2 * 5, Branching.returnInFinally2());
    Assert.assertEquals(2 * 3 * 5, Branching.returnInFinally3());
    Assert.assertEquals(2 * 3, Branching.returnInTry1());
    Assert.assertEquals(2 * 7, Branching.returnInCatch1());
    Assert.assertEquals("A", Branching.returnInTry2());
    Assert.assertEquals(2 * 3, Branching.returnInTry3(new B()).field);
    Assert.assertEquals(-1, Branching.breakInFinally1());
    Assert.assertEquals(4, Branching.continueInFinally1());
    Assert.assertEquals(27, Branching.returnInNestedTry(true));
  }

  @Test
  public void catch1() {
    Assert.assertEquals(2 * 3 * 5, Catch.tryCatchFinally1());
    Assert.assertEquals(2 * 5, Catch.tryCatchFinally2());
  }

  @Test
  public void catch2() {
    Assert.assertEquals(2 * 3 * 7 * 13, Catch.shouldNotCatch1());
    Assert.assertEquals(2 * 3 * 7 * 23, Catch.shouldNotCatch2());
    try {
      Catch.shouldNotCatch3();
      Assert.fail();
    } catch (NullPointerException e) {
      // OK
    }
    Assert.assertEquals(0, Catch.value);
  }

  @Test
  public void nested1() {
    Assert.assertEquals(2 * 23 * 7 * 19 * 13 * 17 * 5, Nested.inTry1());
    Assert.assertEquals(2 * 23 * 7 * 11 * 13 * 17 * 5, Nested.inTry2());
    Assert.assertEquals(2 * 23 * 7 * 13 * 3 * 5, Nested.inTry3());
    Assert.assertEquals(2 * 23 * 7 * 11 * 13 * 3 * 5, Nested.inTry4());
    Assert.assertEquals(2 * 19 * 5, Nested.inCatch1());
    Assert.assertEquals(2 * 3 * 7 * 13 * 17 * 5, Nested.inCatch2());
    Assert.assertEquals(2 * 3 * 7 * 11 * 13 * 17 * 5, Nested.inCatch3());
    Assert.assertEquals(2 * 3 * 7 * 11 * 13 * 5 * 31, Nested.inCatch4());
    Assert.assertEquals(2 * 23 * 5 * 7 * 19 * 13 * 17, Nested.inFinally1());
    Assert.assertEquals(2 * 23 * 5 * 7 * 11 * 13 * 17, Nested.inFinally2());
    Assert.assertEquals(2 * 23 * 5 * 7 * 19 * 13 * 17, Nested.inFinally6());
    Assert.assertEquals(2 * 23 * 5 * 7 * 11 * 13 * 17, Nested.inFinally7());
  }

  @Test
  public void nested2() {
    Assert.assertEquals(2 * 23 * 7 * 29 * 13 * 3 * 5, Nested.inTry5());
    Assert.assertEquals(2 * 23 * 7 * 11 * 13 * 3 * 5, Nested.inTry6());
    Assert.assertEquals(2 * 3 * 7 * 13 * 5 * 31, Nested.inCatch5());
    Assert.assertEquals(2 * 3 * 7 * 11 * 13 * 5 * 31, Nested.inCatch6());
    Assert.assertEquals(2 * 23 * 5 * 7 * 11 * 13 * 31, Nested.inFinally3());
    Assert.assertEquals(2 * 23 * 5 * 7 * 19 * 13 * 31, Nested.inFinally4());
    Assert.assertEquals(2 * 23 * 5 * 7 * 11 * 13 * 31, Nested.inFinally5());
    Assert.assertEquals(2 * 23 * 5 * 7 * 11 * 13 * 31, Nested.inFinally8());
    Assert.assertEquals(2 * 23 * 5 * 7 * 19 * 13 * 31, Nested.inFinally9());
    Assert.assertEquals(2 * 23 * 5 * 7 * 11 * 13 * 31, Nested.inFinally10());
  }

  @Test
  public void emptyBlocks() {
    Assert.assertEquals(5, EmptyBlocks.emptyTry());
    Assert.assertEquals(2 * 3 * 5, EmptyBlocks.emptyCatch1());
    Assert.assertEquals(2 * 5, EmptyBlocks.emptyCatch2());
    Assert.assertEquals(2 * 3, EmptyBlocks.emptyFinally1());
    Assert.assertEquals(2 * 5, EmptyBlocks.emptyFinally2());
    Assert.assertEquals(1, EmptyBlocks.allEmpty());
  }

  @Test
  public void finallyWithNew() {
    Assert.assertEquals(2, Catch.finallyWithNew());
  }

  @Test
  public void loopIntoTryFinally() {
    Assert.assertEquals(6, Branching.loopIntoTryFinally());
  }

  @Test
  public void nestedFinally() {
    Assert.assertEquals(7, Nested.inFinally11());
  }
}
