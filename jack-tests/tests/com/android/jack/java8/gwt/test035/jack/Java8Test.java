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

package com.android.jack.java8.gwt.test035.jack;

import org.junit.Test;

import junit.framework.Assert;

public class Java8Test {

  interface InterfaceI {
    default String print() {
      return "interface1";
    }
  }

  interface InterfaceII {
    default String print() {
      return "interface2";
    }
  }

  class ClassI {
    public String print() {
      return "class1";
    }
  }

  class ClassII extends ClassI implements InterfaceI, InterfaceII {
    public String print() {
      return super.print() + " " + InterfaceI.super.print() + " " + InterfaceII.super.print();
    }
  }

  @Test
  public void testSuperRefInDefenderMethod() {
    ClassII c = new ClassII();
    Assert.assertEquals("class1 interface1 interface2", c.print());
  }
}
