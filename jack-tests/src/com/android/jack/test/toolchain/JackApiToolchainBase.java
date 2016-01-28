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
  private JackProvider configProvider;

  @Nonnull
  private String releaseName;
  @Nonnegative
  private int releaseCode;
  @Nonnull
  private SubReleaseKind subReleaseKind;
  @Nonnegative
  private int subSubReleaseCode;
  @Nonnull
  private String compilerVersion;

  @Nonnull
  public String getReleaseName() {
    return releaseName;
  }

  @Nonnegative
  public int getReleaseCode() {
    return releaseCode;
  }

  @Nonnull
  public SubReleaseKind getSubReleaseKind() {
    return subReleaseKind;
  }

  @Nonnegative
  public int getSubSubReleaseCode() {
    return subSubReleaseCode;
  }

  @Nonnull
  public String getCompilerVersion() {
    return compilerVersion;
  }

  protected <T extends JackConfig> JackApiToolchainBase(@CheckForNull File jackPrebuilt,
      @Nonnull Class<T> jackConfig) {

    try {
      ClassLoader classLoader = null;

      ServiceLoader<JackProvider> serviceLoader;

      if (jackPrebuilt != null) {
        classLoader = URLClassLoader.newInstance(
            new URL[] {jackPrebuilt.toURI().toURL()}, JackApiToolchainBase.class.getClassLoader());
        serviceLoader = ServiceLoader.load(JackProvider.class, classLoader);
      } else {
        serviceLoader = ServiceLoader.load(JackProvider.class);
      }

      configProvider = serviceLoader.iterator().next();

      if (jackPrebuilt != null && configProvider.getClass().getClassLoader() != classLoader) {
        throw new TestConfigurationException("Jack compiler is not loaded from '" + jackPrebuilt
            + "'. Unset jack prebuilt property in configuration file");
      }

    } catch (MalformedURLException e1) {
      throw new TestConfigurationException(e1);
    } catch (NoSuchElementException e) {
      if (jackPrebuilt == null) {
        throw new TestConfigurationException(
            "JackProvider could not be loaded. Ensure Jack is present on classpath or prebuilt is"
            + " specified in configuration file", e);
      } else {
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

}
