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

package com.android.jack.dx.overlapping.dx;

import com.android.jack.dx.overlapping.jack.Data;

import org.junit.Assert;
import org.junit.Test;


/**
 * Tests that generate overlapping.
 */
public class Tests {
  @Test
  public void registerOverlapping001() {
    Assert.assertEquals(1009572021,  new Data().test001(10, 123456789876545321L, 10242048396L));
  }

  @Test
  public void registerOverlapping002() {
    Assert.assertEquals(-1017,  Data.test002(1, 2l, 3));
  }

}
