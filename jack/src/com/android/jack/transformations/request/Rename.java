/*
 * Copyright (C) 2013 The Android Open Source Project
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

import com.android.jack.ir.ast.CanBeRenamed;
import com.android.jack.ir.ast.JNode;
import com.android.sched.transform.TransformStep;

import javax.annotation.Nonnull;

/**
 * A {@link TransformationStep} allowing to rename one {@link JNode}.
 */
public class Rename implements TransformationStep, TransformStep {

  @Nonnull
  private final CanBeRenamed node;

  @Nonnull
  private final String newName;

  /**
   * Constructor specifying the {@code node} to rename with {@code newName}.
   *
   * @param node the node to rename
   * @param newName the new name of the node
   */
  public Rename(@Nonnull CanBeRenamed node, @Nonnull String newName) {
    this.node = node;
    this.newName = newName;
  }

  @Override
  public void apply() throws UnsupportedOperationException {
    node.setName(newName);
  }

  @Override
  @Nonnull
  public String toString() {
    StringBuilder sb = new StringBuilder("Rename ");
    sb.append(node.toString());
    sb.append(" to ");
    sb.append(newName);
    return sb.toString();
  }
}
