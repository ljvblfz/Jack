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
import com.android.jack.encoding.tests.iso8859dash1.EncodingIso8859dash1Tests;
import com.android.jack.encoding.tests.utf16.EncodingUtf16Tests;
import com.android.jack.encoding.tests.utf8.EncodingUtf8Tests;
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

  @Test
  @Runtime
  @Category(SlowTests.class)
  public void testFileDefaultUtf8() throws Exception {
    testFileWithDefault(EncodingUtf8Tests.class, StandardCharsets.UTF_8,      SUCCESS);
    testFileWithDefault(EncodingUtf8Tests.class, StandardCharsets.UTF_16,     ERROR);
    testFileWithDefault(EncodingUtf8Tests.class, StandardCharsets.ISO_8859_1, ERROR);
  }

  @Test
  @Runtime
  @Category(SlowTests.class)
  @KnownIssue
  public void testDirDefaultUtf8() throws Exception {
    testDirWithDefault(EncodingUtf8Tests.class, StandardCharsets.UTF_8,      SUCCESS);
    testDirWithDefault(EncodingUtf8Tests.class, StandardCharsets.UTF_16,     ERROR);
    testDirWithDefault(EncodingUtf8Tests.class, StandardCharsets.ISO_8859_1, ERROR);
  }

  @Test
  @Runtime
  @Category(SlowTests.class)
  public void testFileUtf8() throws Exception {
    testByFile(EncodingUtf8Tests.class, StandardCharsets.UTF_16, StandardCharsets.UTF_8,      SUCCESS);
    testByFile(EncodingUtf8Tests.class, StandardCharsets.UTF_8,  StandardCharsets.UTF_16,     ERROR);
    testByFile(EncodingUtf8Tests.class, StandardCharsets.UTF_8,  StandardCharsets.ISO_8859_1, ERROR);
  }

  @Test
  @Runtime
  @Category(SlowTests.class)
  public void testFileDefaultUtf16() throws Exception {
    testFileWithDefault(EncodingUtf16Tests.class, StandardCharsets.UTF_8,      ERROR);
    testFileWithDefault(EncodingUtf16Tests.class, StandardCharsets.UTF_16,     SUCCESS);
    testFileWithDefault(EncodingUtf16Tests.class, StandardCharsets.ISO_8859_1, ERROR);
  }

  @Test
  @Runtime
  @Category(SlowTests.class)
  @KnownIssue
  public void testDirDefaultUtf16() throws Exception {
    testDirWithDefault(EncodingUtf16Tests.class, StandardCharsets.UTF_8,      ERROR);
    testDirWithDefault(EncodingUtf16Tests.class, StandardCharsets.UTF_16,     SUCCESS);
    testDirWithDefault(EncodingUtf16Tests.class, StandardCharsets.ISO_8859_1, ERROR);
  }

  @Test
  @Runtime
  @Category(SlowTests.class)
  public void testFileUtf16() throws Exception {
    testByFile(EncodingUtf16Tests.class, StandardCharsets.UTF_16, StandardCharsets.UTF_8,      ERROR);
    testByFile(EncodingUtf16Tests.class, StandardCharsets.UTF_8,  StandardCharsets.UTF_16,     SUCCESS);
    testByFile(EncodingUtf16Tests.class, StandardCharsets.UTF_16, StandardCharsets.ISO_8859_1, ERROR);
  }

  @Test
  @Runtime
  @Category(SlowTests.class)
  public void testFileDefaultIso8859dash1() throws Exception {
    testFileWithDefault(EncodingIso8859dash1Tests.class, StandardCharsets.UTF_8,      ERROR);
    testFileWithDefault(EncodingIso8859dash1Tests.class, StandardCharsets.UTF_16,     ERROR);
    testFileWithDefault(EncodingIso8859dash1Tests.class, StandardCharsets.ISO_8859_1, SUCCESS);
  }

  @Test
  @Runtime
  @Category(SlowTests.class)
  @KnownIssue
  public void testDirDefaultIso8859dash1() throws Exception {
    testDirWithDefault(EncodingIso8859dash1Tests.class, StandardCharsets.UTF_8,      ERROR);
    testDirWithDefault(EncodingIso8859dash1Tests.class, StandardCharsets.UTF_16,     ERROR);
    testDirWithDefault(EncodingIso8859dash1Tests.class, StandardCharsets.ISO_8859_1, SUCCESS);
  }

  @Test
  @Runtime
  @Category(SlowTests.class)
  public void testFileIso8859dash1() throws Exception {
    testByFile(EncodingIso8859dash1Tests.class, StandardCharsets.ISO_8859_1, StandardCharsets.UTF_8,      ERROR);
    testByFile(EncodingIso8859dash1Tests.class, StandardCharsets.ISO_8859_1, StandardCharsets.UTF_16,     ERROR);
    testByFile(EncodingIso8859dash1Tests.class, StandardCharsets.UTF_16,     StandardCharsets.ISO_8859_1, SUCCESS);
  }

  private void testFileWithDefault(@Nonnull Class<?> clazz, @Nonnull Charset charset, int expected)
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
          new File(AbstractTestTools.getTestRootDir(clazz.getPackage().getName()),
                   clazz.getSimpleName() + ".java")
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

  private void testDirWithDefault(@Nonnull Class<?> clazz, @Nonnull Charset charset, int expected)
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
          AbstractTestTools.getTestRootDir(clazz.getPackage().getName())
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

  private void testByFile(@Nonnull Class<?> clazz, @Nonnull Charset defaultCharset,
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
          new File(AbstractTestTools.getTestRootDir(clazz.getPackage().getName()),
                   clazz.getSimpleName() + ".java[" + fileCharset.name() + "]")
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

  private void run(@Nonnull Class<?> clazz, @Nonnull File binDirectory,
      int expected)
          throws SecurityException, IllegalArgumentException, RuntimeRunnerException {
    for (RuntimeRunner runner : AbstractTestTools.listRuntimeTestRunners(null)) {
      if (runner.runJUnit(
              new String[0],
              AbstractTestTools.JUNIT_RUNNER_NAME,
              new String[] {clazz.getName()},
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
