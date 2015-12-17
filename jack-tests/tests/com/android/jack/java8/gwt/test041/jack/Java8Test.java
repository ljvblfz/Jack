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

package com.android.jack.java8.gwt.test041.jack;

import org.junit.Test;

import junit.framework.Assert;

public class Java8Test {

  interface SimpleI {
    int fun();
  }

  class SimpleA {
    public int bar() {
      return 11;
    }
  }

  class SimpleB extends SimpleA implements SimpleI {
    public int fun() {
      return 22;
    }
  }

  class SimpleC extends SimpleA implements SimpleI {
    public int fun() {
      return 33;
    }

    public int bar() {
      return 44;
    }
  }

  @Test
  public void testIntersectionCastPolymorphism() {
    SimpleA bb = new SimpleB();
    Assert.assertEquals(22, ((SimpleB & SimpleI) bb).fun());
    Assert.assertEquals(11, ((SimpleB & SimpleI) bb).bar());
    SimpleA cc = new SimpleC();
    Assert.assertEquals(33, ((SimpleC & SimpleI) cc).fun());
    Assert.assertEquals(44, ((SimpleC & SimpleI) cc).bar());
    Assert.assertEquals(33, ((SimpleA & SimpleI) cc).fun());
    SimpleI ii = (SimpleC & SimpleI) cc;
    Assert.assertEquals(33, ii.fun());
  }
}
