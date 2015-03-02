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

package com.android.jack.api.impl;

import com.android.jack.Jack;
import com.android.jack.api.ConfigNotSupportedException;
import com.android.jack.api.JackConfig;
import com.android.jack.api.JackConfigProvider;
import com.android.jack.api.v01.Api01Config;
import com.android.jack.api.v01.impl.Api01ConfigImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class provides an implementation to build the requested {@link JackConfig}
 */
public class JackConfigProviderImpl implements JackConfigProvider {
  @Override
  @SuppressWarnings("unchecked")
  public <T extends JackConfig> T getConfig(Class<T> cls) throws ConfigNotSupportedException {
    if (cls == Api01Config.class) {
      return (T) new Api01ConfigImpl();
    }

    throw new ConfigNotSupportedException(cls.getName() + " are not supported");
  }

  @Override
  public String getCompilerVersion() {
    return Jack.getVersionString();
  }

  @Override
  public String getCompilerBuildId() {
    return Jack.getVersionString();
  }

  @Override
  public String getCompilerCodeBase() {
    return Jack.getVersionString();
  }

  @Override
  public String getCompilerCodeName() {
    return Jack.getVersionString();
  }

  @Override
  public Collection<Class<? extends JackConfig>> getSupportedConfigs() {
    List<Class<? extends JackConfig>> result = new ArrayList<Class<? extends JackConfig>>();
    result.add(Api01Config.class);
    return result;
  }
}