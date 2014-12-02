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

package com.android.jack.threeaddress.test001.dx;

import com.android.jack.threeaddress.test001.jack.ThreeAddressCode001;
import com.android.jack.threeaddress.test001.jack.ThreeAddressCode002;
import com.android.jack.threeaddress.test001.jack.ThreeAddressCode003;

import junit.framework.Assert;

import org.junit.Test;

public class Tests {

  @Test
  public void multiAdd_001() {
    Assert.assertEquals(6, ThreeAddressCode001.threadAddressCode001());
  }

  @Test
  public void multiAdd_002() {
    Assert.assertEquals(6, ThreeAddressCode001.threadAddressCode002(1, 2, 3));
  }

  @Test
  public void arrayAdd_001() {
    int [] a = {5, 6, 7};
    int [] b = {7, 8, 9};
    int [] c = {10, 11, 12};
    Assert.assertEquals(25, ThreeAddressCode001.threadAddressCode003(a, b, c));
  }

  @Test
  public void methodCall_001() {
    Assert.assertEquals(1, ThreeAddressCode001.threadAddressCode004(7));
  }

  @Test
  public void anonymous_001() {
    Assert.assertEquals(17, ThreeAddressCode001.threeAddressCode005(3, 5));
  }

  @Test
  public void conditional_001() {
    Assert.assertEquals(1, ThreeAddressCode001.test_conditionalCode001(3));
    Assert.assertEquals(-1, ThreeAddressCode001.test_conditionalCode001(-3));
    Assert.assertEquals(1, ThreeAddressCode001.test_conditionalCode001(Integer.MAX_VALUE));
    Assert.assertEquals(-1, ThreeAddressCode001.test_conditionalCode001(Integer.MIN_VALUE));
  }

  @Test
  public void conditional_002() {
    Assert.assertEquals(3, ThreeAddressCode001.test_conditionalCode002(2, 3));
    Assert.assertEquals(3, ThreeAddressCode001.test_conditionalCode002(3, 2));
    Assert.assertEquals(-1, ThreeAddressCode001.test_conditionalCode002(-2, 3));
    Assert.assertEquals(-1, ThreeAddressCode001.test_conditionalCode002(-2, -3));
  }

  @Test
  public void conditional_003() {
    Assert.assertEquals(1, ThreeAddressCode001.test_conditionalCode003(2, 3));
    Assert.assertEquals(1, ThreeAddressCode001.test_conditionalCode003(3, 2));
    Assert.assertEquals(3, ThreeAddressCode001.test_conditionalCode003(-2, 3));
    Assert.assertEquals(3, ThreeAddressCode001.test_conditionalCode003(-2, -3));
  }

  @Test
  public void conditional_004() {
    Assert.assertEquals(-1, ThreeAddressCode001.test_conditionalCode004(1, 2, 3, 4, 5, 6, 7));
    Assert.assertEquals(6, ThreeAddressCode001.test_conditionalCode004(1, 6, 30, 4, 6, 6, 7));
    Assert.assertEquals(30, ThreeAddressCode001.test_conditionalCode004(30, 6, 30, 4, 6, 6, 7));
    Assert.assertEquals(4, ThreeAddressCode001.test_conditionalCode004(3, 6, 2, 1, 30, 30, 4));
  }

  @Test
  public void conditional_005() {
    Assert.assertEquals(1, ThreeAddressCode001.test_conditionalCode005(1, 2));
  }

  @Test
  public void multiAssign_001() {
    Assert.assertEquals(4, ThreeAddressCode002.threadAddressCode002());
  }

  @Test
  public void withoutBlock_001() {
    Assert.assertEquals(3, ThreeAddressCode002.threadAddressWithoutBlock(1, 2, 5));
  }

  @Test
  public void assignIntoExpr001() {
    Assert.assertEquals(1, ThreeAddressCode003.assignIntoExpr001());
  }

  @Test
  public void assignIntoExpr002() {
    Assert.assertEquals(2, ThreeAddressCode003.assignIntoExpr002());
  }

  @Test
  public void assignIntoExpr003() {
    Assert.assertEquals(1, ThreeAddressCode003.assignIntoExpr003());
  }

  @Test
  public void assignIntoExpr004() {
    Assert.assertEquals(3, ThreeAddressCode003.assignIntoExpr004());
  }

  @Test
  public void testFromLibCore() {
    Assert.assertEquals(16, ThreeAddressCode003.test());
  }

  @Test
  public void incIntoMethodCall() {
    Assert.assertEquals('c', new ThreeAddressCode003().next());
  }
}
