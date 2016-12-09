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

import com.android.jack.Jack;
import com.android.jack.ir.ast.HasEnclosingPackage;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JPackage;
import com.android.jack.ir.ast.JSession;
import com.android.sched.transform.TransformStep;

import javax.annotation.Nonnull;

/**
 * A {@link TransformationStep} allowing to change the enclosing package of a {@link JPackage}.
 */
public class ChangeEnclosingPackage implements TransformationStep, TransformStep {

  @Nonnull
  private final JPackage newEnclosingPackage;

  @Nonnull
  private final HasEnclosingPackage existingNode;

  @Nonnull
  private final JSession session = Jack.getSession();

  /**
   * Constructor specifying the package {@code newEnclosingPackage} that will become
   * the enclosing package of {@code existingNode}.
   *
   * @param existingNode the existing {@link JNode} to update
   * @param newEnclosingPackage the new enclosing package of the existing node
   */
  public ChangeEnclosingPackage(
      @Nonnull HasEnclosingPackage existingNode, @Nonnull JPackage newEnclosingPackage) {
    assert existingNode != session.getTopLevelPackage()
        : "The default package can't change its enclosing package";
    this.newEnclosingPackage = newEnclosingPackage;
    this.existingNode = existingNode;
  }

  /**
   * Changes the enclosing package of the existing node to the new package. The existing node
   * is removed from its previous enclosing package and added to the new one. The existing node
   * becomes the parent of the new enclosing package.
   *
   * @see HasEnclosingPackage#setEnclosingPackage(JPackage)
   */
  @Override
  public void apply() throws UnsupportedOperationException {
    JPackage enclosingPackage = existingNode.getEnclosingPackage();
    if (enclosingPackage != null) {
      enclosingPackage.remove((JNode) existingNode);
    }
    newEnclosingPackage.add(existingNode);
    existingNode.setEnclosingPackage(newEnclosingPackage);
    ((JNode) existingNode).updateParents(newEnclosingPackage);
  }

  @Override
  @Nonnull
  public String toString() {
    StringBuilder sb = new StringBuilder("Change enclosing package of ");
    sb.append(existingNode.toString());
    sb.append(" to ");
    sb.append(Jack.getUserFriendlyFormatter().getName(newEnclosingPackage));
    return sb.toString();
  }
}
