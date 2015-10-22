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

package com.android.jack.shrob;

import com.android.jack.Options;
import com.android.jack.ProguardFlags;
import com.android.jack.TestTools;
import com.android.jack.category.SlowTests;
import com.android.jack.config.id.JavaVersionPropertyId.JavaVersion;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;

/**
 * Tests the compilation of tree projects using shrob
 */
@Ignore("Tree")
@Category(SlowTests.class)
public class TreeTest {
  private static File CORE_SOURCELIST;

  @BeforeClass
  public static void setUpClass() {
    CORE_SOURCELIST = TestTools.getTargetLibSourcelist("core-libart");
  }

  private static ProguardFlags dontObfuscateFlagFile =
      new ProguardFlags(TestTools.getJackTestFolder("shrob"), "dontobfuscate.flags");

  @Test
  public void testObjectEquals() throws Exception {
    File testFolder = TestTools.getJackTestFolder("shrob/test024");
    Options options = TestTools.buildCommandLineArgs(new File[]{CORE_SOURCELIST, testFolder});
    options.addProperty(Options.JAVA_SOURCE_VERSION.getName(), JavaVersion.JAVA_7.toString());
    options.addProguardFlagsFile(new ProguardFlags(testFolder, "proguard.flags001"));
    options.addProguardFlagsFile(dontObfuscateFlagFile);
    options.addProperty(Options.METHOD_FILTER.getName(), "supported-methods");

    TestTools.buildSession(options);
  }
}
