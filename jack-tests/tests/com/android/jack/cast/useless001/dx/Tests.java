/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.jack.cast.useless001.dx;


import com.android.jack.cast.useless001.jack.Data;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests useless cast.
 */
public class Tests {

  @Test
  public void testValue1() {
    Data data = new Data();
    Assert.assertEquals(2, data.getValue1());
  }

  @Test
  public void testValue2() {
    Data data = new Data();
    Assert.assertEquals(3, data.getValue2());
  }

  @Test
  public void getValue3() {
    Data data = new Data();
    Assert.assertEquals(3, data.getValue3());
  }

  @Test
  public void getValue4() {
    Data data = new Data();
    Assert.assertEquals(3, data.getValue4());
  }

  @Test
  public void getValue5() {
    Data data = new Data();
    Assert.assertEquals(3, data.getValue5(new Object[]{null,null,null}));
  }
}
