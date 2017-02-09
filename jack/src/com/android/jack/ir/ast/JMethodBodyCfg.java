/*
 * Copyright 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.android.jack.ir.ast;

import com.google.common.collect.Lists;

import com.android.jack.Jack;
import com.android.jack.ir.ast.cfg.JCatchBasicBlock;
import com.android.jack.ir.ast.cfg.JControlFlowGraph;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.sched.item.Component;
import com.android.sched.item.Description;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Represents a the body of a method as a CFG. Can be Java or JSNI.
 */
@Description("Represents a the body of a Java method as CFG")
public class JMethodBodyCfg extends JConcreteMethodBody {
  @Nonnull
  private JControlFlowGraph cfg;

  @Nonnull
  private final List<JLocal> catchLocals = Lists.newLinkedList();

  @Nonnull
  private final List<JSsaVariableDefRef> params = new ArrayList<>();

  public JMethodBodyCfg(@Nonnull SourceInfo info, @Nonnull List<JLocal> locals) {
    super(info, locals);
    cfg = new JControlFlowGraph(this.getSourceInfo());
    cfg.updateParents(this);
  }

  @Override
  @Nonnull
  public JMethod getMethod() {
    JNode parent = getParent();
    assert parent instanceof JMethod;
    return (JMethod) parent;
  }

  @Nonnull
  public JControlFlowGraph getCfg() {
    return cfg;
  }

  public void addCatchLocal(@Nonnull JLocal catchLocal) {
    assert catchLocal.getParent() instanceof JCatchBasicBlock;
    assert !catchLocals.contains(catchLocal);
    catchLocals.add(catchLocal);
  }

  public void removeCatchLocal(@Nonnull JLocal catchLocal) {
    assert catchLocals.contains(catchLocal);
    catchLocals.remove(catchLocal);
  }

  @Nonnegative
  public int getNumCatchLocals() {
    return catchLocals.size();
  }

  @Nonnull
  public List<JLocal> getCatchLocals() {
    return Jack.getUnmodifiableCollections().getUnmodifiableList(catchLocals);
  }

  public void addSsaParamDef(@Nonnull JSsaVariableDefRef param) {
    params.add(param);
  }

  @Nonnull
  public List<JSsaVariableDefRef> getSsaParamsDefs() {
    return params;
  }

  @Override
  public void traverse(@Nonnull JVisitor visitor) {
    if (visitor.visit(this)) {
      acceptLocals(visitor);
      visitor.accept(cfg);
    }
    visitor.endVisit(this);
  }

  @Override
  public void traverse(@Nonnull ScheduleInstance<? super Component> schedule) throws Exception {
    schedule.process(this);
    traverseLocals(schedule);
    cfg.traverse(schedule);
  }

  @Override
  protected void replaceImpl(@Nonnull JNode existingNode, @Nonnull JNode newNode) {
    if (cfg == existingNode) {
      cfg = (JControlFlowGraph) newNode;
    } else {
      super.replaceImpl(existingNode, newNode);
    }
  }

  @Override
  public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest request) throws Exception {
    visitor.visit(this, request);
  }
}
