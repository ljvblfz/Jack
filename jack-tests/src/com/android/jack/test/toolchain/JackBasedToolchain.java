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
import com.android.jack.test.TestsProperties;

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
  protected List<File> resImport = new ArrayList<File>();
  @Nonnull
  protected List<File> metaImport = new ArrayList<File>();
  @Nonnull
  protected final Map<String, String> annotationProcessorOptions = new HashMap<String, String>();
  @CheckForNull
  protected String processorPath;

  @Nonnull
  public abstract JackBasedToolchain addProperty(@Nonnull String propertyName,
      @Nonnull String propertyValue);

  @Nonnull
  public final JackBasedToolchain addAnnotationProcessorOption(@Nonnull String propertyName,
      @Nonnull String propertyValue) {
    annotationProcessorOptions.put(propertyName, propertyValue);
    return this;
  }

  @Nonnull
  public final JackBasedToolchain setAnnotationProcessorPath(
      @Nonnull String processorPath) {
    this.processorPath = processorPath;
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
  public JackBasedToolchain addResourceDir(@Nonnull File resourceDir) {
    resImport.add(resourceDir);
    return this;
  }

  @Nonnull
  public JackBasedToolchain addMetaDir(@Nonnull File metaDir) {
    metaImport.add(metaDir);
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

  @Nonnull
  public abstract JackBasedToolchain setSanityChecks(boolean sanityChecks);

  @Override
  @Nonnull
  public JackBasedToolchain enableDxOptimizations() {
    addProperty(CodeItemBuilder.DEX_OPTIMIZE.getName(), "true");
    return this;
  }

  @Override
  @Nonnull
  public File[] getDefaultBootClasspath() {
    return new File[] {
        new File(TestsProperties.getJackRootDir(),
            "jack-tests/prebuilts/core-stubs-mini.jack"),
        new File(TestsProperties.getJackRootDir(),
            "jack-tests/prebuilts/junit4-lib.jack")};
  }

  @Override
  @Nonnull
  public String getLibraryExtension() {
    return ".jack";
  }

  @Override
  @Nonnull
  public String getLibraryElementsExtension() {
    return ".jayce";
  }

}
