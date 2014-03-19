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

package com.android.jack.dx.compiler.dx;

import org.junit.Assert;
import org.junit.Test;

import com.android.jack.dx.compiler.jack.Data;


/**
 * Tests bug related to dx.
 */
public class Tests {

  @Test
  public void subInt() {
    Assert.assertEquals(0,  Data.apply());
  }

  @Test
  public void largeFrame() {
    Assert.assertEquals(499500,  Data.largeFrame());
  }

  @Test
  public void getArray() {
    Assert.assertEquals(11,  Data.getArray(5)[0]);
  }
}
