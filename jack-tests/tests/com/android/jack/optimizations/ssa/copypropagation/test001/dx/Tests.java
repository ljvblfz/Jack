/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.android.jack.optimizations.ssa.copypropagation.test001.dx;

import com.android.jack.optimizations.ssa.copypropagation.test001.jack.CopyPropagation;
import com.android.jack.optimizations.ssa.copypropagation.test001.jack.CopyPropagation.Testing;

import junit.framework.Assert;

import org.junit.Test;

public class Tests {

  @Test
  public void test001() {
    Testing a = new Testing(99);
    Testing b = new Testing(3);
    Testing c = new Testing(4);
    Testing d = new Testing(5);

    a.next = b;
    b.next = c;
    c.next = d;
    d.next = a;

    CopyPropagation cp = new CopyPropagation();
    Assert.assertEquals(3 + 4 + 5, cp.sum(a));
    Assert.assertEquals(4 + 5 + 99, cp.sum(b));
    Assert.assertEquals(5 + 99 + 3, cp.sum(c));
    Assert.assertEquals(99 + 3 + 4, cp.sum(d));
  }
}
