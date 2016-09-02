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

package com.android.jack.conditional.test001.dx;

import com.android.jack.conditional.test001.jack.Conditional;

import junit.framework.Assert;

import org.junit.Test;

public class Tests {

  @Test
  public void test1() {
    Assert.assertEquals(1, Conditional.test_conditionalCode001(15));
    Assert.assertEquals(-1, Conditional.test_conditionalCode001(-15));
    Assert.assertEquals(15, Conditional.test_conditionalCode002(15, -15));
    Assert.assertEquals(1, Conditional.test_conditionalCode003(15, -15));
    Assert.assertEquals(-1, Conditional.test_conditionalCode004(1, 2, 3, 3, 5, 8, 6));
    Assert.assertEquals(1, Conditional.test_conditionalCode005(15, -15));
    Assert.assertEquals(1, Conditional.test_conditionalCode006(true));
    Assert.assertEquals(-1, Conditional.test_conditionalCode006(false));

    Assert.assertEquals(4, Conditional.test_conditionalCode007(false));
    Assert.assertEquals(5, Conditional.test_conditionalCode008(false));
    Assert.assertEquals(5, Conditional.test_conditionalCode009(false));
    Assert.assertEquals(3, Conditional.test_conditionalCode010(false));
    Assert.assertEquals(4, Conditional.test_conditionalCode011(false));
    Assert.assertEquals(1, Conditional.test_conditionalCode012(false));
    Assert.assertEquals(2, Conditional.test_conditionalCode013(false));
    Assert.assertEquals(4, Conditional.test_conditionalCode014(false));
    Assert.assertEquals(3, Conditional.test_conditionalCode015(false));
    Assert.assertEquals(4, Conditional.test_conditionalCode016(false));
    Assert.assertEquals(6, Conditional.test_conditionalCode017(false));
    Assert.assertEquals(6, Conditional.test_conditionalCode018(false));
    Assert.assertEquals(6, Conditional.test_conditionalCode019());
    Assert.assertEquals(5, Conditional.test_conditionalCode020(false));
    Assert.assertEquals(5, Conditional.test_conditionalCode021(false));
    Assert.assertEquals(9, Conditional.test_conditionalCode022(false));
    Assert.assertEquals(9, Conditional.test_conditionalCode023(false));
    Assert.assertEquals(9, Conditional.test_conditionalCode024());
    Assert.assertEquals(7, Conditional.test_conditionalCode025(false));
    Assert.assertEquals(7, Conditional.test_conditionalCode026(false));
    Assert.assertEquals(8, Conditional.test_conditionalCode027(false));
    Assert.assertEquals(8, Conditional.test_conditionalCode028(false));
    Assert.assertEquals(8, Conditional.test_conditionalCode029());
    Assert.assertEquals(8, Conditional.test_conditionalCode030(false));
  }
}
