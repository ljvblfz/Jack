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

package com.android.jack.api.v03.impl;

import com.google.common.base.Joiner;

import com.android.jack.api.v02.impl.Api02ConfigImpl;
import com.android.jack.api.v03.Api03Config;

import java.io.File;
import java.util.Collection;

import javax.annotation.Nonnull;

/**
 * A configuration implementation for API level 03 of the Jack compiler.
 */
public class Api03ConfigImpl extends Api02ConfigImpl implements Api03Config {
  public Api03ConfigImpl() {
    super();
  }

  @Override
  public void setPluginPath(@Nonnull Collection<File> pluginPath) {
    options.setPluginPath(Joiner.on(File.pathSeparator).join(pluginPath));
  }

  @Override
  public void setPluginNames(@Nonnull Collection<String> pluginNames) {
    options.setPluginNames(Joiner.on(',').join(pluginNames));
  }
}
