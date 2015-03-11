/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.jack.test.toolchain;

import com.android.jack.api.ConfigNotSupportedException;
import com.android.jack.api.JackConfig;
import com.android.jack.api.JackProvider;
import com.android.jack.api.JackProvider.SubReleaseKind;
import com.android.jack.api.v01.VerbosityLevel;
import com.android.jack.shrob.spec.Flags;
import com.android.jack.test.TestConfigurationException;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.NoSuchElementException;
import java.util.ServiceLoader;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * This class implements a {@link JackBasedToolchain} by calling Jack via API.
 */
public abstract class JackApiToolchainBase extends JackBasedToolchain {

  @Nonnull
  protected JackConfig config;

  @CheckForNull
  protected File incrementalFolder;
  @Nonnull
  protected VerbosityLevel verbosityLevel = VerbosityLevel.WARNING;

  @CheckForNull
  private static JackProvider configProvider;

  @Nonnull
  private static String releaseName;
  @Nonnegative
  private static int releaseCode;
  @Nonnull
  private static SubReleaseKind subReleaseKind;
  @Nonnegative
  private static int subSubReleaseCode;
  @Nonnull
  private static String compilerVersion;


  @Nonnull
  public static String getReleaseName() {
    return releaseName;
  }

  @Nonnegative
  public static int getReleaseCode() {
    return releaseCode;
  }

  @Nonnull
  public static SubReleaseKind getSubReleaseKind() {
    return subReleaseKind;
  }

  @Nonnegative
  public static int getSubSubReleaseCode() {
    return subSubReleaseCode;
  }

  @Nonnull
  public static String getCompilerVersion() {
    return compilerVersion;
  }

  protected <T extends JackConfig> JackApiToolchainBase(@Nonnull File jackPrebuilt,
      @Nonnull Class<T> jackConfig) {

    if (configProvider == null) {
      try {
        ClassLoader classLoader = URLClassLoader.newInstance(
            new URL[] {jackPrebuilt.toURI().toURL()}, JackApiToolchainBase.class.getClassLoader());
        ServiceLoader<JackProvider> serviceLoader =
            ServiceLoader.load(JackProvider.class, classLoader);
        configProvider = serviceLoader.iterator().next();
      } catch (MalformedURLException e1) {
        throw new TestConfigurationException(e1);
      } catch (NoSuchElementException e) {
        throw new TestConfigurationException(e);
      }
    }

    assert configProvider != null;

    releaseName = configProvider.getCompilerReleaseName();
    releaseCode = configProvider.getCompilerReleaseCode();
    subReleaseKind = configProvider.getCompilerSubReleaseKind();
    subSubReleaseCode = configProvider.getCompilerSubReleaseCode();
    compilerVersion = configProvider.getCompilerVersion();

    try {

      config = configProvider.createConfig(jackConfig);

    } catch (ConfigNotSupportedException e) {
      throw new TestConfigurationException("Jack API v01 not supported", e);
    }
  }

  @Override
  @Nonnull
  public JackApiToolchainBase setIncrementalFolder(@Nonnull File incrementalFolder) {
    this.incrementalFolder = incrementalFolder;
    return this;
  }

  @Nonnull
  public abstract JackApiToolchainBase setShrobFlags(@Nonnull Flags shrobFlags);

}
