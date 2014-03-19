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

package com.android.jack.order.dx;

import com.android.jack.order.jack.Order;

import org.junit.Assert;
import org.junit.Test;

/**
 * Evaluation order test.
 */
public class Tests {

  @Test
  public void getValue1() {
    Assert.assertEquals(11,  Order.getValue1(5,6));
  }

  @Test
  public void getValue2() {
    Assert.assertEquals(1,  Order.getValue2(5,6));
  }

  @Test
  public void getValue3() {
    Assert.assertEquals(11,  Order.getValue3());
  }

  @Test
  public void getValue4() {
    Assert.assertEquals(1,  Order.getValue4());
  }

  @Test
  public void getValue5() {
    Assert.assertEquals(1,  Order.getValue5(5));
  }

  @Test
  public void getValue6() {
    Assert.assertEquals(18,  Order.getValue6());
  }
}
