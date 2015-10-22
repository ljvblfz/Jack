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

import com.android.jack.test.TestConfigurationException;
import com.android.jill.api.ConfigNotSupportedException;
import com.android.jill.api.JillConfig;
import com.android.jill.api.JillProvider;
import com.android.jill.api.JillProvider.SubReleaseKind;

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
 * This class defines a toolchain that uses Jill via jill-api, and rely on
 * Jack and legacy compiler CLIs for complementary steps.
 */
public abstract class JillApiToolchainBase extends JillBasedToolchain {

  @Nonnull
  protected JillConfig config;

  @CheckForNull
  private static JillProvider configProvider;

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


  protected <T extends JillConfig> JillApiToolchainBase(@Nonnull File jillPrebuilt,
      @Nonnull File jackPrebuilt, @Nonnull Class<T> jillConfig, @Nonnull File refCompilerPrebuilt,
      @Nonnull File jarjarPrebuilt, @Nonnull File proguardPrebuilt) {
    super(jillPrebuilt, jackPrebuilt, refCompilerPrebuilt, jarjarPrebuilt, proguardPrebuilt);

    if (configProvider == null) {
      try {
        ClassLoader classLoader = URLClassLoader.newInstance(
            new URL[] {jillPrebuilt.toURI().toURL()}, JillApiToolchainBase.class.getClassLoader());
        ServiceLoader<JillProvider> serviceLoader =
            ServiceLoader.load(JillProvider.class, classLoader);
        configProvider = serviceLoader.iterator().next();
      } catch (MalformedURLException e1) {
        throw new TestConfigurationException(e1);
      } catch (NoSuchElementException e) {
        throw new TestConfigurationException(e);
      }
    }

    assert configProvider != null;

    releaseName = configProvider.getTranslatorReleaseName();
    releaseCode = configProvider.getTranslatorReleaseCode();
    subReleaseKind = configProvider.getTranslatorSubReleaseKind();
    subSubReleaseCode = configProvider.getTranslatorSubReleaseCode();
    compilerVersion = configProvider.getTranslatorVersion();

    try {

      config = configProvider.createConfig(jillConfig);

    } catch (ConfigNotSupportedException e) {
      throw new TestConfigurationException("Jill API v01 not supported", e);
    }
  }

}

