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

package com.android.jack.analysis.defsuses;

import com.android.jack.Jack;
import com.android.jack.Options;
import com.android.jack.analysis.UseDefsMarker;
import com.android.jack.cfg.BasicBlock;
import com.android.jack.cfg.ControlFlowGraph;
import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JBlock;
import com.android.jack.ir.ast.JCatchBlock;
import com.android.jack.ir.ast.JIfStatement;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JVariableRef;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.util.filter.Filter;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.util.config.ThreadConfig;

import javax.annotation.Nonnull;

/**
 * Check use/defs result analysis.
 */
@Description("Check use/defs result analysis.")
@Constraint(need = {UseDefsMarker.class})
public class UseDefsChecker implements RunnableSchedulable<JMethod> {

  @Nonnull
  private final Filter<JMethod> filter = ThreadConfig.get(Options.METHOD_FILTER);

  private static class Visitor extends JVisitor {

    @Nonnull
    private final JMethod jmethod;

    public Visitor(@Nonnull JMethod jmethod) {
      this.jmethod = jmethod;
    }

    @Override
    public boolean visit(@Nonnull JVariableRef varRef) {
      JNode parent = varRef.getParent();
      if (!(parent instanceof JAsgOperation)
          || ((JAsgOperation) parent).getLhs() != varRef) {
        if (varRef.getMarker(UseDefsMarker.class) == null) {
          throw new AssertionError("Failed to verify use/defs chains of "
              + Jack.getUserFriendlyFormatter().getName(jmethod.getEnclosingType()) + "."
              + Jack.getUserFriendlyFormatter().getName(jmethod));
        }
      }
      return super.visit(varRef);
    }

    @Override
    public boolean visit(@Nonnull JIfStatement jIf) {
      accept(jIf.getIfExpr());
      return false;
    }

    @Override
    public boolean visit(@Nonnull JBlock jBlock) {
      return false;
    }

    @Override
    public boolean visit(@Nonnull JCatchBlock jCatchBlock) {
      return false;
    }
  }

  @Override
  public void run(@Nonnull JMethod method) throws Exception {
    if (method.getEnclosingType().isExternal() || method.isNative() || method.isAbstract()
        || !filter.accept(this.getClass(), method)) {
      return;
    }

    ControlFlowGraph cfg = method.getMarker(ControlFlowGraph.class);
    assert cfg != null;

    Visitor visitor = new Visitor(method);

    for (BasicBlock bb : cfg.getNodes()) {
      visitor.accept(bb.getStatements());
    }
  }
}