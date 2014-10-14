/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.jack;

import com.android.jack.annotation.AnnotationTests;
import com.android.jack.arithmetic.ArithmeticTests;
import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.runtime.RuntimeTest;
import com.android.jack.test.runtime.RuntimeTestInfo;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class RegressionTests {

  public RuntimeTest[] tests = {
      new AnnotationTests(),
      new ArithmeticTests()
  };

  @BeforeClass
  public static void setUpClass() {
    Main.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  @Test
  public void runRegressionTests() throws Exception {
    List<RuntimeTestInfo> rtTestInfos = new ArrayList<RuntimeTestInfo>();

    for (RuntimeTest test : tests) {
      for (RuntimeTestInfo testInfos : test.getRuntimeTestInfos()) {
        rtTestInfos.add(testInfos);
      }
    }
    new RuntimeTestHelper(rtTestInfos.toArray(new RuntimeTestInfo[rtTestInfos.size()]))
        .compileAndRunTest();
  }

}
