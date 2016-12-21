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

package com.android.jack.conditional.test009.jack;

import org.junit.Assert;
import org.junit.Test;

public class ConditionalTest009 {
  @Test
  public void test001() {
    Assert.assertEquals(35312635, get());
  }

  @Test
  public void test002() {
    Assert.assertEquals(35312635, get2());
  }

  @SuppressWarnings("unused")
  public int get() {
    int i = 35312635;
    int s = 702730947;
    i >>>= (int) (true ? s : -1284717918.0f);
    return i;
  }

  @SuppressWarnings("cast")
  public int get2() {
    int i = 35312635;
    i >>>= (int) (float) 702730947;
    return i;
  }
}
