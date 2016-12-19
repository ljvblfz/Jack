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

package com.android.jack.coverage;

import com.android.jack.test.TestsProperties;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.JackApiV03Toolchain;
import com.android.jack.test.toolchain.JackApiV04Toolchain;
import com.android.jack.test.toolchain.JackBasedToolchain;
import com.android.jack.test.toolchain.JackCliToolchain;
import com.android.sched.util.file.CannotChangePermissionException;
import com.android.sched.util.file.CannotCreateFileException;

import java.io.File;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * A class to set up a {@link JackBasedToolchain} with code coverage.
 */
public class CoverageToolchainBuilder<T extends JackBasedToolchain> {
  @Nonnull
  private static final String COVERAGE_PLUGIN_NAME = "com.android.jack.coverage.CodeCoverage";

  @Nonnull
  private static final String JACOCO_RUNTIME_PACKAGE = "org.jacoco.agent.rt.internal_04864a1";

  @Nonnull
  private final T toolchain;

  private CoverageToolchainBuilder(@Nonnull T toolchain) {
    this.toolchain = toolchain;
  }

  /**
   * Creates a {@link CoverageToolchainBuilder} for the given {@code toolchain}.
   *
   * @param toolchain the toolchain to configure for code coverage.
   * @return a new builder instance
   */
  public static <T extends JackBasedToolchain> CoverageToolchainBuilder<T> create(
      @Nonnull T toolchain) {
    return new CoverageToolchainBuilder<T>(toolchain);
  }

  /**
   * Sets coverage include filter.
   *
   * @param includeFilter a non-null include filter string
   * @return this instance
   */
  @Nonnull
  public CoverageToolchainBuilder<T> setIncludeFilter(@Nonnull String includeFilter) {
    toolchain.addProperty("jack.coverage.jacoco.include", includeFilter);
    return this;
  }

  /**
   * Sets coverage exclude filter.
   *
   * @param excludeFilter a non-null exclude filter string
   * @return this instance
   */
  @Nonnull
  public CoverageToolchainBuilder<T> setExcludeFilter(@Nonnull String excludeFilter) {
    toolchain.addProperty("jack.coverage.jacoco.exclude", excludeFilter);
    return this;
  }

  /**
   * Applies code coverage configuration to the toolchain. It returns the coverage metadata file
   * that will be produced by the compilation.
   *
   * This file is a temporary file that will be automatically deleted after the test.
   *
   * @return the coverage metadata file
   */
  @Nonnull
  public File build() throws Exception {
    // Enable code coverage
    toolchain.addProperty("jack.coverage", "true");

    // Set output metadata file
    File coverageMetadataFile = createTempCoverageMetadataFile();
    toolchain.addProperty("jack.coverage.metadata.file", coverageMetadataFile.getAbsolutePath());

    // Set up jacoco library
    toolchain.addProperty("jack.coverage.jacoco.package", JACOCO_RUNTIME_PACKAGE);
    toolchain.addToClasspath(getJacocoAgentLib());

    // Enable coverage plugin
    File pluginFile = getCodeCoveragePluginFile();
    List<File> pluginPath = Collections.singletonList(pluginFile);
    List<String> pluginNames = Collections.singletonList(COVERAGE_PLUGIN_NAME);
    if (toolchain instanceof JackCliToolchain) {
      JackCliToolchain cliToolchain = (JackCliToolchain) toolchain;
      cliToolchain.setPluginPath(pluginPath);
      cliToolchain.setPluginNames(pluginNames);
    } else {
      // TODO: need to rework API toolchain hierarchy in test framework to avoid these if/else.
      if (toolchain instanceof JackApiV03Toolchain) {
        JackApiV03Toolchain jackApiV03 = (JackApiV03Toolchain) toolchain;
        jackApiV03.setPluginPath(pluginPath);
        jackApiV03.setPluginNames(pluginNames);
      } else if (toolchain instanceof JackApiV04Toolchain) {
        JackApiV04Toolchain jackApiV04 = (JackApiV04Toolchain) toolchain;
        jackApiV04.setPluginPath(pluginPath);
        jackApiV04.setPluginNames(pluginNames);
      } else {
        throw new AssertionError("Unsupported toolchain: " + toolchain.getClass().getName());
      }
    }
    return coverageMetadataFile;
  }

  @Nonnull
  private static File createTempCoverageMetadataFile()
      throws CannotCreateFileException, CannotChangePermissionException {
    return AbstractTestTools.createTempFile("coverage", ".metadata");
  }

  @Nonnull
  private static File getJacocoAgentLib() {
    return new File(TestsProperties.getJackRootDir(),
        "jacoco/org.jacoco.agent.rt-0.7.5.201505241946-all.jar");
  }

  @Nonnull
  private static File getCodeCoveragePluginFile() {
    return new File(TestsProperties.getJackRootDir(),
        "jack-coverage/dist/jack-coverage-plugin.jar");
  }
}
