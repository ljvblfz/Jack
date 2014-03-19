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

package com.android.jack.cfg;

import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JVisitor;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;

import javax.annotation.Nonnull;

/**
 * A {@code Schedulable} that removes markers related to the {@code ControlFlowGraph}.
 */
@Description("Removes markers related to the ControlFlowGraph.")
@Name("CfgMarkerRemover")
@Constraint(need = {ControlFlowGraph.class, BasicBlockMarker.class})
@Transform(remove = {ControlFlowGraph.class, BasicBlockMarker.class})
public class CfgMarkerRemover implements RunnableSchedulable<JMethod> {

  private static class Visitor extends JVisitor {
    @Override
    public boolean visit(@Nonnull JStatement stmt) {
      stmt.removeMarker(BasicBlockMarker.class);
      return super.visit(stmt);
    }
  }

  @Override
  public void run(@Nonnull JMethod method) throws Exception {
    method.removeMarker(ControlFlowGraph.class);
    Visitor v = new Visitor();
    v.accept(method);
  }
}
