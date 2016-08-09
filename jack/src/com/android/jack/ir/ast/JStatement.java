/*
 * Copyright 2007 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.android.jack.ir.ast;

import com.android.jack.ir.JNodeInternalError;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.sched.item.Description;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Java program statement.
 */
@Description("Java program statement")
public abstract class JStatement extends JNode {

  /**
   * Each statements can contains a list of catch blocks representing all potential targets on
   * exceptions. The list is ordered by decreasing priority.
   */
  @Nonnull
  private final List<JCatchBlock> catchBlocks = new ArrayList<JCatchBlock>();

  public JStatement(@Nonnull SourceInfo info) {
    super(info);
  }

  @Override
  protected boolean isResultOfExpressionUsed(JExpression expr) {
    return true;
  }

  /**
   * Append a catch block for the statement.
   * @param catchBlock Catch block to append.
   */
  public void appendCatchBlock(@Nonnull JCatchBlock catchBlock) {
    assert !catchBlocks.contains(catchBlock);
    catchBlocks.add(catchBlock);
  }

  /**
   * Set catch blocks for the statement.
   * @param catchBlocks Catch blocks to set.
   */
  public void setCatchBlocks(@Nonnull List<JCatchBlock> catchBlocks) {
    assert this.catchBlocks.isEmpty();
    this.catchBlocks.addAll(catchBlocks);
  }

  /**
   * Get all catch blocks related to this statement.
   * @return Catch list.
   */
  @Nonnull
  public List<JCatchBlock> getJCatchBlocks() {
    return catchBlocks;
  }

  @Override
  public void checkValidity() {
    if (!(parent instanceof JStatement || parent instanceof JAbstractMethodBody)) {
      throw new JNodeInternalError(this, "Invalid parent");
    }
  }

  public boolean isUnconditionalBranch() {
    return false;
  }
}
