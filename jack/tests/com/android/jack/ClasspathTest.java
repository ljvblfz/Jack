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

package com.android.jack;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

public class ClasspathTest {

  @BeforeClass
  public static void setUpClass() {
    Main.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  @Test
  public void test001() throws Exception {
      File libOut = TestTools.createTempDir("ClasspathTest", "lib");

      Options libOptions = TestTools.buildCommandLineArgs(
          TestTools.getJackTestLibFolder("classpath/test001"));
      libOptions.setJayceOutputDir(libOut);
      TestTools.runCompilation(libOptions);

      File testOut = TestTools.createTempDir("ClasspathTest", "test");
      Options testOptions = TestTools.buildCommandLineArgs(
          TestTools.getJackTestsWithJackFolder("classpath/test001"));
      testOptions.setJayceOutputDir(testOut);
      testOptions.setClasspath(libOut.getAbsolutePath());
      TestTools.runCompilation(testOptions);
  }

  @Test
  public void test002() throws Exception {
    File testFolder = TestTools.getJackTestFolder("classpath/test002");
    File outFolder = TestTools.createTempDir("ClasspathTest2", "2");

    File lib1Out = new File(outFolder, "lib1");
    {
      if (!lib1Out.mkdir()) {
        throw new AssertionError("Failed to create dir " + lib1Out.getAbsolutePath());
      }
      Options lib1Options = TestTools.buildCommandLineArgs(new File(testFolder, "lib1"));
      lib1Options.setJayceOutputDir(lib1Out);
      TestTools.runCompilation(lib1Options);
    }

    File lib1BisOut = new File(outFolder, "lib1override");
    {
      if (!lib1BisOut.mkdir()) {
        throw new AssertionError("Failed to create dir " + lib1BisOut.getAbsolutePath());
      }
      Options lib1BisOptions = TestTools.buildCommandLineArgs(new File(testFolder, "lib1override"));
      lib1BisOptions.setJayceOutputDir(lib1BisOut);
      TestTools.runCompilation(lib1BisOptions);
    }

    File lib2Out = new File(outFolder, "lib2");
    {
      if (!lib2Out.mkdir()) {
        throw new AssertionError("Failed to create dir " + lib2Out.getAbsolutePath());
      }
      Options lib2Options = TestTools.buildCommandLineArgs(new File(testFolder, "lib2"));
      lib2Options.setJayceOutputDir(lib2Out);
      lib2Options.setClasspath(lib1Out.getAbsolutePath());
      TestTools.runCompilation(lib2Options);
    }

    {
      Options testOptions = TestTools.buildCommandLineArgs(new File(testFolder, "jack"));
      testOptions.setOutputDir(outFolder);
      testOptions.addJayceImport(lib2Out);
      testOptions.setClasspath(lib1BisOut.getAbsolutePath());
      TestTools.runCompilation(testOptions);
    }
  }
}
