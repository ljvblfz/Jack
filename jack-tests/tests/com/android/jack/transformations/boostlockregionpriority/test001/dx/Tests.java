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

package com.android.jack.transformations.boostlockregionpriority.test001.dx;

import com.android.jack.transformations.boostlockregionpriority.test001.jack.LockedRegion;
import com.android.jack.transformations.boostlockregionpriority.test001.jack.Request;
import com.android.jack.transformations.boostlockregionpriority.test001.jack.Reset;

import org.junit.Assert;
import org.junit.Test;

public class Tests {
  @Test
  public void testLockThisBoost() {
    LockedRegion r = new LockedRegion();
    r.invokeLockThis();
    Assert.assertEquals(1, Request.count);
    Assert.assertEquals(1, Reset.count);
    Assert.assertEquals(1, LockedRegion.count);
  }

  @Test
  public void testLockThisBoostFourTimes() {
    LockedRegion r = new LockedRegion();
    r.invokeLockThisFourTimes();
    Assert.assertEquals(4, Request.count);
    Assert.assertEquals(4, Reset.count);
    Assert.assertEquals(4, LockedRegion.count);
  }

  /**
   * Make sure that reset is called even if there might be an exception.
   */
  @Test
  public void testLockThisBoostException() {
    boolean hasException = false;
    LockedRegion r = new LockedRegion();
    try {
      r.invokeLockThisException();
    } catch (RuntimeException e) {
      hasException = true;
    }
    Assert.assertTrue(hasException);
    Assert.assertEquals(1, Request.count);
    Assert.assertEquals(1, Reset.count);
    Assert.assertEquals(1, LockedRegion.count);
  }

  @Test
  public void testLockObjectNoBoost() {
    LockedRegion r = new LockedRegion();
    r.invokeLockObject();
    Assert.assertEquals(0, Request.count);
    Assert.assertEquals(0, Reset.count);
    Assert.assertEquals(1, LockedRegion.count);
  }
}