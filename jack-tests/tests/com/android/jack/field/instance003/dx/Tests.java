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

package com.android.jack.field.instance003.dx;

import com.android.jack.field.instance003.jack.Data;

import junit.framework.Assert;

import org.junit.Test;

public class Tests {

  @Test
  public void test1() {
    Data d = new Data();
    Assert.assertTrue(d.check());
    Assert.assertTrue(d.check2());
    Assert.assertTrue(d.check3());
    Assert.assertTrue(d.check4());
    Assert.assertTrue(d.check5());
    Assert.assertTrue(d.check6());
  }
}
