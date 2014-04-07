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


import com.android.jack.Jack;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.sched.item.Component;
import com.android.sched.item.Description;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Represents a the body of a method. Can be Java or JSNI.
 */
@Description("Represents a the body of a Java method")
public class JMethodBody extends JAbstractMethodBody {

  @Nonnull
  private JBlock block;
  @Nonnull
  private final List<JLocal> locals = new LinkedList<JLocal>();

  public JMethodBody(@Nonnull SourceInfo info, @Nonnull JBlock block) {
    super(info);
    this.block = block;
  }

  /**
   * Adds a local to this method body.
   */
  public void addLocal(@Nonnull JLocal local) {
    locals.add(local);
  }

  @Nonnull
  public JBlock getBlock() {
    return block;
  }

  /**
   * Returns this method's local variables.
   */
  @Nonnull
  public List<JLocal> getLocals() {
    return Jack.getUnmodifiableCollections().getUnmodifiableList(locals);
  }

  @Nonnull
  public List<JStatement> getStatements() {
    return block.getStatements();
  }

  @Override
  public boolean isNative() {
    return false;
  }

  /**
   * Removes a local from this method body.
   */
  public void removeLocal(@Nonnull JLocal localToDelete) {
    locals.remove(localToDelete);
  }

  /**
   * Removes locals from this method body.
   */
  public void removeLocals(@Nonnull List<JLocal> localsToDelete) {
    locals.removeAll(localsToDelete);
  }

  @Override
  public void traverse(@Nonnull JVisitor visitor) {
    if (visitor.visit(this)) {
      visitor.accept(locals);
      visitor.accept(block);
    }
    visitor.endVisit(this);
  }

  @Override
  public void traverse(@Nonnull ScheduleInstance<? super Component> schedule) throws Exception {
    schedule.process(this);
    for (JLocal local : locals) {
      local.traverse(schedule);
    }
    block.traverse(schedule);
  }

  @Override
  protected void transform(@Nonnull JNode existingNode, @CheckForNull JNode newNode,
      @Nonnull Transformation transformation) throws UnsupportedOperationException {
    if (!transform(locals, existingNode, (JLocal) newNode, transformation)) {
      super.transform(existingNode, newNode, transformation);
    }
  }

  @Override
  protected void replaceImpl(@Nonnull JNode existingNode, @Nonnull JNode newNode)
      throws UnsupportedOperationException {
    assert newNode != null;

    if (block == existingNode) {
      block = (JBlock) newNode;
    } else {
      super.replaceImpl(existingNode, newNode);
    }
  }

  @Override
  public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visitor.visit(this, transformRequest);
  }
}
