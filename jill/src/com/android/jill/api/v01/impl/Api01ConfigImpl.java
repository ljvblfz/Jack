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

package com.android.jill.api.v01.impl;

import com.android.jill.Jill;
import com.android.jill.Options;
import com.android.jill.api.v01.Api01Config;
import com.android.jill.api.v01.Api01TranslationTask;
import com.android.jill.api.v01.ConfigurationException;
import com.android.jill.utils.FileUtils;

import java.io.File;

import javax.annotation.Nonnull;

/**
 * This class provides the version 01 implementation of Jill API.
 */
public class Api01ConfigImpl implements Api01Config {

  @Nonnull
  private final Options options;

  public Api01ConfigImpl() {
    options = new Options();
  }

  @Override
  @Nonnull
  public Api01TranslationTask getTask() {
    return new Api01TranslationTaskImpl(options);
  }

  private static class Api01TranslationTaskImpl implements Api01TranslationTask {

    @Nonnull
    private final Options options;

    public Api01TranslationTaskImpl(@Nonnull Options options) {
      this.options = options;
    }

    @Override
    public void run() {
      Jill.process(options);
    }

  }

  @Override
  public void setVerbose(boolean isVerbose) {
    options.setVerbose(isVerbose);
  }

  @Override
  public void setInputJavaBinaryFile(@Nonnull File input) throws ConfigurationException {
    if (!input.exists()) {
      throw new ConfigurationException("Input file does not exist: " + input.getPath());
    }
    if (!input.getAbsoluteFile().isFile()) {
      throw new ConfigurationException("Input is not a file: " + input.getPath());
    }
    if (!FileUtils.isJarFile(input)) {
      throw new ConfigurationException("Unsupported file type: " + input.getName());
    }
    options.setBinaryFile(input);
  }

  @Override
  public void setOutputJackFile(@Nonnull File outputJackFile) {
    options.setOutput(outputJackFile);
  }

  @Override
  public void setDebugInfo(boolean debugInfo) {
    options.setEmitDebugInfo(debugInfo);
  }

}
