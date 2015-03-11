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

package com.android.jack.test.comparator;

import com.android.jack.comparator.DexComparator;
import com.android.jack.comparator.DifferenceFoundException;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nonnull;

/**
 * This {@link Comparator} is used ton compare dex files.
 */
public class ComparatorDex extends ComparatorFile {

  private boolean withDebugInfo = false;
  private boolean strict = false;
  private boolean compareDebugInfoBinary = false;
  private boolean compareInstructionNumber = false;
  private float instructionNumberTolerance = 0f;

  public ComparatorDex(@Nonnull File candidate, @Nonnull File reference) {
    super(candidate, reference);
  }

  @Nonnull
  public ComparatorDex setWithDebugInfo(boolean withDebugInfo) {
    this.withDebugInfo = withDebugInfo;
    return this;
  }

  @Nonnull
  public ComparatorDex setStrict(boolean strict) {
    this.strict = strict;
    return this;
  }

  @Nonnull
  public ComparatorDex setCompareDebugInfoBinary(boolean compareDebugInfoBinary) {
    this.compareDebugInfoBinary = compareDebugInfoBinary;
    return this;
  }

  @Nonnull
  public ComparatorDex setCompareInstructionNumber(boolean compareInstructionNumber) {
    this.compareInstructionNumber = compareInstructionNumber;
    return this;
  }

  @Nonnull
  public ComparatorDex setInstructionNumberTolerance(float instructionNumberTolerance) {
    this.instructionNumberTolerance = instructionNumberTolerance;
    return this;
  }

  @Override
  public void compare() throws DifferenceFoundException, ComparatorException {
    try {
      new DexComparator(withDebugInfo, strict, compareDebugInfoBinary, compareInstructionNumber,
          instructionNumberTolerance).compare(reference, candidate);
    } catch (IOException e) {
      throw new ComparatorException(e);
    }
  }
}
