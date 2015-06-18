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
import com.android.jack.api.v01.impl.Api01ConfigImpl;
import com.android.jack.api.v01.impl.Cli01ConfigImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * This class provides an implementation to build the requested {@link JackConfig}
 */
public class JackProviderImpl implements JackProvider {
  @Override
  @SuppressWarnings("unchecked")
  public <T extends JackConfig> T createConfig(Class<T> cls) throws ConfigNotSupportedException {
    if (cls == Api01Config.class) {
      return (T) new Api01ConfigImpl();
    } else if (cls == Cli01Config.class) {
      return (T) new Cli01ConfigImpl();
    }

    throw new ConfigNotSupportedException(cls.getName() + " are not supported");
  }

  @Override
  public Collection<Class<? extends JackConfig>> getSupportedConfigs() {
    List<Class<? extends JackConfig>> result = new ArrayList<Class<? extends JackConfig>>(1);
    result.add(Api01Config.class);
    result.add(Cli01Config.class);
    return result;
  }

  @Override
  @Nonnull
  public <T extends JackConfig> boolean isConfigSupported(@Nonnull Class<T> cls) {
    return cls == Api01Config.class || cls == Cli01Config.class;
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