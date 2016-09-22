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
import com.android.jack.api.ConfigNotSupportedAnymoreException;
import com.android.jack.api.ConfigNotSupportedException;
import com.android.jack.api.JackConfig;
import com.android.jack.api.JackProvider;
import com.android.jack.api.ResourceController;
import com.android.jack.api.UnknownConfigException;
import com.android.jack.api.v01.Api01Config;
import com.android.jack.api.v01.Cli01Config;
import com.android.jack.api.v01.impl.Api01Feature;
import com.android.jack.api.v01.impl.Cli01ConfigImpl;
import com.android.jack.api.v02.Api02Config;
import com.android.jack.api.v02.impl.Api02Feature;
import com.android.jack.api.v03.Api03Config;
import com.android.jack.api.v03.impl.Api03Feature;
import com.android.jack.api.v04.Api04Config;
import com.android.jack.api.v04.impl.Api04ConfigImpl;
import com.android.jack.api.v04.impl.Api04Feature;
import com.android.jack.management.CleanCodeRequest;
import com.android.jack.management.CleanDiskRequest;
import com.android.jack.management.CleanMemoryRequest;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * This class provides an implementation to build the requested {@link JackConfig}
 */
public class JackProviderImpl implements JackProvider, ResourceController {
  @Nonnull
  private static final Map<Class<? extends JackConfig>, Class<? extends JackConfigImpl>> impl =
      new HashMap<>();
  @Nonnull
  private static final Map<Class<? extends JackConfig>, Class<? extends ApiFeature>> features =
      new HashMap<>();

  static {
    impl.put(Api01Config.class, Api04ConfigImpl.class);
    impl.put(Api02Config.class, Api04ConfigImpl.class);
    impl.put(Api03Config.class, Api04ConfigImpl.class);
    impl.put(Api04Config.class, Api04ConfigImpl.class);
    impl.put(Cli01Config.class, Cli01ConfigImpl.class);

    features.put(Api01Config.class, Api01Feature.class);
    features.put(Api02Config.class, Api02Feature.class);
    features.put(Api03Config.class, Api03Feature.class);
    features.put(Api04Config.class, Api04Feature.class);
    features.put(Cli01Config.class, Api04Feature.class);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends JackConfig> T createConfig(Class<T> cls) throws ConfigNotSupportedException {
    if (!impl.containsKey(cls)) {
      throw new UnknownConfigException(cls.getName() + " is not supported");
    }

    Class<? extends JackConfigImpl> clsImpl = impl.get(cls);
    if (clsImpl == null) {
      throw new ConfigNotSupportedAnymoreException(cls.getName() + " is not supported anymore");
    }

    try {
      JackConfigImpl config = clsImpl.newInstance();
      assert(features.containsKey(cls));
      config.setApi(features.get(cls));

      return (T) config;
    } catch (InstantiationException e) {
      throw new AssertionError(e);
    } catch (IllegalAccessException e) {
      throw new AssertionError(e);
    }
  }

  @Override
  public Collection<Class<? extends JackConfig>> getSupportedConfigs() {
    return impl.keySet();
  }

  @Override
  @Nonnull
  public <T extends JackConfig> boolean isConfigSupported(@Nonnull Class<T> cls) {
    return impl.containsKey(cls);
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

  @Override
  public void clean(@Nonnull Set<Category> categories, @Nonnull Set<Impact> impacts) {
    EnumSet<com.android.jack.management.Impact> impactsInternal =
        EnumSet.noneOf(com.android.jack.management.Impact.class);

    // Convert API into Jack internal
    if (impacts.contains(Impact.LATENCY)) {
      impactsInternal.add(com.android.jack.management.Impact.LATENCY);
    }
    if (impacts.contains(Impact.PERFORMANCE)) {
      impactsInternal.add(com.android.jack.management.Impact.PERFORMANCE);
    }

    // Post events
    if (categories.contains(Category.CODE)) {
      Jack.getResourceRequestBus().post(new CleanCodeRequest(impactsInternal));
    }

    if (categories.contains(Category.MEMORY)) {
      Jack.getResourceRequestBus().post(new CleanMemoryRequest(impactsInternal));
    }

    if (categories.contains(Category.DISK)) {
      Jack.getResourceRequestBus().post(new CleanDiskRequest(impactsInternal));
    }
  }

  @Override
  public EnumSet<Category> getSupportedCategories() {
    // Here, we cannot use EnumSet.allOf(...) because we want all elements that Jack
    // supports, and not all elements that jack-api has.
    return EnumSet.of(Category.CODE, Category.DISK, Category.MEMORY);
  }

  @Override
  public EnumSet<Impact> getSupportedImpacts() {
    // Here, we cannot use EnumSet.allOf(...) because we want all elements that Jack
    // supports, and not all elements that jack-api has.
    return EnumSet.of(Impact.LATENCY, Impact.PERFORMANCE);
  }
}