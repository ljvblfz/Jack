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

package com.android.jack.java8.gwt.test045.jack;

import org.junit.Test;

import junit.framework.Assert;

public class Java8Test {

  interface I {
    int foo(Integer i);
  }
  interface Lambda<T> {
    T run(int a, int b);
  }

  @Test
  public void testLambdaNestingInMultipleMixedAnonymousCaptureLocalAndField() {
    // checks that lambda has access to local variable, field and arguments when placed in mixed
    // scopes - Local Class -> Local Class -> Local Anonymous -> lambda -> Local Anonymous
    class A {
      int fA = 1;

      int a() {
        int[] x = new int[] {42};
        class B {
          int fB = 2;

          int b() {
            I i = new I() {
              int fI = 3;

              public int foo(Integer arg) {
                Runnable r = () -> {
                  new Runnable() {
                    public void run() {
                      Lambda<Integer> l = (a, b) -> x[0] = x[0] + a + b + arg + fA + fB + fI;
                      l.run(1, 2);
                    }
                  }.run();
                };
                r.run();
                return x[0];
              }
            };
            return i.foo(1);
          }
        }
        B b = new B();
        return b.b();
      }
    }
    A a = new A();
    Assert.assertEquals(52, a.a());
  }

}
