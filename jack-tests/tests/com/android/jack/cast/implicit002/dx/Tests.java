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

package com.android.jack.cast.implicit002.dx;


import com.android.jack.cast.implicit002.jack.Data;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests about arithmetic.
 */
public class Tests {

  @Test
  public void test1() {
    boolean cond = true;
    boolean cond2 = false;
    byte b = 13;
    short s = 14;
    char c = 15;
    int i = 16;
    long l = 17;
    float f = 0.18f;
    double d = 0.19;
    Assert.assertEquals(cond ? b : s, Data.conditonalBS(cond, b, s));
    Assert.assertEquals(cond ? l : b, Data.conditonalLB(cond, l, b));
    Assert.assertEquals(cond ? b : b, Data.conditonalBB(cond, b, b));
    Assert.assertEquals(cond ? i : b, Data.conditonalIB(cond, i, b));
    Assert.assertEquals(cond ? b : d, Data.conditonalBD(cond, b, d), 0.0);
    Assert.assertEquals(cond ? i : d, Data.conditonalID(cond, i, d), 0.0);
    Assert.assertEquals(cond ? l : d, Data.conditonalLD(cond, l, d), 0.0);
    Assert.assertEquals(cond ? i : f, Data.conditonalIF(cond, i, f), 0.0);
    
    Assert.assertEquals(cond2 ? b : s, Data.conditonalBS(cond2, b, s));
    Assert.assertEquals(cond2 ? l : b, Data.conditonalLB(cond2, l, b));
    Assert.assertEquals(cond2 ? b : b, Data.conditonalBB(cond2, b, b));
    Assert.assertEquals(cond2 ? i : b, Data.conditonalIB(cond2, i, b));
    Assert.assertEquals(cond2 ? b : d, Data.conditonalBD(cond2, b, d), 0.0);
    Assert.assertEquals(cond2 ? i : d, Data.conditonalID(cond2, i, d), 0.0);
    Assert.assertEquals(cond2 ? l : d, Data.conditonalLD(cond2, l, d), 0.0);
    Assert.assertEquals(cond2 ? i : f, Data.conditonalIF(cond2, i, f), 0.0);
  }
}
