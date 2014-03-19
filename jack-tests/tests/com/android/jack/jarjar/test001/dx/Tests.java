/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.jack.jarjar.test001.dx;

import com.android.jack.jarjar.test001.jack.A;

import junit.framework.Assert;

import org.junit.Test;

public class Tests {

  @Test
  public void test() {
    Assert.assertEquals("com.android.jack.jarjar.test001.jack.relocated.B", new A().getClassNameOfB());

    Assert.assertEquals("com.android.jack.jarjar.test001.jack.star.relocated.B", new A().starGetClassNameOfB());
    Assert.assertEquals("com.android.jack.jarjar.test001.jack.star.relocated.C", new A().starGetClassNameOfC());
    Assert.assertEquals("com.android.jack.jarjar.test001.jack.star.untouched.B", new A().starGetClassNameOfUntouchedB());
    Assert.assertEquals("com.android.jack.jarjar.test001.jack.star.untouched.C", new A().starGetClassNameOfUntouchedC());

    Assert.assertEquals("com.android.jack.jarjar.test001.jack.dstar.relocated.B", new A().dStarGetClassNameOfB());
    Assert.assertEquals("com.android.jack.jarjar.test001.jack.dstar.relocated.C", new A().dStarGetClassNameOfC());
    Assert.assertEquals("com.android.jack.jarjar.test001.jack.dstar.relocated.sub.B", new A().dStarGetClassNameOfUntouchedB());
    Assert.assertEquals("com.android.jack.jarjar.test001.jack.dstar.relocated.sub.C", new A().dStarGetClassNameOfUntouchedC());

    Assert.assertEquals("com.android.jack.jarjar.test001.jack.complex.two.sep.one.B", new A().complexGetClassNameOfB());
    Assert.assertEquals("com.android.jack.jarjar.test001.jack.complex.one.sep.sub.two.B", new A().complexGetClassNameOfUntouchedB());

    Assert.assertEquals("com.android.jack.jarjar.test001.jack.complex.two.sep.one.B", new A().testFieldRef());
    Assert.assertEquals("com.android.jack.jarjar.test001.jack.relocated.B", new A().testCtsString());

    Assert.assertEquals("com.android.jack.jarjar.test001.jack.relocated.B", new A().testAnnot());
  }
}
