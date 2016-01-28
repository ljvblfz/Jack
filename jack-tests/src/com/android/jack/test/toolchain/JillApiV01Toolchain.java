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
import com.android.jill.api.v01.Api01Config;
import com.android.jill.api.v01.ConfigurationException;
import com.android.sched.util.log.LoggerFactory;

import java.io.File;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * This class defines a {@link JillApiToolchainBase} based on API v01.
 */
public class JillApiV01Toolchain extends JillApiToolchainBase {

  @Nonnull
  private Api01Config apiV01Config;

  JillApiV01Toolchain(@CheckForNull File jillPrebuilt, @Nonnull File jackPrebuilt,
      @Nonnull File refCompilerPrebuilt, @Nonnull File jarjarPrebuilt,
      @Nonnull File proguardPrebuilt) {
    super(jillPrebuilt, jackPrebuilt, Api01Config.class, refCompilerPrebuilt, jarjarPrebuilt,
        proguardPrebuilt);
    apiV01Config = (Api01Config) config;
  }

  @Override
  @Nonnull
  public JillApiV01Toolchain setVerbose(boolean isVerbose) {
    super.setVerbose(isVerbose);

    try {
      apiV01Config.setVerbose(isVerbose);
    } catch (ConfigurationException e) {
      throw new TestConfigurationException(e);
    }

    return this;
  }

  @Override
  @Nonnull
  public JillApiV01Toolchain setWithDebugInfos(boolean withDebugInfos) {
    super.setWithDebugInfos(withDebugInfos);

    try {
      apiV01Config.setDebugInfo(withDebugInfos);
    } catch (ConfigurationException e) {
      throw new TestConfigurationException(e);
    }

    return this;
  }

  @Override
  protected void executeJill(@Nonnull File in, @Nonnull File out) throws Exception {
    try {
    apiV01Config.setInputJavaBinaryFile(in);
    apiV01Config.setOutputJackFile(out);
    run();
    } catch (ConfigurationException e) {
      throw new TestConfigurationException(e);
    }
  }

  private void run() throws Exception {
    try {
      System.setOut(outRedirectStream);
      System.setErr(errRedirectStream);
      LoggerFactory.configure(LogLevel.ERROR);
      apiV01Config.getTask().run();
    } catch (ConfigurationException e) {
      Throwable t1 = e.getCause();
      if (t1 instanceof Exception) {
        throw (Exception) t1;
      } else {
        throw new AssertionError();
      }
    } finally {
      System.setOut(stdOut);
      System.setOut(stdErr);
      LoggerFactory.configure(LogLevel.ERROR);
    }
  }

}

