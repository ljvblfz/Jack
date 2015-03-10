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
import com.android.jack.api.JackConfigProvider;
import com.android.jack.api.v01.VerbosityLevel;
import com.android.jack.shrob.spec.Flags;
import com.android.jack.test.TestConfigurationException;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * This class implements a {@link JackBasedToolchain} by calling Jack via API.
 */
public abstract class JackApiToolchainBase extends JackBasedToolchain {

  @Nonnull
  protected JackConfig config;

  @Nonnull
  private String compilerCodeName;
  @Nonnull
  private String compilerVersion;
  @Nonnull
  private String compilerBuildId;
  @Nonnull
  private String compilerCodeBase;

  @CheckForNull
  protected File incrementalFolder;
  @Nonnull
  protected VerbosityLevel verbosityLevel = VerbosityLevel.WARNING;

  @CheckForNull
  private static JackConfigProvider configProvider;

  public String getCompilerCodeName() {
    return compilerCodeName;
  }

  public String getCompilerVersion() {
    return compilerVersion;
  }

  public String getCompilerBuildId() {
    return compilerBuildId;
  }

  public String getCompilerCodeBase() {
    return compilerCodeBase;
  }

  protected <T extends JackConfig> JackApiToolchainBase(@Nonnull File jackPrebuilt,
      @Nonnull Class<T> jackConfig) {
    try {

      if (configProvider == null) {
        ClassLoader classLoader = URLClassLoader.newInstance(
            new URL[] {jackPrebuilt.toURI().toURL()}, JackApiToolchainBase.class.getClassLoader());
        Class<? extends JackConfigProvider> confProviderClass = Class.forName(
            JackConfigProvider.CLASS_NAME, true, classLoader).asSubclass(JackConfigProvider.class);
        configProvider = confProviderClass.getConstructor().newInstance();
      }

      assert configProvider != null;

      compilerCodeName = configProvider.getCompilerCodeName();
      compilerVersion = configProvider.getCompilerVersion();
      compilerBuildId = configProvider.getCompilerBuildId();
      compilerCodeBase = configProvider.getCompilerCodeBase();

      config = configProvider.getConfig(jackConfig);

    } catch (ConfigNotSupportedException e) {
      throw new TestConfigurationException("Jack API v01 not supported", e);
    } catch (MalformedURLException e) {
      throw new TestConfigurationException(e);
    } catch (ClassNotFoundException e) {
      throw new TestConfigurationException(e);
    } catch (InstantiationException e) {
      throw new TestConfigurationException(e);
    } catch (IllegalAccessException e) {
      throw new TestConfigurationException(e);
    } catch (IllegalArgumentException e) {
      throw new TestConfigurationException(e);
    } catch (InvocationTargetException e) {
      throw new TestConfigurationException(e);
    } catch (NoSuchMethodException e) {
      throw new TestConfigurationException(e);
    } catch (SecurityException e) {
      throw new TestConfigurationException(e);
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
