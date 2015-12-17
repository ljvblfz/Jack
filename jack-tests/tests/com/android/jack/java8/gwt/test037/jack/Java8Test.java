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

package com.android.jack.java8.gwt.test037.jack;

import org.junit.Test;

import junit.framework.Assert;

public class Java8Test {

  interface II {
    default String fun() {
      return "fun() in i: " + this.foo();
    };

    default String foo() {
      return "foo() in i.\n";
    };
  }

  interface JJ extends II {
    default String fun() {
      return "fun() in j: " + this.foo() + II.super.fun();
    };

    default String foo() {
      return "foo() in j.\n";
    }
  }

  class AA {
    public String fun() {
      return "fun() in a: " + this.foo();
    }

    public String foo() {
      return "foo() in a.\n";
    }
  }

  class BB extends AA implements JJ {
    public String fun() {
      return "fun() in b: " + this.foo() + super.fun() + JJ.super.fun();
    }

    public String foo() {
      return "foo() in b.\n";
    }
  }

  class CC extends BB implements JJ {
    public String fun() {
      return "fun() in c: " + super.fun();
    }
  }

  @Test
  public void testSuperThisRefsInDefenderMethod() {
  CC c = new CC();
  II i1 = c;
  JJ j1 = c;
  BB b = new BB();
  II i2 = b;
  JJ j2 = b;
  JJ j3 = new JJ() { };
  II i3 = j3;
  II i4 = new II() { };
  String c_fun = "fun() in c: fun() in b: foo() in b.\n"
      + "fun() in a: foo() in b.\n"
      + "fun() in j: foo() in b.\n"
      + "fun() in i: foo() in b.\n";
  String b_fun = "fun() in b: foo() in b.\n"
      + "fun() in a: foo() in b.\n"
      + "fun() in j: foo() in b.\n"
      + "fun() in i: foo() in b.\n";
  String j_fun = "fun() in j: foo() in j.\n"
      + "fun() in i: foo() in j.\n";
  String i_fun = "fun() in i: foo() in i.\n";
  Assert.assertEquals(c_fun, c.fun());
  Assert.assertEquals(c_fun, i1.fun());
  Assert.assertEquals(c_fun, j1.fun());
  Assert.assertEquals(b_fun, b.fun());
  Assert.assertEquals(b_fun, i2.fun());
  Assert.assertEquals(b_fun, j2.fun());
  Assert.assertEquals(j_fun, j3.fun());
  Assert.assertEquals(j_fun, i3.fun());
  Assert.assertEquals(i_fun, i4.fun());
}
}
