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
package com.android.jack.java8.gwt.test019.jack;

import org.junit.Test;

import junit.framework.Assert;

public class Java8Test {
  interface Ctor {
    X2 makeX(int x);
  }

  static class X2 {
    protected int field;
    void foo() {
      int local;
      class Y extends X2 {
        class Z extends X2 {
          void f() {
            Ctor c = X2::new;
            X2 x = c.makeX(123456);
            Assert.assertEquals(123456, x.field);
            c = Y::new;
            x = c.makeX(987654);
            x = new Y(987654);
            Assert.assertEquals(987655, x.field);
            c = Z::new;
            x = c.makeX(456789);
            x = new Z(456789);
            Assert.assertEquals(456791, x.field);
          }
          private Z(int z) {
            super(z + 2);
          }
          Z() {
          }
        }

        private Y(int y) {
          super(y + 1);
        }

        private Y() {
        }
      }
      new Y().new Z().f();
    }

    private X2(int x) {
      this.field = x;
    }
    X2() {
    }
  }

  @Test
  public void testPrivateConstructorReference() {
    new X2().foo();
  }
}
