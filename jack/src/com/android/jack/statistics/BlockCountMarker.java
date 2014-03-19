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

package com.android.jack.statistics;

import com.android.jack.ir.ast.JProgram;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Statistics about block and extra created block.
 */
@Description("Statistics about block and extra created block.")
@ValidOn(JProgram.class)
public class BlockCountMarker implements Marker {

  @Nonnegative
  private int existingBlockCount = 0;
  @Nonnegative
  private int extraIfThenBlockCount = 0;
  @Nonnegative
  private int extraIfElseBlockCount = 0;
  @Nonnegative
  private int extraLabeledStatementBlockCount = 0;
  @Nonnegative
  private int extraForBodyBlockCount = 0;
  @Nonnegative
  private int extraWhileBlockCount = 0;
  @Nonnegative
  private int extraImplicitForBlockCount = 0;

   @Override
   @Nonnull
   public Marker cloneIfNeeded() {
     return this;
   }

  /**
   * @return the existingBlockCount
   */
  @Nonnegative
  public int getExistingBlockCount() {
    return existingBlockCount;
  }

  /**
   * @return the addedBlockCount
   */
  @Nonnegative
  public int getExtraBlockCount() {
    return extraIfThenBlockCount + extraIfElseBlockCount + extraLabeledStatementBlockCount
        + extraForBodyBlockCount + extraWhileBlockCount + extraImplicitForBlockCount;
  }

  /**
   * @param existingBlockCount the existingBlockCount to set
   */
  public void addExistingBlockCount(@Nonnegative int existingBlockCount) {
    this.existingBlockCount += existingBlockCount;
  }

  /**
   * @return the extraIfThenBlockCount
   */
  @Nonnegative
  public int getExtraIfThenBlockCount() {
    return extraIfThenBlockCount;
  }

  /**
   * @return the extraIfElseBlockCount
   */
  @Nonnegative
  public int getExtraIfElseBlockCount() {
    return extraIfElseBlockCount;
  }

  /**
   * @return the extraLabeledStatementBlockCount
   */
  @Nonnegative
  public int getExtraLabeledStatementBlockCount() {
    return extraLabeledStatementBlockCount;
  }

  /**
   * @return the extraForBodyBlockCount
   */
  @Nonnegative
  public int getExtraForBodyBlockCount() {
    return extraForBodyBlockCount;
  }

  /**
   * @return the extraWhileBlockCount
   */
  @Nonnegative
  public int getExtraWhileBlockCount() {
    return extraWhileBlockCount;
  }

  /**
   * @param extraIfThenBlockCount the extraIfThenBlockCount to set
   */
  public void addExtraIfThenBlockCount(@Nonnegative int extraIfThenBlockCount) {
    this.extraIfThenBlockCount += extraIfThenBlockCount;
  }

  /**
   * @param extraIfElseBlockCount the extraIfElseBlockCount to set
   */
  public void addExtraIfElseBlockCount(@Nonnegative int extraIfElseBlockCount) {
    this.extraIfElseBlockCount += extraIfElseBlockCount;
  }

  /**
   * @param extraLabeledStatementBlockCount the extraLabeledStatementBlockCount to set
   */
  public void addExtraLabeledStatementBlockCount(@Nonnegative int extraLabeledStatementBlockCount) {
    this.extraLabeledStatementBlockCount += extraLabeledStatementBlockCount;
  }

  /**
   * @param extraForBodyBlockCount the extraForBodyBlockCount to set
   */
  public void addExtraForBodyBlockCount(@Nonnegative int extraForBodyBlockCount) {
    this.extraForBodyBlockCount += extraForBodyBlockCount;
  }

  /**
   * @param extraWhileBlockCount the extraWhileBlockCount to set
   */
  public void addExtraWhileBlockCount(@Nonnegative int extraWhileBlockCount) {
    this.extraWhileBlockCount += extraWhileBlockCount;
  }

  /**
   * @return the extraImplicitForBlockCount
   */
  @Nonnegative
  public int getExtraImplicitForBlockCount() {
    return extraImplicitForBlockCount;
  }

  /**
   * @param extraImplicitForBlockCount the extraImplicitForBlockCount to set
   */
  public void addExtraImplicitForBlockCount(@Nonnegative int extraImplicitForBlockCount) {
    this.extraImplicitForBlockCount += extraImplicitForBlockCount;
  }
}
