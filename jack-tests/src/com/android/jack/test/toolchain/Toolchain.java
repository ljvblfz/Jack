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

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.processing.Processor;

/**
 * A toolchain the ouptut of which can be redirected to user defined streams.
 */
public abstract class Toolchain implements IToolchain {

  protected boolean withDebugInfos = false;

  @CheckForNull
  protected Class<? extends Processor> annotationProcessorClass;

  /**
   * Java source level.
   */
  public static enum SourceLevel {
    JAVA_6,
    JAVA_7,
  }

  @Nonnull
  protected SourceLevel sourceLevel = SourceLevel.JAVA_6;
  @Nonnull
  protected List<File> staticLibs = Collections.emptyList();
  @Nonnull
  protected List<File> proguardFlags = Collections.emptyList();
  @CheckForNull
  protected File jarjarRules;

  @Nonnull
  protected PrintStream stdOut = System.out;
  @Nonnull
  protected PrintStream stdErr = System.err;
  @Nonnull
  protected PrintStream outRedirectStream = System.out;
  @Nonnull
  protected PrintStream errRedirectStream = System.err;

  protected boolean isVerbose = false;

  Toolchain() {}

  @Override
  @Nonnull
  public abstract void srcToExe(@CheckForNull String classpath, @Nonnull File out,
      boolean zipFile, @Nonnull File... sources) throws Exception;

  @Override
  @Nonnull
  public abstract void srcToLib(@CheckForNull String classpath, @Nonnull File out,
      boolean zipFiles, @Nonnull File... sources) throws Exception;

  @Override
  @Nonnull
  public abstract void libToExe(@Nonnull File in, @Nonnull File out, boolean zipFile)
      throws Exception;

  @Override
  @Nonnull
  public final void libToLib(@Nonnull File in, @Nonnull File out, boolean zipFiles)
      throws Exception {
    libToLib(new File[] {in}, out, zipFiles);
  }

  @Override
  @Nonnull
  public final void libToLib(@Nonnull List<File> in, @Nonnull File out, boolean zipFiles)
      throws Exception {
    libToLib(in.toArray(new File[in.size()]), out, zipFiles);
  }

  @Override
  @Nonnull
  public Toolchain setWithDebugInfos(boolean withDebugInfos) {
    this.withDebugInfos = withDebugInfos;
    return this;
  }

  @Override
  @Nonnull
  public final Toolchain setAnnotationProcessorClass(
      @Nonnull Class<? extends Processor> annotationProcessorClass) {
    this.annotationProcessorClass = annotationProcessorClass;
    return this;
  }

  @Override
  @Nonnull
  public Toolchain setSourceLevel(@Nonnull SourceLevel sourceLevel) {
    this.sourceLevel = sourceLevel;
    return this;
  }

  @Override
  @Nonnull
  public final Toolchain addProguardFlags(@Nonnull File... proguardFlags) {
    if (this.proguardFlags == Collections.EMPTY_LIST) {
      this.proguardFlags = new ArrayList<File>(proguardFlags.length);
    }
    Collections.addAll(this.proguardFlags, proguardFlags);
    return this;
  }

  @Override
  @Nonnull
  public final Toolchain setJarjarRules(@Nonnull File jarjarRules) {
    this.jarjarRules = jarjarRules;
    return this;
  }

  @Override
  @Nonnull
  public final Toolchain addStaticLibs(@Nonnull File... staticLibs) {
    if (this.staticLibs == Collections.EMPTY_LIST) {
      this.staticLibs = new ArrayList<File>(staticLibs.length);
    }
    Collections.addAll(this.staticLibs, staticLibs);
    return this;
  }

  @Override
  @Nonnull
  public final Toolchain setOutputStream(@Nonnull OutputStream outputStream) {
    outRedirectStream = new PrintStream(outputStream);
    return this;
  }

  @Override
  @Nonnull
  public final Toolchain setErrorStream(@Nonnull OutputStream errorStream) {
    errRedirectStream = new PrintStream(errorStream);
    return this;
  }

  @Override
  @Nonnull
  public Toolchain setVerbose(boolean isVerbose) {
    this.isVerbose = isVerbose;
    return this;
  }
}
