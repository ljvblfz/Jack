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

import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JSsaVariableDefRef;
import com.android.jack.ir.ast.JSsaVariableDefRefPlaceHolder;
import com.android.jack.ir.ast.JSsaVariableRef;
import com.android.jack.ir.ast.JSsaVariableUseRef;
import com.android.jack.ir.ast.JVariable;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.sched.item.Component;
import com.android.sched.item.Description;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Represents a Phi statement in the SSA form.
 */
@Description("Phi elements for SSA.")
public class JPhiBlockElement extends JBasicBlockElement {
  @Nonnull
  private final JVariable var;

  @Nonnull
  private JSsaVariableDefRef lhs;

  @Nonnull
  private final JBasicBlock[] preds;

  @Nonnull
  private final JSsaVariableUseRef[] rhs;
  /**
   * Creates a new Phi statement.
   *
   * @param target Non-ssa variable this phi node is to denote to.
   * @param predsList Predecessors block list of this phi node.
   * @param info SourceInfo
   */
  public JPhiBlockElement(JVariable target, List<JBasicBlock> predsList, SourceInfo info) {
    // Phi instructions are not real instructions and should not throw any exception.
    super(info, ExceptionHandlingContext.EMPTY);
    this.lhs = new JSsaVariableDefRefPlaceHolder(info, target);
    lhs.updateParents(this);
    rhs = new JSsaVariableUseRef[predsList.size()];
    preds = new JBasicBlock[predsList.size()];
    int index = 0;
    for (JBasicBlock pred : predsList) {
      // We are going to insert a place holder first. The SSA renamer will replace the var ref
      // with a proper version.
      JSsaVariableUseRef use = lhs.makeRef(info);
      rhs[index] = use;
      preds[index] = pred;
      use.updateParents(this);
      index++;
    }
    this.var = lhs.getTarget();
  }

  @Override
  public void traverse(@Nonnull JVisitor visitor) {
    if (visitor.visit(this)) {
      visitor.accept(lhs);
      for (int i = 0; i < rhs.length; i++) {
        visitor.accept(rhs[i]);
      }
    }
    visitor.endVisit(this);
  }

  @Override
  public void traverse(@Nonnull ScheduleInstance<? super Component> schedule) throws Exception {
    schedule.process(this);
    lhs.traverse(schedule);
    for (JSsaVariableRef var : rhs) {
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
      lhs = (JSsaVariableDefRef) newNode;
      return;
    }

    for (int i = 0; i < rhs.length; i++) {
      if (rhs[i] == existingNode) {
        rhs[i] = (JSsaVariableUseRef) newNode;
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
  public JSsaVariableDefRef getLhs() {
    return lhs;
  }

  @Nonnull
  public JSsaVariableUseRef[] getRhs() {
    return Arrays.copyOf(rhs, rhs.length);
  }

  @Nonnull
  public JSsaVariableUseRef getRhs(@Nonnull JBasicBlock pred) {
    for (int i = 0; i < preds.length; i++) {
      if (preds[i] == pred) {
        return rhs[i];
      }
    }
    throw new RuntimeException();
  }

  @Override
  public boolean isTerminal() {
    return false;
  }
}
