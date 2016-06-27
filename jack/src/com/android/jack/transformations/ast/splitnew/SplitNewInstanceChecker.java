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

package com.android.jack.transformations.ast.splitnew;

import com.android.jack.Options;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JNewInstance;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.scheduling.filter.TypeWithoutPrebuiltFilter;
import com.android.jack.transformations.SanityChecks;
import com.android.jack.transformations.ast.NewInstanceRemoved;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.util.config.ThreadConfig;

import javax.annotation.Nonnull;

/**
 * This {@link RunnableSchedulable} checks that all occurrences of JSymbolicNewInstance
 * have been removed.
 */
@Description("Checkes that all occurrences of JSymbolicNewInstance have been removed.")
@Name("SplitNewInstanceChecker")
@Constraint(no = {JNewInstance.class}, need = {NewInstanceRemoved.class})
@Support(SanityChecks.class)
@Filter(TypeWithoutPrebuiltFilter.class)
public class SplitNewInstanceChecker implements RunnableSchedulable<JMethod> {

  @Nonnull
  private final com.android.jack.util.filter.Filter<JMethod> filter =
      ThreadConfig.get(Options.METHOD_FILTER);

  private static class Visitor extends JVisitor {

    @Override
    public boolean visit(@Nonnull JNewInstance newInstance) {
      throw new AssertionError("All JSymbolicNewInstance occurences should have been removed");
    }
  }

  @Override
  public void run(@Nonnull JMethod method) {
    if (method.isNative() || method.isAbstract() || !filter.accept(this.getClass(), method)) {
      return;
    }
    Visitor visitor = new Visitor();
    visitor.accept(method);
  }
}
