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

package com.android.jack.compile.androidtree.bouncycastle;

import com.android.jack.JarJarRules;
import com.android.jack.Options;
import com.android.jack.ProguardFlags;
import com.android.jack.TestTools;
import com.android.jack.category.RedundantTests;
import com.android.jack.category.SlowTests;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;

public class BouncycastleCompilationTest {

  private static final File[] BOOTCLASSPATH = new File[] {
    TestTools.getFromAndroidTree(
        "out/target/common/obj/JAVA_LIBRARIES/core_intermediates/classes.jar")
  };

  private static final File SOURCELIST = TestTools.getTargetLibSourcelist("bouncycastle");

  private static final JarJarRules JARJAR_RULES = new JarJarRules(
      TestTools.getFromAndroidTree("external/bouncycastle/jarjar-rules.txt"));

  @BeforeClass
  public static void setUpClass() {
    BouncycastleCompilationTest.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  @Test
  @Category(RedundantTests.class)
  public void compileBouncycastle() throws Exception {
    File out = TestTools.createTempFile("bouncycastle", ".dex");
    Options options = new Options();
    options.disableDxOptimizations();
    TestTools.compileSourceToDex(options,
        SOURCELIST,
        TestTools.getClasspathAsString(BOOTCLASSPATH),
        out,
        false /* zip */,
        JARJAR_RULES,
        null /* flagFiles */,
        false /* emitDebugInfo */);
  }

  @Test
  @Category(SlowTests.class)
  public void compareBouncycastleStructure() throws Exception {
    TestTools.checkStructure(
        BOOTCLASSPATH,
        null,
        SOURCELIST,
        false /* compareDebugInfoBinary */,
        true /* compareInstructionNumber */,
        0.4f,
        JARJAR_RULES,
        (ProguardFlags[]) null);
  }
}
