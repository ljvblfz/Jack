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

import com.android.jack.category.KnownBugs;
import com.android.jack.frontend.FrontendCompilationException;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import javax.annotation.Nonnull;

/**
 * JUnit checking that compilation failed properly.
 */
public class ErrorTest {

  @BeforeClass
  public static void setUpClass() {
    Main.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  @Test
  public void testError001() throws Exception {
    checkInternalCompilerException("error/test001");
  }

  @Test
  @Category(KnownBugs.class)
  public void testError002() throws Exception {
    checkInternalCompilerException("error/test002");
  }

  private void checkInternalCompilerException(@Nonnull String testName)
      throws Exception, IOException {
    ByteArrayOutputStream baos = null;
    PrintStream redirectStream = null;
    try {
      baos = new ByteArrayOutputStream();
      redirectStream = new PrintStream(baos);
      System.setErr(redirectStream);

      TestTools.runCompilation(
          TestTools.buildCommandLineArgs(TestTools.getJackTestsWithJackFolder(testName)));
      Assert.fail();
    } catch (FrontendCompilationException e) {
      if (baos != null && baos.toString().contains("InternalCompilerException")) {
        Assert.fail();
      }
    } catch (RuntimeException e) {
      Assert.fail();
    } finally {
      if (redirectStream != null) {
        redirectStream.close();
      }
      if (baos != null) {
        baos.close();
      }
    }
  }
}
