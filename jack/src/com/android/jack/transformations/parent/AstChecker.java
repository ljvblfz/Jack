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
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JVisitorWithAnnotation;
import com.android.jack.transformations.SanityChecks;
import com.android.sched.item.Description;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;

import java.util.Stack;

import javax.annotation.Nonnull;

/**
 * Check that AST of {@link JNode}s is correct.
 */
@Description("Check that AST is correct.")
@Support(SanityChecks.class)
public class AstChecker implements RunnableSchedulable<JSession> {

  static class AstCheckerVisitor extends JVisitorWithAnnotation {
    @Nonnull
    private final Stack<JNode> nodes = new Stack<JNode>();

    AstCheckerVisitor() {
      super(false /* needLoading */);
    }

    @Override
    public boolean visit(@Nonnull JNode node) {
      node.checkValidity();

      if (node instanceof JSession) {
        if (node.getParent() != null) {
          throw new AssertionError("Parent of JSession must be null.");
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
  public void run(@Nonnull JSession session) throws Exception {
    AstCheckerVisitor checker = new AstCheckerVisitor();
    checker.accept(session);
  }
}