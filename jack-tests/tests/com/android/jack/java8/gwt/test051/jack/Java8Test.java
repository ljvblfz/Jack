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

package com.android.jack.java8.gwt.test051.jack;

import org.junit.Test;

import junit.framework.Assert;

public class Java8Test {

  interface InterfaceWithThisReference {
    default String n() {
      return "default n";
    }

    default String callNUnqualified() {
      class Super implements InterfaceWithThisReference {
        public String n() {
          return "super n";
        }
      }
      return new Super() {
        public String callNUnqualified() {
          return "Object " + n();
        }
      }.callNUnqualified();
    }

    default String callNWithThis() {
      class Super implements InterfaceWithThisReference {
        public String n() {
          return "super n";
        }
      }
      return new Super() {
        public String callNWithThis() {
          return "Object " + this.n();
        }
      }.callNWithThis();
    }

    default String callNWithInterfaceThis() {
      class Super implements InterfaceWithThisReference {
        public String n() {
          return "super n";
        }
      }
      return new Super() {
        public String callNWithInterfaceThis() {
          // In this method this has interface Test as its type, but it refers to outer n();
          return "Object " + InterfaceWithThisReference.this.n();
        }
      }.callNWithInterfaceThis();
    }

    default String callNWithSuper() {
      class Super implements InterfaceWithThisReference {
        public String n() {
          return "super n";
        }
      }
      return new Super() {
        public String callNWithSuper() {
          // In this method this has interface Test as its type.
          return "Object " + super.n();
        }
      }.callNWithSuper();
    }

    default String callNWithInterfaceSuper() {
      return new InterfaceWithThisReference() {
        public String n() {
          return "this n";
        }

        public String callNWithInterfaceSuper() {
          // In this method this has interface Test as its type and refers to default n();
          return "Object " + InterfaceWithThisReference.super.n();
        }
      }.callNWithInterfaceSuper();
    }
  }

  @Test
  public void testInterfaceThis() {
    class A implements InterfaceWithThisReference {
      public String n() {
        return "n";
      }
    }
    Assert.assertEquals("Object super n", new A().callNUnqualified());
    Assert.assertEquals("Object super n", new A().callNWithThis());
    Assert.assertEquals("Object n", new A().callNWithInterfaceThis());
    Assert.assertEquals("Object super n", new A().callNWithSuper());
    Assert.assertEquals("Object default n", new A().callNWithInterfaceSuper());
  }
}
