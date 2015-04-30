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

package com.android.jack.frontend;

import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JVisitor;

import java.util.Stack;

import javax.annotation.Nonnull;

/**
 * Parent setter.
 */
public class ParentSetter extends JVisitor {

  public ParentSetter() {
    super(false /* needLoading */);
  }

  @Nonnull
  private final Stack<JNode> nodes = new Stack<JNode>();

  @Override
  public boolean visit(@Nonnull JNode node) {
    if (!nodes.isEmpty()) {
      node.updateParents(nodes.peek());
    }
    nodes.push(node);
    return super.visit(node);
  }

  @Override
  public void endVisit(@Nonnull JNode node) {
    nodes.pop();
    super.endVisit(node);
  }
}
