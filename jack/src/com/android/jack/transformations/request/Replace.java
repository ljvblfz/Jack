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
 * A {@link TransformationStep} allowing to replace one existing {@link JNode} with a new
 * {@link JNode}.
 */
public class Replace implements TransformationStep, TransformStep {

  @Nonnull
  private final JNode existingNode;
  @Nonnull
  private final JNode newNode;

  /**
   * Constructor specifying the existing node to replace with the new node.
   *
   * @param existingNode the node to replace
   * @param newNode the new node that replaces the existing node
   */
  public Replace(@Nonnull JNode existingNode, @Nonnull JNode newNode) {
    assert existingNode != null;
    assert newNode != null;

    this.existingNode = existingNode;
    this.newNode = newNode;
  }

  @Override
  public void apply() throws UnsupportedOperationException {
    if (existingNode.getParent() == null) {
      throw new UnsupportedOperationException();
    }
    existingNode.getParent().replace(existingNode, newNode);
  }

  @Override
  @Nonnull
  public String toString() {
    StringBuilder sb = new StringBuilder("Replace ");
    sb.append(existingNode.toSource());
    sb.append(" with ");
    sb.append(newNode.toSource());
    sb.append(" in ");
    sb.append(existingNode.getParent().toSource());
    return sb.toString();
  }
}
