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

import javax.annotation.Nonnull;

/**
 * This class implements a pattern of tests where two compilers are used on the same
 * sources and the result is then compared with a variety of {@link Comparator}s.
 */
public abstract class GenericComparisonTestHelper {

  @Nonnull
  protected abstract void executeCandidateToolchain() throws Exception;
  @Nonnull
  protected abstract void executeReferenceToolchain() throws Exception;

  public final void runTest(@Nonnull Comparator... comparators) throws Exception {

    assert comparators.length > 0 : "You must provide at least one comparator";

    executeCandidateToolchain();
    executeReferenceToolchain();

    for (Comparator comparator : comparators) {
      comparator.compare();
    }
  }
}
