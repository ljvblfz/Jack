/*
 * Copyright 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.android.jack.java8.gwt.test039.jack;

import org.junit.Test;

import junit.framework.Assert;

public class Java8Test {

  class EmptyA {
  }
  interface EmptyI {
  }
  interface EmptyJ {
  }
  class EmptyB extends EmptyA implements EmptyI {
  }
  class EmptyC extends EmptyA implements EmptyI, EmptyJ {
  }

  @Test
  public void testBaseIntersectionCast() {
    EmptyA localB = new EmptyB();
    EmptyA localC = new EmptyC();
    EmptyB b2BI = (EmptyB & EmptyI) localB;
    EmptyC c2CIJ = (EmptyC & EmptyI & EmptyJ) localC;
    EmptyI ii1 = (EmptyB & EmptyI) localB;
    EmptyI ii2 = (EmptyC & EmptyI) localC;
    EmptyI ii3 = (EmptyC & EmptyJ) localC;
    EmptyI ii4 = (EmptyC & EmptyI & EmptyJ) localC;
    EmptyJ jj1 = (EmptyC & EmptyI & EmptyJ) localC;
    EmptyJ jj2 = (EmptyC & EmptyI) localC;
    EmptyJ jj3 = (EmptyC & EmptyJ) localC;
    EmptyJ jj4 = (EmptyI & EmptyJ) localC;

    try {
      EmptyC b2CIJ = (EmptyC & EmptyI & EmptyJ) localB;
      Assert.fail("Should have thrown a ClassCastException");
    } catch (ClassCastException e) {
      // Expected.
    }
    try {
      EmptyB c2BI = (EmptyB & EmptyI) localC;
      Assert.fail("Should have thrown a ClassCastException");
    } catch (ClassCastException e) {
      // Expected.
    }
    try {
      EmptyJ jj = (EmptyB & EmptyJ) localB;
      Assert.fail("Should have thrown a ClassCastException");
    } catch (ClassCastException e) {
      // Expected.
    }
  }
}
