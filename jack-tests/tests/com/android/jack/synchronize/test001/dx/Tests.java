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

package com.android.jack.synchronize.test001.dx;

import org.junit.Assert;
import org.junit.Test;

import com.android.jack.synchronize.test001.jack.Synchronize;

/**
 * Tests Synchronization.
 */
public class Tests {
  @Test
  public void lockUnlockWithSameObject() {
    Assert.assertEquals(1,  Synchronize.syncBlock());
  }

  @Test
  public void waitFor() {
    Assert.assertEquals(1,  Synchronize.waitFor());
  }

  @Test
  public void exceptionRange() {
    Assert.assertEquals(1, Synchronize.exceptionRange(new Integer(1)));
    try {
      Synchronize.exceptionRange(new Integer(0));
      Assert.fail();
    } catch (IllegalArgumentException e) {
      // Ok
    }
  }

  @Test
  public void exceptionRange2() {
    Synchronize.result = 0;
    Synchronize.exceptionRange2();
    Assert.assertEquals(3,  Synchronize.result);
  }

  @Test
  public void synchronizeIntoACatch() {
    Synchronize.result = 0;
    Synchronize.synchronizeIntoACatch();
    Assert.assertEquals(1,  Synchronize.result);
  }
}
