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
 * A {@link TransformationStep} allowing to remove one {@link JNode}.
 */
public class Remove implements TransformationStep, TransformStep {

  @Nonnull
  private final JNode node;

  /**
   * Constructor specifying the node to remove from its parent.
   *
   * @param node the node to remove
   */
  public Remove(@Nonnull JNode node) {
    assert node != null;
    this.node = node;
  }

  @Override
  public void apply() throws UnsupportedOperationException {
    if (node.getParent() == null) {
      throw new UnsupportedOperationException();
    }
    node.getParent().remove(node);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("Remove ");
    sb.append(node.toSource());
    sb.append(" in ");
    sb.append(node.getParent().toSource());
    return sb.toString();
  }

}
