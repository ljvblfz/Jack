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

package com.android.jack.inner.test026.dx;

import com.android.jack.inner.test026.jack.D;

import org.junit.Assert;
import org.junit.Test;

public class Tests {

  @Test
  public void test() {
    D d = new D();
    Assert.assertEquals(1, d.Dvalue);
    Assert.assertEquals(1, d.Evalue);
    Assert.assertEquals(1, d.Fvalue);
    Assert.assertEquals(2, d.Gvalue);
    Assert.assertEquals(1, d.G2value);
    Assert.assertEquals(2, d.G3value);
  }
}
