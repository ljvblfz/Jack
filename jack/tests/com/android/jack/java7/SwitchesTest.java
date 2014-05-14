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

package com.android.jack.java7;

import com.android.jack.Main;
import com.android.jack.Options;
import com.android.jack.TestTools;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * JUnit test for compilation of Java 7 features
 */
public class SwitchesTest {

  @BeforeClass
  public static void setUpClass() {
    Main.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  @Test
  public void java7Switches001() throws Exception {
    Options options = TestTools.buildCommandLineArgs(TestTools
        .getJackTestsWithJackFolder("java7/switches/test001"));
    options.addProperty(Options.JAVA_SOURCE_VERSION.getName(), "1.7");
    TestTools.runCompilation(options);
  }

  @Test
  public void java7Switches002() throws Exception {
    Options options = TestTools.buildCommandLineArgs(TestTools
        .getJackTestsWithJackFolder("java7/switches/test002"));
    options.addProperty(Options.JAVA_SOURCE_VERSION.getName(), "1.7");
    TestTools.runCompilation(options);
  }

  @Test
  public void java7Switches003() throws Exception {
    Options options = TestTools.buildCommandLineArgs(TestTools
        .getJackTestsWithJackFolder("java7/switches/test003"));
    options.addProperty(Options.JAVA_SOURCE_VERSION.getName(), "1.7");
    TestTools.runCompilation(options);
  }

  @Test
  public void java7Switches004() throws Exception {
    File jackZipFile = TestTools.createTempFile("tmp", ".zip");

    Options options = new Options();
    options.addProperty(Options.JAVA_SOURCE_VERSION.getName(), "1.7");

    TestTools.compileSourceToJack(options,
        TestTools.getJackTestsWithJackFolder("java7/switches/test001"),
        TestTools.getDefaultBootclasspathString(), jackZipFile, true /* zip */
    );

    options = new Options();
    List<File> imports = new ArrayList<File>(1);
    imports.add(jackZipFile);
    options.setJayceImports(imports);
    File outDexFile = TestTools.createTempFile("tmp", ".dex");
    TestTools.compileJackToDex(options, jackZipFile, outDexFile, false /* zip */);
  }
}
