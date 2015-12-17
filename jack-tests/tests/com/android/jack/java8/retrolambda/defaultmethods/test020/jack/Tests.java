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

package com.android.jack.java8.retrolambda.defaultmethods.test020.jack;

import org.junit.Assert;
import org.junit.Test;

public class Tests {

  private interface CallOtherMethods {
    int foo();

    default int callsFoo() {
        return foo() + 10;
    }
}

  @Test
  public void lambdas_with_default_methods() {
      CallOtherMethods lambda = () -> 2;
      Assert.assertEquals(lambda.foo(), 2);
      Assert.assertEquals(lambda.callsFoo(), 12);
  }

}

