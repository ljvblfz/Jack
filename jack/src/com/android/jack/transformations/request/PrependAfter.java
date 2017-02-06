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

package com.android.jack.transformations.request;

import com.android.jack.ir.ast.JNode;
import com.android.sched.transform.TransformStep;

import javax.annotation.Nonnull;

/**
 * A {@link TransformationStep} allowing to insert a {@link JNode} just after an existing
 * {@link JNode} in the existing {@link JNode} parent.
 *
 * @see AppendBefore
 */
public class PrependAfter implements TransformationStep, TransformStep {
  @Nonnull
  private final JNode existingNode;

  @Nonnull
  private final JNode newNode;

  /**
   * Constructor specifying the new node to insert after the existing node.
   *
   * @param existingNode the existing node that will precede the new node
   * @param newNode the new node to be inserted after the existing node
   */
  public PrependAfter(@Nonnull JNode existingNode, @Nonnull JNode newNode) {
    assert existingNode != null;
    assert newNode != null;

    this.existingNode = existingNode;
    this.newNode = newNode;
  }

  @Override
  public void apply() throws UnsupportedOperationException {
    final JNode existingNodeParent = existingNode.getParent();
    if (existingNodeParent == null) {
      throw new UnsupportedOperationException();
    }
    existingNodeParent.insertAfter(existingNode, newNode);
  }

  @Override
  @Nonnull
  public String toString() {
    final JNode existingNodeParent = existingNode.getParent();
    assert existingNodeParent != null;
    StringBuilder sb = new StringBuilder("Prepend ");
    sb.append(newNode.toSource());
    sb.append(" after ");
    sb.append(existingNode.toSource());
    sb.append(" in ");
    sb.append(existingNodeParent.toSource());
    return sb.toString();
  }

}
