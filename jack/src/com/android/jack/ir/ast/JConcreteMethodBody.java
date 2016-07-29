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

package com.android.jack.ir.ast;

import com.android.jack.Jack;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.sched.item.Component;
import com.android.sched.scheduler.ScheduleInstance;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Base implementation for regular and cfg method body,
 * represents a the body of a method. Can be Java or JSNI.
 */
public abstract class JConcreteMethodBody extends JAbstractMethodBody {
  @Nonnull
  protected final List<JLocal> locals = new LinkedList<>();

  public JConcreteMethodBody(@Nonnull SourceInfo info) {
    this(info, Collections.<JLocal>emptyList());
  }

  public JConcreteMethodBody(@Nonnull SourceInfo info, @Nonnull List<JLocal> locals) {
    super(info);
    this.locals.addAll(locals);
  }

  /**
   * Adds a local to this method body.
   */
  public void addLocal(@Nonnull JLocal local) {
    locals.add(local);
  }

  /**
   * Returns this method's local variables.
   */
  @Nonnull
  public List<JLocal> getLocals() {
    return Jack.getUnmodifiableCollections().getUnmodifiableList(locals);
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

  final void acceptLocals(@Nonnull JVisitor visitor) {
    visitor.accept(locals);
  }

  final void traverseLocals(
      @Nonnull ScheduleInstance<? super Component> schedule) throws Exception {
    for (JLocal local : locals) {
      local.traverse(schedule);
    }
  }

  @Override
  protected void transform(@Nonnull JNode existingNode, @CheckForNull JNode newNode,
      @Nonnull Transformation transformation) throws UnsupportedOperationException {
    if (!transform(locals, existingNode, (JLocal) newNode, transformation)) {
      super.transform(existingNode, newNode, transformation);
    }
  }
}
