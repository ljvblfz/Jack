/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.jack.transformations.boostlockregionpriority;

import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.junit.Runtime;
import com.android.jack.test.runtime.RuntimeTest;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.JillBasedToolchain;

import org.junit.Test;

public class BoostLockedRegionPriorityTests extends RuntimeTest {

  private RuntimeTestInfo TEST001 = new RuntimeTestInfo(
      AbstractTestTools
          .getTestRootDir("com.android.jack.transformations.boostlockregionpriority.test001"),
      "com.android.jack.transformations.boostlockregionpriority.test001.dx.Tests");

  @Test
  @Runtime
  public void test001() throws Exception {
    new RuntimeTestHelper(TEST001).addIgnoredCandidateToolchain(JillBasedToolchain.class)
        .addProperty("jack.transformations.boost-locked-region-priority", "true")
        .addProperty("jack.transformations.boost-locked-region-priority.classname",
            "com.android.jack.transformations.boostlockregionpriority.test001.jack.LockedRegion,"
                + "com.android.jack.transformations.boostlockregionpriority.test001.jack.LockedRegion2,")
        .addProperty("jack.transformations.boost-locked-region-priority.request",
            "com.android.jack.transformations.boostlockregionpriority.test001.jack."
                + "Request#request,"
                + "com.android.jack.transformations.boostlockregionpriority.test001.jack."
                + "Request2#request")
        .addProperty("jack.transformations.boost-locked-region-priority.reset",
            "com.android.jack.transformations.boostlockregionpriority.test001.jack."
                + "Reset#reset,"
                + "com.android.jack.transformations.boostlockregionpriority.test001.jack."
                + "Reset2#reset,")
        .compileAndRunTest();
  }

  @Override
  protected void fillRtTestInfos() {}
}
