/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.jack.shrob;

import com.android.jack.ProguardFlags;
import com.android.jack.test.comparator.ComparatorDex;
import com.android.jack.test.helper.SourceToDexComparisonTestHelper;
import com.android.jack.test.toolchain.AbstractTestTools;

import java.io.File;

import javax.annotation.Nonnull;

public class ObfuscationWithDebugInfoTests extends AbstractTest {

  @Override
  protected void runTest(
      @Nonnull String testNumber,
      @Nonnull String flagNumber,
      @Nonnull String mappingNumber)
      throws Exception {

    File testFolder = AbstractTestTools.getTestRootDir("com.android.jack.shrob.test" + testNumber);

    SourceToDexComparisonTestHelper env =
        new SourceToDexComparisonTestHelper(new File(testFolder, "jack"));
    env.setWithDebugInfo(true);
    env.setProguardFlags(
        generateApplyMapping(
        new File(testFolder, "proguard.flags" + flagNumber + ".mapping" + mappingNumber)),
        new ProguardFlags(AbstractTestTools.getTestRootDir("com.android.jack.shrob"),
            "keepDebugInfo.flags"), new ProguardFlags(testFolder, "proguard.flags" + flagNumber));

    env.runTest(new ComparatorDex(env.getReferenceDex(), env.getCandidateDex()));
  }
}
