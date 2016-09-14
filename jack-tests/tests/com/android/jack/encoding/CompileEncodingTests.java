/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.jack.encoding;

import com.android.jack.backend.dex.DexFileWriter;
import com.android.jack.frontend.FrontendCompilationException;
import com.android.jack.test.TestsProperties;
import com.android.jack.test.category.SlowTests;
import com.android.jack.test.junit.KnownIssue;
import com.android.jack.test.junit.Runtime;
import com.android.jack.test.runner.RuntimeRunner;
import com.android.jack.test.runner.RuntimeRunnerException;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.JackApiV04Toolchain;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.annotation.Nonnull;

public class CompileEncodingTests {
  private static int SUCCESS = 0;
  private static int ERROR  = 1;

  private static final String UTF8 = "com.android.jack.encoding.tests.utf8.EncodingUtf8Tests";
  private static final String UTF16 = "com.android.jack.encoding.tests.utf16.EncodingUtf16Tests";
  private static final String ISO8859_1 =
      "com.android.jack.encoding.tests.iso8859dash1.EncodingIso8859dash1Tests";

  @Test
  @Runtime
  @Category(SlowTests.class)
  public void testFileDefaultUtf8() throws Exception {
    testFileWithDefault(UTF8, StandardCharsets.UTF_8,      SUCCESS);
    testFileWithDefault(UTF8, StandardCharsets.UTF_16,     ERROR);
    testFileWithDefault(UTF8, StandardCharsets.ISO_8859_1, ERROR);
  }

  @Test
  @Runtime
  @Category(SlowTests.class)
  @KnownIssue
  public void testDirDefaultUtf8() throws Exception {
    testDirWithDefault(UTF8, StandardCharsets.UTF_8,      SUCCESS);
    testDirWithDefault(UTF8, StandardCharsets.UTF_16,     ERROR);
    testDirWithDefault(UTF8, StandardCharsets.ISO_8859_1, ERROR);
  }

  @Test
  @Runtime
  @Category(SlowTests.class)
  public void testFileUtf8() throws Exception {
    testByFile(UTF8, StandardCharsets.UTF_16, StandardCharsets.UTF_8,      SUCCESS);
    testByFile(UTF8, StandardCharsets.UTF_8,  StandardCharsets.UTF_16,     ERROR);
    testByFile(UTF8, StandardCharsets.UTF_8,  StandardCharsets.ISO_8859_1, ERROR);
  }

  @Test
  @Runtime
  @Category(SlowTests.class)
  public void testFileDefaultUtf16() throws Exception {
    testFileWithDefault(UTF16, StandardCharsets.UTF_8,      ERROR);
    testFileWithDefault(UTF16, StandardCharsets.UTF_16,     SUCCESS);
    testFileWithDefault(UTF16, StandardCharsets.ISO_8859_1, ERROR);
  }

  @Test
  @Runtime
  @Category(SlowTests.class)
  @KnownIssue
  public void testDirDefaultUtf16() throws Exception {
    testDirWithDefault(UTF16, StandardCharsets.UTF_8,      ERROR);
    testDirWithDefault(UTF16, StandardCharsets.UTF_16,     SUCCESS);
    testDirWithDefault(UTF16, StandardCharsets.ISO_8859_1, ERROR);
  }

  @Test
  @Runtime
  @Category(SlowTests.class)
  public void testFileUtf16() throws Exception {
    testByFile(UTF16, StandardCharsets.UTF_16, StandardCharsets.UTF_8,      ERROR);
    testByFile(UTF16, StandardCharsets.UTF_8,  StandardCharsets.UTF_16,     SUCCESS);
    testByFile(UTF16, StandardCharsets.UTF_16, StandardCharsets.ISO_8859_1, ERROR);
  }

  @Test
  @Runtime
  @Category(SlowTests.class)
  public void testFileDefaultIso8859dash1() throws Exception {
    testFileWithDefault(ISO8859_1, StandardCharsets.UTF_8,      ERROR);
    testFileWithDefault(ISO8859_1, StandardCharsets.UTF_16,     ERROR);
    testFileWithDefault(ISO8859_1, StandardCharsets.ISO_8859_1, SUCCESS);
  }

  @Test
  @Runtime
  @Category(SlowTests.class)
  @KnownIssue
  public void testDirDefaultIso8859dash1() throws Exception {
    testDirWithDefault(ISO8859_1, StandardCharsets.UTF_8,      ERROR);
    testDirWithDefault(ISO8859_1, StandardCharsets.UTF_16,     ERROR);
    testDirWithDefault(ISO8859_1, StandardCharsets.ISO_8859_1, SUCCESS);
  }

  @Test
  @Runtime
  @Category(SlowTests.class)
  public void testFileIso8859dash1() throws Exception {
    testByFile(ISO8859_1, StandardCharsets.ISO_8859_1, StandardCharsets.UTF_8,      ERROR);
    testByFile(ISO8859_1, StandardCharsets.ISO_8859_1, StandardCharsets.UTF_16,     ERROR);
    testByFile(ISO8859_1, StandardCharsets.UTF_16,     StandardCharsets.ISO_8859_1, SUCCESS);
  }

  private void testFileWithDefault(@Nonnull String clazz, @Nonnull Charset charset, int expected)
      throws Exception {
    JackApiV04Toolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiV04Toolchain.class);
    File binDirectory = AbstractTestTools.createTempDir();
    toolchain.setDefaultCharset(charset);
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath());

    try {
      toolchain.srcToExe(
          binDirectory,
          false,
          new File(AbstractTestTools.getTestRootDir(clazz.substring(0, clazz.lastIndexOf('.'))),
              clazz.substring(clazz.lastIndexOf('.') + 1) + ".java")
      );
    } catch (FrontendCompilationException e) {
      if (expected == SUCCESS) {
        Assert.fail();
      } else {
        return;
      }
    }

    run(clazz, binDirectory, expected);
  }

  private void testDirWithDefault(@Nonnull String clazz, @Nonnull Charset charset, int expected)
      throws Exception {
    JackApiV04Toolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiV04Toolchain.class);
    File binDirectory = AbstractTestTools.createTempDir();
    toolchain.setDefaultCharset(charset);
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath());

    try {
      toolchain.srcToExe(
          binDirectory,
          false,
          AbstractTestTools.getTestRootDir(clazz.substring(0, clazz.lastIndexOf('.')))
      );
    } catch (FrontendCompilationException e) {
      if (expected == SUCCESS) {
        Assert.fail();
      } else {
        return;
      }
    }

    run(clazz, binDirectory, expected);
  }

  private void testByFile(@Nonnull String clazz, @Nonnull Charset defaultCharset,
      @Nonnull Charset fileCharset, int expected) throws Exception {
    JackApiV04Toolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiV04Toolchain.class);
    File binDirectory = AbstractTestTools.createTempDir();
    toolchain.setDefaultCharset(defaultCharset);
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath());

    try {
      toolchain.srcToExe(
          binDirectory,
          false,
          new File(AbstractTestTools.getTestRootDir(clazz.substring(0, clazz.lastIndexOf('.'))),
              clazz.substring(clazz.lastIndexOf('.') + 1) + ".java[" + fileCharset.name() + "]")
      );
    } catch (FrontendCompilationException e) {
      if (expected == SUCCESS) {
        Assert.fail();
      } else {
        return;
      }
    }

    run(clazz, binDirectory, expected);
  }

  private void run(@Nonnull String clazz, @Nonnull File binDirectory,
      int expected)
          throws SecurityException, IllegalArgumentException, RuntimeRunnerException {
    for (RuntimeRunner runner : AbstractTestTools.listRuntimeTestRunners(null)) {
      if (runner.runJUnit(
              new String[0],
              AbstractTestTools.JUNIT_RUNNER_NAME,
              new String[] {clazz},
              new File(TestsProperties.getJackRootDir(),
                       "jack-tests/prebuilts/junit4-hostdex.jar"),
              new File(binDirectory,
                       DexFileWriter.DEX_FILENAME)
          ) == 0) {
        if (expected == ERROR) {
          Assert.fail();
        }
      } else {
        if (expected == SUCCESS) {
          Assert.fail();
        }
      }
    }
  }
}
