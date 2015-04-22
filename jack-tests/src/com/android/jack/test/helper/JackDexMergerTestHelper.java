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

import com.android.jack.Options;
import com.android.jack.backend.dex.rop.CodeItemBuilder;
import com.android.jack.test.comparator.ComparatorComposite;
import com.android.jack.test.comparator.ComparatorDex;
import com.android.jack.test.comparator.ComparatorDexAnnotations;
import com.android.jack.test.comparator.ComparatorDiff;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.JackBasedToolchain;
import com.android.sched.scheduler.ScheduleInstance;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nonnull;

/**
 * This class defines a helper for dex merger tests.
 */
public class JackDexMergerTestHelper extends SourceToDexComparisonTestHelper {

  public JackDexMergerTestHelper(@Nonnull File fileOrSourceList) throws Exception {
    super(fileOrSourceList);
  }

  @Override
  @Nonnull
  protected JackBasedToolchain getCandidateToolchain() {
    // Mono dex
    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    toolchain.addProperty(Options.EMIT_LINE_NUMBER_DEBUG_INFO.getName(),
        Boolean.toString(withDebugInfos));
    toolchain.addProperty(ScheduleInstance.DEFAULT_RUNNER.getName(), "single-threaded");
    toolchain.addProperty(CodeItemBuilder.FORCE_JUMBO.getName(), "true");

    return toolchain;
  }

  @Override
  @Nonnull
  protected JackBasedToolchain getReferenceToolchain() {
    // One dex per type
    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    File internalJackOutputLib;
    try {
      internalJackOutputLib = AbstractTestTools.createTempDir();
      toolchain.addProperty(Options.EMIT_LINE_NUMBER_DEBUG_INFO.getName(),
          Boolean.toString(withDebugInfos));
      toolchain.addProperty(ScheduleInstance.DEFAULT_RUNNER.getName(), "single-threaded");
      toolchain.addProperty(Options.LIBRARY_OUTPUT_DIR.getName(),
        internalJackOutputLib.getAbsolutePath());
    } catch (IOException e) {
      throw new AssertionError(e);
    }
    return toolchain;
  }

  public void compare() throws Exception {
    ComparatorDex comparatorDex = new ComparatorDex(refDex, candidateDex);
    comparatorDex.setWithDebugInfo(false);
    comparatorDex.setStrict(true);
    comparatorDex.setCompareDebugInfoBinary(false);
    comparatorDex.setCompareInstructionNumber(true);
    comparatorDex.setInstructionNumberTolerance(0);
    ComparatorDexAnnotations comparatorAnnotations =
        new ComparatorDexAnnotations(refDex, candidateDex);
    ComparatorDiff comparatorDiff = new ComparatorDiff(refDex, candidateDex);
    runTest(new ComparatorComposite(comparatorDex, comparatorAnnotations, comparatorDiff));
  }
}
