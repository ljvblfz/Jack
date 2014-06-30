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

package com.android.jack.errorhandling;

import com.android.jack.Options;
import com.android.jack.TestTools;
import com.android.jack.frontend.FrontendCompilationException;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * JUnit test checking Jack behavior with source errors.
 */
public class SourceErrorTest {

  /**
   * Checks that compilation fails because of invalid "class" keyword.
   */
  @Test
  public void testInvalidSource001() throws Exception {
    TestingEnvironment te = new TestingEnvironment();

    te.addFile(te.getSourceFolder(), "jack.invalidsource", "A.java", "package jack.invalidsource;\n"
        + "public clas A {}\n");

    try {
      te.startErrRedirection();
      te.compile(getOptions(te));
      Assert.fail();
    } catch (FrontendCompilationException ex) {
      // Failure is ok since source does not compile.
    } finally {
      Assert.assertTrue(
          te.endErrRedirection().contains("Syntax error on token \"clas\", class expected"));
    }
  }

  /**
   * Checks that compilation fails because of invalid "public" keyword.
   */
  @Test
  public void testInvalidSource002() throws Exception {
    TestingEnvironment te = new TestingEnvironment();

    te.addFile(te.getSourceFolder(), "jack.invalidsource", "A.java", "package jack.invalidsource;\n"
        + "publi class A {}\n");

    try {
      te.startErrRedirection();
      te.compile(getOptions(te));
      Assert.fail();
    } catch (FrontendCompilationException ex) {
      // Failure is ok since source does not compile.
    } finally {
      Assert.assertTrue(
          te.endErrRedirection().contains("Syntax error on token \"publi\", public expected"));
    }
  }

  /**
   * Checks that compilation fails because of a class name that does not match the file name.
   */
  @Test
  public void testInvalidSource003() throws Exception {
    TestingEnvironment te = new TestingEnvironment();

    te.addFile(te.getSourceFolder(), "jack.invalidsource", "A.java", "package jack.invalidsource;\n"
        + "public class B {}\n");

    try {
      te.startErrRedirection();
      te.compile(getOptions(te));
      Assert.fail();
    } catch (FrontendCompilationException ex) {
      // Failure is ok since source does not compile.
    } finally {
      Assert.assertTrue(
          te.endErrRedirection().contains("The public type B must be defined in its own file"));
    }
  }

  /**
   * Checks that compilation fails because of an import of a class that is not on classpath.
   */
  @Test
  public void testInvalidSource004() throws Exception {
    TestingEnvironment te = new TestingEnvironment();

    te.addFile(te.getSourceFolder(), "jack.invalidsource", "A.java", "package jack.invalidsource;\n"
        + "import jack.invalidsource.B;\n"
        + "public class A {}\n");

    try {
      te.startErrRedirection();
      te.compile(getOptions(te));
      Assert.fail();
    } catch (FrontendCompilationException ex) {
      // Failure is ok since source does not compile.
    } finally {
      Assert.assertTrue(
          te.endErrRedirection().contains("The import jack.invalidsource.B cannot be resolved"));
    }
  }

  @Nonnull
  private Options getOptions(@Nonnull TestingEnvironment te) {
    Options options = new Options();
    List<String> ecjArgs = new ArrayList<String>();
    ecjArgs.add("-d");
    ecjArgs.add(te.getTestingFolder().getAbsolutePath());
    ecjArgs.add(te.getSourceFolder().getAbsolutePath());
    options.setEcjArguments(ecjArgs);
    options.setClasspath(TestTools.getDefaultBootclasspathString() + File.pathSeparator
        + te.getJackFolder());
    return options;
  }

}
