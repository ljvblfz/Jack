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

package com.android.jack.java8.gwt.test036.jack;

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

  abstract class AbstractClass implements InterfaceWithTwoDefenderMethods {
  }

  class Child1 extends AbstractClass {
    public String foo() {
      return super.foo() + " child1.foo";
    }
  }

  class Child2 extends AbstractClass {
  }

  @Test
  public void testAbstractClassImplementsInterface() {
    Child1 child1 = new Child1();
    Child2 child2 = new Child2();
    Assert.assertEquals("interface.foo child1.foo", child1.foo());
    Assert.assertEquals("interface.foo", child2.foo());
  }
}
