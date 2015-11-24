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
package com.android.jack.java8.gwt.test040.jack;

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

  interface SimpleI {
    int fun();
  }
  interface SimpleJ {
    int foo();
    int bar();
  }
  interface SimpleK {
  }

  @Test
  public void testIntersectionCastWithLambdaExpr() {
    SimpleI simpleI1 = (SimpleI & EmptyI) () -> {
      return 11;
    };
    Assert.assertEquals(11, simpleI1.fun());
    SimpleI simpleI2 = (EmptyI & SimpleI) () -> {
      return 22;
    };
    Assert.assertEquals(22, simpleI2.fun());
    EmptyI emptyI = (EmptyI & SimpleI) () -> {
      return 33;
    };
    Assert.assertEquals(55, ((SimpleI & SimpleK) () -> {
      return 55;
    }).fun());
  }
}
