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

package com.android.jack.java8.gwt.test033.jack;

import org.junit.Test;

import junit.framework.Assert;

public class Java8Test {

  interface InterfaceWithTwoDefenderMethods {
    default String foo() {
      return "interface.foo";
    }

    default String bar() {
      return this.foo() + " " + foo();
    }
  }

  class ClassImplementOneDefenderMethod implements InterfaceWithTwoDefenderMethods {
    public String foo() {
      return "class.foo";
    }
  }

  @Test
  public void testThisRefInDefenderMethod() {
    ClassImplementOneDefenderMethod c = new ClassImplementOneDefenderMethod();
    InterfaceWithTwoDefenderMethods i1 = c;
    InterfaceWithTwoDefenderMethods i2 = new InterfaceWithTwoDefenderMethods() {};
    Assert.assertEquals("class.foo class.foo", c.bar());
    Assert.assertEquals("class.foo class.foo", i1.bar());
    Assert.assertEquals("interface.foo interface.foo", i2.bar());
  }
}
