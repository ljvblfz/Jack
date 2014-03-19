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

package com.android.jack.transformations.parent;

import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JProgram;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.transformations.SanityChecks;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;

import java.util.Stack;

import javax.annotation.Nonnull;

/**
 * Check that parent of {@link JNode} are correctly set.
 */
@Description("Check that parent of JNode are correctly set.")
@Name("ParentSetterChecker")
@Support(SanityChecks.class)
public class ParentSetterChecker implements RunnableSchedulable<JProgram> {

  private static class ParentSetterCheckerVisitor extends JVisitor {
    @Nonnull
    private final Stack<JNode> nodes = new Stack<JNode>();

    private ParentSetterCheckerVisitor() {
      super(false /* needLoading */);
    }

    @Override
    public boolean visit(@Nonnull JNode node) {
      if (node instanceof JProgram) {
        if (node.getParent() != null) {
          throw new AssertionError("Parent of JProgram must be null.");
        }
      } else {
        if (node.getParent() != nodes.peek()) {
          throw new AssertionError("Node with wrong parent.");
        }
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

  @Override
  public void run(@Nonnull JProgram program) throws Exception {
    ParentSetterCheckerVisitor checker = new ParentSetterCheckerVisitor();
    checker.accept(program);
  }
}