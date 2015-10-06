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
package com.android.jack.java8.gwt.test038.jack;

import org.junit.Test;

import junit.framework.Assert;

public class Java8Test {

  interface OuterInterface {
    default String m() {
      return "I.m;" + new InnerClass().n();
    }

    default String n() {
      return "I.n;" + this.m();
    }

    class InnerClass {
      public String n() {
        return "A.n;" + m();
      }

      public String m() {
        return "A.m;";
      }
    }
  }

  class OuterClass {
    public String m() {
      return "B.m;";
    }

    public String n1() {
      OuterInterface i = new OuterInterface() {};
      return "B.n1;" + i.n() + OuterClass.this.m();
    }

    public String n2() {
      OuterInterface i = new OuterInterface() {
        @Override
        public String n() {
          return this.m() + OuterClass.this.m();
        }
      };
      return "B.n2;" + i.n() + OuterClass.this.m();
    }
  }

  @Test
  public void testNestedInterfaceClass() {
    OuterClass outerClass = new OuterClass();
    Assert.assertEquals("B.n1;I.n;I.m;A.n;A.m;B.m;", outerClass.n1());
    Assert.assertEquals("B.n2;I.m;A.n;A.m;B.m;B.m;", outerClass.n2());
  }
}
