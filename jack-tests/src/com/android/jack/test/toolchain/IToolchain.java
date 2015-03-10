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

import com.android.jack.test.toolchain.Toolchain.SourceLevel;

import java.io.File;
import java.io.OutputStream;
import java.util.List;

import javax.annotation.Nonnull;


/**
 * Abstraction of a toolchain which takes source files and produce libraries and executables.
 */
public interface IToolchain {

  void srcToExe(@Nonnull File out, boolean zipFile, @Nonnull File... sources) throws Exception;

  void srcToLib(@Nonnull File out, boolean zipFiles, @Nonnull File... sources) throws Exception;

  void libToExe(@Nonnull File in, @Nonnull File out, boolean zipFile) throws Exception;

  void libToExe(@Nonnull File[] in, @Nonnull File out, boolean zipFile) throws Exception;

  void libToExe(@Nonnull List<File> in, @Nonnull File out, boolean zipFile) throws Exception;

  void libToLib(@Nonnull File in, @Nonnull File out, boolean zipFiles) throws Exception;

  void libToLib(@Nonnull File[] in, @Nonnull File out, boolean zipFiles) throws Exception;

  void libToLib(@Nonnull List<File> in, @Nonnull File out, boolean zipFiles) throws Exception;

  @Nonnull
  IToolchain addStaticLibs(@Nonnull File... staticLibs);

  @Nonnull
  File[] getDefaultBootClasspath();

  @Nonnull
  String getExeExtension();

  @Nonnull
  String getLibraryExtension();

  @Nonnull
  IToolchain addToClasspath(@Nonnull File... classpath);

  @Nonnull
  String getLibraryElementsExtension();

  @Nonnull
  IToolchain setWithDebugInfos(boolean withDebugInfos);

  @Nonnull
  IToolchain setAnnotationProcessorClasses(
      @Nonnull List<String> annotationProcessorClasses);

  @Nonnull
  IToolchain setSourceLevel(@Nonnull SourceLevel sourceLevel);

  @Nonnull
  IToolchain addProguardFlags(@Nonnull File... proguardFlags);

  @Nonnull
  IToolchain setJarjarRules(@Nonnull File jarjarRules);

  @Nonnull
  IToolchain setOutputStream(@Nonnull OutputStream outputStream);

  @Nonnull
  IToolchain setErrorStream(@Nonnull OutputStream errorStream);

  @Nonnull
  IToolchain setVerbose(boolean isVerbose);

}
