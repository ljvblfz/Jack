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

package com.android.jack.test.runtime;

import com.android.jack.test.helper.FileChecker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * This class hold the information needed by the runtime tests framework to execute
 * a test.
 */
public class RuntimeTestInfo {

  @Nonnull
  public File directory;
  @Nonnull
  public String jUnit;
  @Nonnull
  public String srcDirName = "jack";
  @Nonnull
  public String libDirName = "lib";
  @Nonnull
  public String refDirName = "dx";
  @Nonnull
  public String linkDirName = "link";
  @Nonnull
  public String propertyFileName = "test.properties";
  @Nonnull
  public String jarjarRulesFileName = "jarjar-rules.txt";
  @Nonnull
  public List<String> proguardFilesNames = new ArrayList<String>();
  @Nonnull
  public List<File> referenceExtraSources = new ArrayList<File>();
  @Nonnull
  public List<File> candidateExtraSources = new ArrayList<File>();
  @Nonnull
  public List<FileChecker> checkers = new ArrayList<FileChecker>(0);

  public RuntimeTestInfo(@Nonnull File directory, @Nonnull String jUnit) {
    this.directory = directory;
    this.jUnit = jUnit;
  }

  @Nonnull
  public RuntimeTestInfo setSrcDirName(@Nonnull String srcDirName) {
    this.srcDirName = srcDirName;
    return this;
  }

  @Nonnull
  public RuntimeTestInfo setLibDirName(@Nonnull String libDirName) {
    this.libDirName = libDirName;
    return this;
  }

  @Nonnull
  public RuntimeTestInfo setRefDirName(@Nonnull String refDirName) {
    this.refDirName = refDirName;
    return this;
  }

  @Nonnull
  public RuntimeTestInfo setLinkDirName(@Nonnull String linkDirName) {
    this.linkDirName = linkDirName;
    return this;
  }

  @Nonnull
  public RuntimeTestInfo setPropertyFileName(@Nonnull String propertyFileName) {
    this.propertyFileName = propertyFileName;
    return this;
  }

  @Nonnull
  public RuntimeTestInfo setJarjarRulesFileName(String jarjarRulesFileName) {
    this.jarjarRulesFileName = jarjarRulesFileName;
    return this;
  }

  @Nonnull
  public RuntimeTestInfo addProguardFlagsFileName(@Nonnull String name) {
    proguardFilesNames.add(name);
    return this;
  }

  @Nonnull
  public RuntimeTestInfo addReferenceExtraSources(File file) {
    referenceExtraSources.add(file);
    return this;
  }

  @Nonnull
  public RuntimeTestInfo addCandidateExtraSources(File file) {
    candidateExtraSources.add(file);
    return this;
  }

  @Nonnull
  public RuntimeTestInfo addFileChecker(@Nonnull FileChecker checker) {
    checkers.add(checker);
    return this;
  }

}