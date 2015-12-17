/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// Copyright © 2013-2015 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package com.android.jack.java8.retrolambda.interfacestaticmethods.test002.jack;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.concurrent.Callable;

public class Tests {

  private interface Interface {
    default int callStaticMethod() {
        return staticMethod();
    }

    static int staticMethod() {
        return 42;
    }

    // arguments of just a couple of different types because we're lazy
    static String staticMethodWithArgs(String s, int a, long b) {
        return s + a + b;
    }

    static int staticMethodContainingLambdas() throws Exception {
        Callable<Integer> lambda = () -> 123;
        return lambda.call();
    }
}

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void static_methods_on_interfaces_containing_lambdas() throws Exception {
    Assert.assertEquals(Interface.staticMethodContainingLambdas(), 123);
  }

}

