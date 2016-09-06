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
import com.android.jack.api.JackProvider;
import com.android.jack.api.v01.Api01Config;
import com.android.jack.api.v01.Cli01Config;
import com.android.jack.api.v01.impl.Cli01ConfigImpl;
import com.android.jack.api.v02.Api02Config;
import com.android.jack.api.v03.Api03Config;
import com.android.jack.api.v04.Api04Config;
import com.android.jack.api.v04.impl.Api04ConfigImpl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * This class provides an implementation to build the requested {@link JackConfig}
 */
public class JackProviderImpl implements JackProvider {
  @Nonnull
  private static final Map<Class<? extends JackConfig>, Class<? extends JackConfig>> map =
      new HashMap<Class<? extends JackConfig>, Class<? extends JackConfig>>();

  static {
    map.put(Api01Config.class, Api04ConfigImpl.class);
    map.put(Api02Config.class, Api04ConfigImpl.class);
    map.put(Api03Config.class, Api04ConfigImpl.class);
    map.put(Api04Config.class, Api04ConfigImpl.class);
    map.put(Cli01Config.class, Cli01ConfigImpl.class);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends JackConfig> T createConfig(Class<T> cls) throws ConfigNotSupportedException {
    Class<? extends JackConfig> clsImpl = map.get(cls);
    if (clsImpl == null) {
      throw new ConfigNotSupportedException(cls.getName() + " is not supported");
    }

    try {
      return (T) clsImpl.newInstance();
    } catch (InstantiationException e) {
      throw new AssertionError(e);
    } catch (IllegalAccessException e) {
      throw new AssertionError(e);
    }
  }

  @Override
  public Collection<Class<? extends JackConfig>> getSupportedConfigs() {
    return map.keySet();
  }

  @Override
  @Nonnull
  public <T extends JackConfig> boolean isConfigSupported(@Nonnull Class<T> cls) {
    return map.containsKey(cls);
  }

  @Override
  @Nonnull
  public String getCompilerReleaseName() {
    return Jack.getVersion().getReleaseName();
  }

  @Override
  @Nonnegative
  public int getCompilerReleaseCode() {
    return Jack.getVersion().getReleaseCode();
  }

  @Override
  @Nonnegative
  public int getCompilerSubReleaseCode() {
    return Jack.getVersion().getSubReleaseCode();
  }

  @Override
  @Nonnull
  public SubReleaseKind getCompilerSubReleaseKind() {
    switch (Jack.getVersion().getSubReleaseKind()) {
      case ENGINEERING:
        return SubReleaseKind.ENGINEERING;
      case PRE_ALPHA:
        return SubReleaseKind.PRE_ALPHA;
      case ALPHA:
        return SubReleaseKind.ALPHA;
      case BETA:
        return SubReleaseKind.BETA;
      case CANDIDATE:
        return SubReleaseKind.CANDIDATE;
      case RELEASE:
        return SubReleaseKind.RELEASE;
      default:
        throw new AssertionError(Jack.getVersion().getSubReleaseKind().name());
    }
  }

  @Override
  @CheckForNull
  public String getCompilerSourceCodeBase() {
    return Jack.getVersion().getCodeBase();
  }

  @Override
  public String getCompilerVersion() {
    return Jack.getVersion().getVersion();
  }

  @Override
  @CheckForNull
  public String getCompilerBuildId() {
    return Jack.getVersion().getBuildId();
  }
}