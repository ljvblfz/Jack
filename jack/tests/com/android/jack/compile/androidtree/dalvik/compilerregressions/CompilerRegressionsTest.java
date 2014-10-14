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

package com.android.jack.compile.androidtree.dalvik.compilerregressions;

import com.android.jack.Options;
import com.android.jack.TestTools;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

@Ignore("Tree")
public class CompilerRegressionsTest {

  private static File[] BOOTCLASSPATH;

  @BeforeClass
  public static void setUpClass() {
    CompilerRegressionsTest.class.getClassLoader().setDefaultAssertionStatus(true);
    BOOTCLASSPATH = new File[] {TestTools.getFromAndroidTree(
        "out/target/common/obj/JAVA_LIBRARIES/core-libart_intermediates/classes.zip")};
  }

  @Test
  public void compileRegressions() throws Exception {
    File out = TestTools.createTempFile("out", ".zip");
    String classpath = TestTools.getClasspathAsString(BOOTCLASSPATH);
    TestTools.compileSourceToDex(new Options(),
        TestTools.getArtTestFolder("083-compiler-regressions"), classpath, out, /* zip = */ true);
  }
}
