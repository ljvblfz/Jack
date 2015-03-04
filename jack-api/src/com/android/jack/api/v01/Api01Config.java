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

  void setReporter(@Nonnull ReporterKind reporterKind, @Nonnull OutputStream reporterStream)
      throws ConfigurationException;

  void setTypeImportCollisionPolicy(@Nonnull TypeCollisionPolicy typeImportCollisionPolicy)
      throws ConfigurationException;

  void setResourceImportCollisionPolicy(
      @Nonnull ResourceCollisionPolicy resourceImportCollisionPolicy) throws ConfigurationException;

  void setJavaSourceVersion(@Nonnull JavaSourceVersion javaSourceVersion)
      throws ConfigurationException;

  void setObfuscationMappingOutputFile(@Nonnull File obfuscationMappingOuputFile)
      throws ConfigurationException;

  void setClasspath(@Nonnull List<File> classpath) throws ConfigurationException;

  void setImportedJackLibraryFiles(@Nonnull List<File> importedJackLibraryFiles)
      throws ConfigurationException;

  void setMetaDirs(@Nonnull List<File> metaDirs) throws ConfigurationException;

  void setResourceDirs(@Nonnull List<File> resourceDirs) throws ConfigurationException;

  void setIncrementalDir(@Nonnull File incrementalDir) throws ConfigurationException;

  void setOutputDexDir(@Nonnull File outputDexDir) throws ConfigurationException;

  void setOutputJackFile(@Nonnull File outputJackFile) throws ConfigurationException;

  void setJarJarConfigFile(@Nonnull File jarjarConfigFile) throws ConfigurationException;

  void setProguardConfigFiles(@Nonnull List<File> proguardConfigFiles)
      throws ConfigurationException;

  void setDebugInfoLevel(@Nonnull DebugInfoLevel debugInfoLevel) throws ConfigurationException;

  void setMultiDexKind(@Nonnull MultiDexKind multiDexKind) throws ConfigurationException;

  void setVerbosityLevel(@Nonnull VerbosityLevel verbosityLevel) throws ConfigurationException;

  void setProcessorNames(@Nonnull List<String> processorNames) throws ConfigurationException;

  void setProcessorPath(@Nonnull List<File> processorPath) throws ConfigurationException;

  void setProcessorOptions(@Nonnull Map<String, String> processorOptions)
      throws ConfigurationException;

  void setSourceEntries(@Nonnull List<File> sourceEntries) throws ConfigurationException;

  void setProperty(@Nonnull String key, @Nonnull String value) throws ConfigurationException;

  @Nonnull
  Api01CompilationTask getTask() throws ConfigurationException;
}
