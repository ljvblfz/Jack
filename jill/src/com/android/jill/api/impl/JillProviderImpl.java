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

package com.android.jill.api.impl;

import com.android.jill.Jill;
import com.android.jill.api.ConfigNotSupportedException;
import com.android.jill.api.JillConfig;
import com.android.jill.api.JillProvider;
import com.android.jill.api.v01.Api01Config;
import com.android.jill.api.v01.Cli01Config;
import com.android.jill.api.v01.impl.Api01ConfigImpl;
import com.android.jill.api.v01.impl.Cli01ConfigImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * This class provides an implementation to build the requested {@link JillConfig}
 */
public class JillProviderImpl implements JillProvider {

  @Override
  @Nonnull
  @SuppressWarnings("unchecked")
  public <T extends JillConfig> T createConfig(@Nonnull Class<T> cls)
      throws ConfigNotSupportedException {
    if (cls == Api01Config.class) {
      return (T) new Api01ConfigImpl();
    } else if (cls == Cli01Config.class) {
      return (T) new Cli01ConfigImpl();
    }

    throw new ConfigNotSupportedException(cls.getName() + " are not supported");
  }

  @Override
  @Nonnull
  public <T extends JillConfig> boolean isConfigSupported(@Nonnull Class<T> cls) {
    return cls == Api01Config.class || cls == Cli01Config.class;
  }

  @Override
  @Nonnull
  public Collection<Class<? extends JillConfig>> getSupportedConfigs() {
    List<Class<? extends JillConfig>> result = new ArrayList<Class<? extends JillConfig>>(2);
    result.add(Api01Config.class);
    result.add(Cli01Config.class);
    return result;
  }

  @Override
  @Nonnull
  public String getTranslatorVersion() {
     return Jill.getVersion().getVersion();
  }

  @Override
  @Nonnull
  public String getTranslatorReleaseName() {
    return Jill.getVersion().getReleaseName();
  }

  @Override
  public int getTranslatorReleaseCode() {
    return Jill.getVersion().getReleaseCode();
  }

  @Override
  public int getTranslatorSubReleaseCode() {
    return Jill.getVersion().getSubReleaseCode();
  }

  @Override
  @Nonnull
  public SubReleaseKind getTranslatorSubReleaseKind() {
    switch (Jill.getVersion().getSubReleaseKind()) {
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
        throw new AssertionError(Jill.getVersion().getSubReleaseKind().name());
    }
  }

  @Override
  @CheckForNull
  public String getTranslatorBuildId() {
    return Jill.getVersion().getBuildId();
  }

  @Override
  @CheckForNull
  public String getTranslatorSourceCodeBase() {
     return Jill.getVersion().getCodeBase();
  }
}