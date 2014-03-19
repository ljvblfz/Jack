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

/**
 * JUnit test for compilation of Java 7 features
 */
public class ExceptionsTest {

  @BeforeClass
  public static void setUpClass() {
    Main.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  @Test
  public void java7Exception001() throws Exception {
    Options options = TestTools.buildCommandLineArgs(TestTools
        .getJackTestsWithJackFolder("java7/exceptions/test001"));
    options.addProperty(Options.JAVA_SOURCE_VERSION.getName(), "1.7");
    TestTools.runCompilation(options);
  }

  @Test
  public void java7Exception002() throws Exception {
    Options options = TestTools.buildCommandLineArgs(TestTools
        .getJackTestsWithJackFolder("java7/exceptions/test002"));
    options.addProperty(Options.JAVA_SOURCE_VERSION.getName(), "1.7");
    TestTools.runCompilation(options);
  }

  @Test
  public void java7Exception003() throws Exception {
    Options options = TestTools.buildCommandLineArgs(TestTools
        .getJackTestsWithJackFolder("java7/exceptions/test003"));
    options.addProperty(Options.JAVA_SOURCE_VERSION.getName(), "1.7");
    TestTools.runCompilation(options);
  }

  @Test
  public void java7Exception004() throws Exception {
    Options options = TestTools.buildCommandLineArgs(TestTools
        .getJackTestsWithJackFolder("java7/exceptions/test004"));
    options.addProperty(Options.JAVA_SOURCE_VERSION.getName(), "1.7");
    TestTools.runCompilation(options);
  }

  @Test
  public void java7Exception005() throws Exception {
    Options options = TestTools.buildCommandLineArgs(TestTools
        .getJackTestsWithJackFolder("java7/exceptions/test005"));
    options.addProperty(Options.JAVA_SOURCE_VERSION.getName(), "1.7");
    TestTools.runCompilation(options);
  }
}
