/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.jack.ir.ast.cfg;

import com.google.common.collect.Maps;

import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JSsaVariableRef;
import com.android.jack.ir.ast.JVariable;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.sched.item.Component;
import com.android.sched.item.Description;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * Represents a Phi statement in the SSA form.
 */
@Description("Phi elements for SSA.")
public class JPhiBlockElement extends JBasicBlockElement {
  @Nonnull
  private final JVariable var;

  @Nonnull
  private JSsaVariableRef lhs;

  @Nonnull
  private final Map<JBasicBlock, JSsaVariableRef> rhs;

  /**
   * Creates a new Phi statement.
   *
   * @param var The original variable.
   * @param numPred Number of predecesssors of this Phi statement.
   */
  public JPhiBlockElement(JVariable var, List<JBasicBlock> preds, SourceInfo info) {
    // Phi instructions are not real instructions and should not throw any exception.
    super(info, ExceptionHandlingContext.EMPTY);
    rhs = Maps.newHashMap();
    for (JBasicBlock pred : preds) {
      // We are going to insert a place holder first. The SSA renamer will replace the var ref
      // with a proper version.
      rhs.put(pred, new JSsaVariableRef(info, var, 0, this, true));
    }
    lhs = new JSsaVariableRef(info, var, 0, this, false);
    this.var = var;
  }


  @Override
  public void traverse(@Nonnull JVisitor visitor) {
    if (visitor.visit(this)) {
      visitor.accept(lhs);
      for (JSsaVariableRef rhsVar : rhs.values()) {
        if (rhsVar == null) {
          throw new RuntimeException("this can't be that good for business");
        }
        visitor.accept(rhsVar);
      }
    }
    visitor.endVisit(this);
  }

  @Override
  public void traverse(@Nonnull ScheduleInstance<? super Component> schedule) throws Exception {
    schedule.process(this);
    lhs.traverse(schedule);
    for (JSsaVariableRef var : rhs.values()) {
      var.traverse(schedule);
    }
  }

  @Override
  public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visitor.visit(this, transformRequest);
  }

  @Override
  protected void replaceImpl(@Nonnull JNode existingNode, @Nonnull JNode newNode)
      throws UnsupportedOperationException {
    if (lhs == existingNode) {
      lhs = (JSsaVariableRef) newNode;
      return;
    }

    for (JBasicBlock pred : rhs.keySet()) {
      if (rhs.get(pred) == existingNode) {
        rhs.put(pred, (JSsaVariableRef) newNode);
        return;
      }
    }
    super.replaceImpl(existingNode, newNode);
  }

  @Nonnull
  public JVariable getTarget() {
    return var;
  }

  @Nonnull
  public JSsaVariableRef getLhs() {
    return lhs;
  }

  @Nonnull
  public Iterable<JSsaVariableRef> getRhs() {
    return rhs.values();
  }

  @Nonnull
  public JSsaVariableRef getRhs(@Nonnull JBasicBlock pred) {
    JSsaVariableRef ref = rhs.get(pred);
    assert ref != null;
    return ref;
  }

  @Override
  public boolean isTerminal() {
    return false;
  }
}
