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

package com.android.jack.reporting;

import com.android.jack.VerbosityLevel;
import com.android.jack.reporting.Reportable.ProblemLevel;

import junit.framework.Assert;

import org.junit.Test;

public class ProblemLevelTest {

  /**
   * {@link ProblemLevel} makes assumptions on the order of the ordinal values of
   * {@link VerbosityLevel}. Let's check that they are true here.
   */
  @Test
  public void testVerboseLevelOrdinalOrder() {
    Assert.assertTrue(VerbosityLevel.ERROR.ordinal() < VerbosityLevel.WARNING.ordinal());
    Assert.assertTrue(VerbosityLevel.WARNING.ordinal() < VerbosityLevel.INFO.ordinal());
    Assert.assertTrue(VerbosityLevel.INFO.ordinal() < VerbosityLevel.DEBUG.ordinal());
    Assert.assertTrue(VerbosityLevel.DEBUG.ordinal() < VerbosityLevel.TRACE.ordinal());
  }

  @Test
  public void testProblemLevelIsVisibleWith() {
    Assert.assertTrue(ProblemLevel.ERROR.isVisibleWith(VerbosityLevel.INFO));
    Assert.assertTrue(ProblemLevel.INFO.isVisibleWith(VerbosityLevel.INFO));
    Assert.assertFalse(ProblemLevel.INFO.isVisibleWith(VerbosityLevel.ERROR));
  }
}
