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

package com.android.jack.api.v01;

import com.android.jack.api.JackConfig;

import java.io.File;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * STOPSHIP
 */
public interface Api01Config extends JackConfig {

  @Nonnull
  void setReporter(@Nonnull ReporterKind reporterKind, @Nonnull OutputStream reporterStream)
      throws ConfigurationException;

  @Nonnull
  void setTypeImportCollisionPolicy(@Nonnull TypeCollisionPolicy typeImportCollisionPolicy)
      throws ConfigurationException;

  @Nonnull
  void setResourceImportCollisionPolicy(
      @Nonnull ResourceCollisionPolicy resourceImportCollisionPolicy) throws ConfigurationException;

  @Nonnull
  void setJavaSourceVersion(@Nonnull JavaSourceVersion javaSourceVersion)
      throws ConfigurationException;

  @Nonnull
  void setObfuscationMappingOutputFile(@Nonnull File obfuscationMappingOuputFile)
      throws ConfigurationException;

  @Nonnull
  void setClasspath(@Nonnull List<File> classpath) throws ConfigurationException;

  @Nonnull
  void setImportedJackLibraryFiles(@Nonnull List<File> importedJackLibraryFiles)
      throws ConfigurationException;

  @Nonnull
  void setMetaDirs(@Nonnull List<File> metaDirs) throws ConfigurationException;

  @Nonnull
  void setResourceDirs(@Nonnull List<File> resourceDirs) throws ConfigurationException;

  @Nonnull
  void setIncrementalDir(@Nonnull File incrementalDir) throws ConfigurationException;

  @Nonnull
  void setOutputDexDir(@Nonnull File outputDexDir) throws ConfigurationException;

  @Nonnull
  void setOutputJackFile(@Nonnull File outputJackFile) throws ConfigurationException;

  @Nonnull
  void setJarJarConfigFile(@Nonnull File jarjarConfigFile) throws ConfigurationException;

  @Nonnull
  void setProguardConfigFiles(@Nonnull List<File> proguardConfigFiles)
      throws ConfigurationException;

  @Nonnull
  void setEmitDebug(boolean emitDebug) throws ConfigurationException;

  @Nonnull
  void setMultiDexKind(@Nonnull MultiDexKind multiDexKind) throws ConfigurationException;

  @Nonnull
  void setVerbosityLevel(@Nonnull VerbosityLevel verbosityLevel) throws ConfigurationException;

  @Nonnull
  void setProcessorNames(@Nonnull List<String> processorNames) throws ConfigurationException;

  @Nonnull
  void setProcessorPath(@Nonnull List<File> processorPath) throws ConfigurationException;

  @Nonnull
  void setProcessorOptions(@Nonnull Map<String, String> processorOptions)
      throws ConfigurationException;

  @Nonnull
  void setSourceEntries(@Nonnull List<File> sourceEntries) throws ConfigurationException;

  @Nonnull
  void setProperty(@Nonnull String key, @Nonnull String value) throws ConfigurationException;

  @Nonnull
  Api01CompilationTask getTask() throws ConfigurationException;
}
