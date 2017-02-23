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

package com.android.jack.test.helper;

import com.android.jack.test.comparator.Comparator;
import com.android.jack.test.comparator.ComparatorComposite;
import com.android.jack.test.comparator.ComparatorDex;
import com.android.jack.test.comparator.ComparatorDexAnnotations;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.AndroidToolchain;
import com.android.jack.test.toolchain.Toolchain.SourceLevel;

import java.io.File;
import java.util.Collections;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * This class is a {@link GenericComparisonTestHelper} where the two compilers perform
 * a source-to-dex compilation.
 */
public class SourceToDexComparisonTestHelper extends GenericComparisonTestHelper {

  @Nonnull
  private File candidateDexDir;
  @Nonnull
  protected File candidateDex;
  @Nonnull
  private File refDexDir;
  @Nonnull
  protected File refDex;

  @Nonnull
  private File[] candidateClasspath;
  @Nonnull
  private File[] referenceClasspath;

  @Nonnull
  private File[] filesOrSourceList;

  @CheckForNull
  private File jarjarRulesFile = null;
  @Nonnull
  private File[] proguardFlagFiles = new File[0];

  @Nonnull
  private SourceLevel sourceLevel = SourceLevel.JAVA_6;

  @Nonnull
  private AndroidToolchain candidateTestTools;
  @Nonnull
  private AndroidToolchain referenceTestTools;

  protected boolean withDebugInfos = false;

  public SourceToDexComparisonTestHelper(@Nonnull File... filesOrSourceList) throws Exception {

    this.filesOrSourceList = filesOrSourceList;

    candidateTestTools = getCandidateToolchain();
    referenceTestTools = getReferenceToolchain();

    candidateClasspath = candidateTestTools.getDefaultBootClasspath();
    referenceClasspath = referenceTestTools.getDefaultBootClasspath();

    candidateDexDir = AbstractTestTools.createTempDir();
    refDexDir = AbstractTestTools.createTempDir();

    candidateDex = new File(candidateDexDir, candidateTestTools.getBinaryFileName());
    refDex = new File(refDexDir, referenceTestTools.getBinaryFileName());
  }

  @Nonnull
  protected AndroidToolchain getCandidateToolchain() {
    return AbstractTestTools.getCandidateToolchain(AndroidToolchain.class);
  }

  @Nonnull
  protected AndroidToolchain getReferenceToolchain() {
    return AbstractTestTools.getReferenceToolchain(AndroidToolchain.class);
  }

  @Nonnull
  public SourceToDexComparisonTestHelper setCandidateTestTools(
      @Nonnull AndroidToolchain candidateTestTools) {
    this.candidateTestTools = candidateTestTools;
    return this;
  }

  @Nonnull
  public SourceToDexComparisonTestHelper setReferenceTestTools(
      @Nonnull AndroidToolchain referenceTestTools) {
    this.referenceTestTools = referenceTestTools;
    return this;
  }

  @Nonnull
  public SourceToDexComparisonTestHelper setCandidateClasspath(@Nonnull File... classpath) {
    candidateClasspath = classpath;
    return this;
  }

  @Nonnull
  public SourceToDexComparisonTestHelper setReferenceClasspath(@Nonnull File... classpath) {
    referenceClasspath = classpath;
    return this;
  }

  @Nonnull
  public SourceToDexComparisonTestHelper setWithDebugInfo(boolean withDebugInfo) {
    this.withDebugInfos = withDebugInfo;
    return this;
  }

  public void setSourceLevel(SourceLevel sourceLevel) {
    this.sourceLevel = sourceLevel;
  }

  public File getCandidateDex() {
    return candidateDex;
  }

  public File getCandidateDexDir() {
    return candidateDexDir;
  }

  public File getReferenceDex() {
    return refDex;
  }

  public File getReferenceDexDir() {
    return refDexDir;
  }

  @Nonnull
  public ComparatorDex createDexFileComparator() {
    ComparatorDex comparator = new ComparatorDex(refDex, candidateDex);
    comparator.setWithDebugInfo(withDebugInfos);
    comparator.setStrict(false);
    comparator.setCompareDebugInfoBinary(false);
    comparator.setCompareInstructionNumber(false);
    comparator.setInstructionNumberTolerance(0f);
    return comparator;
  }

  @Nonnull
  public Comparator createDexFileComparatorWithAnnotations() {
    return new ComparatorComposite(createDexFileComparator(),
        new ComparatorDexAnnotations(refDex, candidateDex));
  }

  @Nonnull
  public SourceToDexComparisonTestHelper setJarjarRulesFile(@Nonnull File jarjarRulesFile) {
    this.jarjarRulesFile = jarjarRulesFile;
    return this;
  }

  @Nonnull
  public SourceToDexComparisonTestHelper setProguardFlags(@Nonnull File... proguardFlags) {
    this.proguardFlagFiles = proguardFlags;
    return this;
  }

  @Override
  @Nonnull
  protected void executeCandidateToolchain() throws Exception {
    if (jarjarRulesFile != null) {
      candidateTestTools.setJarjarRules(Collections.singletonList(jarjarRulesFile));
    }
    candidateTestTools.setWithDebugInfos(withDebugInfos);
    candidateTestTools.setSourceLevel(sourceLevel);
    candidateTestTools.addProguardFlags(proguardFlagFiles)
    .addToClasspath(candidateClasspath)
    .srcToExe(candidateDexDir,
        /* zipFile = */ false, filesOrSourceList);
  }

  @Override
  @Nonnull
  protected void executeReferenceToolchain() throws Exception {
    if (jarjarRulesFile != null) {
      referenceTestTools.setJarjarRules(Collections.singletonList(jarjarRulesFile));
    }
    referenceTestTools.setWithDebugInfos(withDebugInfos);
    referenceTestTools.setSourceLevel(sourceLevel);
    referenceTestTools.addProguardFlags(proguardFlagFiles)
    .addToClasspath(referenceClasspath)
    .srcToExe(refDexDir, /* zipFile = */ false, filesOrSourceList);
  }
}
