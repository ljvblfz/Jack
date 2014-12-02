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


import com.android.jack.test.toolchain.AndroidToolchain;

import java.io.File;

import javax.annotation.Nonnull;

/**
 * This {@link SourceToDexComparisonTestHelper} is used to compare dex files structures.
 */
public class CheckDexStructureTestHelper extends SourceToDexComparisonTestHelper {

  public CheckDexStructureTestHelper(@Nonnull File... filesOrSourceList) throws Exception {
    super(filesOrSourceList);
  }

  @Override
  @Nonnull
  public CheckDexStructureTestHelper setCandidateTestTools(
      @Nonnull AndroidToolchain candidateTestTools) {
    return (CheckDexStructureTestHelper) super.setCandidateTestTools(candidateTestTools);
  }

  @Override
  @Nonnull
  public CheckDexStructureTestHelper setReferenceTestTools(
      @Nonnull AndroidToolchain referenceTestTools) {
    return (CheckDexStructureTestHelper) super.setReferenceTestTools(referenceTestTools);
  }

  @Override
  @Nonnull
  public CheckDexStructureTestHelper setCandidateClasspath(@Nonnull File[] classpath) {
    return (CheckDexStructureTestHelper) super.setCandidateClasspath(classpath);
  }

  @Override
  @Nonnull
  public CheckDexStructureTestHelper setReferenceClasspath(@Nonnull File[] classpath) {
    return (CheckDexStructureTestHelper) super.setReferenceClasspath(classpath);
  }

  @Override
  @Nonnull
  public CheckDexStructureTestHelper setWithDebugInfo(boolean withDebugInfo) {
    return (CheckDexStructureTestHelper) super.setWithDebugInfo(withDebugInfo);
  }

  @Override
  @Nonnull
  public CheckDexStructureTestHelper setJarjarRulesFile(@Nonnull File jarjarRulesFile) {
    return (CheckDexStructureTestHelper) super.setJarjarRulesFile(jarjarRulesFile);
  }

  @Override
  @Nonnull
  public CheckDexStructureTestHelper setProguardFlags(@Nonnull File... proguardFlags) {
    return (CheckDexStructureTestHelper) super.setProguardFlags(proguardFlags);
  }

  @Override
  @Nonnull
  protected void executeCandidateToolchain() throws Exception {
    if (withDebugInfos) {
      getCandidateToolchain().disableDxOptimizations();
    } else {
      getCandidateToolchain().enableDxOptimizations();
    }
    super.executeCandidateToolchain();
  }

  @Override
  @Nonnull
  protected void executeReferenceToolchain() throws Exception {
    if (withDebugInfos) {
      getReferenceToolchain().disableDxOptimizations();
    } else {
      getReferenceToolchain().enableDxOptimizations();
    }
    super.executeReferenceToolchain();
  }

  public void compare() throws Exception {
    runTest(createDexFileComparator());
  }

}
