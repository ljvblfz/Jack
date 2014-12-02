/*
 * Copyright (C) 2014 The Android Open Source Project
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

import com.android.jack.backend.dex.DexFileWriter;
import com.android.jack.backend.dex.MultiDexLegacy;
import com.android.jack.backend.dex.rop.CodeItemBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Defines an {@link AndroidToolchain} built on Jack.
 */
public abstract class JackBasedToolchain extends AndroidToolchain {

  /**
   * Available mode for the multidex feature
   */
  public enum MultiDexKind {
    NONE,
    NATIVE,
    LEGACY
  }

  @Nonnull
  protected final Map<String, String> properties = new HashMap<String, String>();
  @CheckForNull
  protected File annotationProcessorOutDir;
  @Nonnull
  protected List<String> extraEcjArgs = new ArrayList<String>();

  @Nonnull
  public final JackBasedToolchain addProperty(@Nonnull String propertyName,
      @Nonnull String propertyValue) {
    properties.put(propertyName, propertyValue);
    return this;
  }

  public final JackBasedToolchain setMultiDexKind(@Nonnull MultiDexKind kind) {
    switch (kind) {
      case NATIVE:
        addProperty(DexFileWriter.DEX_WRITING_POLICY.getName(), "multidex");
        break;
      case LEGACY:
        addProperty(DexFileWriter.DEX_WRITING_POLICY.getName(), "multidex");
        addProperty(MultiDexLegacy.MULTIDEX_LEGACY.getName(), "true");
        break;
      case NONE:
        break;
      default:
        throw new AssertionError("Unsupported multi dex kind: '" + kind.name() + "'");
    }
    return this;
  }

  @Nonnull
  public final JackBasedToolchain addEcjArgs(@Nonnull String arg) {
    extraEcjArgs.add(arg);
    return this;
  }

  @Nonnull
  public final JackBasedToolchain setAnnotationProcessorOutDir(
      @Nonnull File annotationProcessorOutDir) {
    this.annotationProcessorOutDir = annotationProcessorOutDir;
    return this;
  }

  @Override
  @Nonnull
  public JackBasedToolchain setSourceLevel(@Nonnull SourceLevel sourceLevel) {
    super.setSourceLevel(sourceLevel);
    switch (sourceLevel) {
      case JAVA_6:
        addProperty("jack.java.source.version", "1.6");
        break;
      case JAVA_7:
        addProperty("jack.java.source.version", "1.7");
        break;
      default:
        throw new AssertionError("Unkown level: '" + sourceLevel.toString() + "'");
    }
    return this;
  }

  @Nonnull
  public abstract JackBasedToolchain setIncrementalFolder(@Nonnull File incrementalFolder);

  @Override
  @Nonnull
  public JackBasedToolchain disableDxOptimizations() {
    addProperty(CodeItemBuilder.DEX_OPTIMIZE.getName(), "false");
    return this;
  }

  @Override
  @Nonnull
  public JackBasedToolchain enableDxOptimizations() {
    addProperty(CodeItemBuilder.DEX_OPTIMIZE.getName(), "true");
    return this;
  }

  @Override
  @Nonnull
  public File[] getDefaultBootClasspath() {
    return new File[] {new File(AbstractTestTools.getJackRootDir(),
        "toolchain/jack/jack-tests/libs/core-stubs-mini.jar"), new File(
        AbstractTestTools.getJackRootDir(),
        "toolchain/jack/jack-tests/prebuilts/junit4-targetdex-jack.zip")};
  }
}
