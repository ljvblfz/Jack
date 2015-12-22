/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.jack.transformations.assertion;

import com.android.jack.Options;
import com.android.jack.ir.ast.JAssertStatement;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.transformations.request.Remove;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.util.filter.Filter;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.ThreadConfig;

import javax.annotation.Nonnull;
/**
 * This {@link RunnableSchedulable} removes "assert" statements.
 */
@Description("Remove assert statements")
@Name("AssertionRemover")
@Constraint(need = {JAssertStatement.class})
@Transform(remove = {JAssertStatement.class})
@Support(DisabledAssertionFeature.class)
public class AssertionRemover implements RunnableSchedulable<JMethod> {

  @Nonnull
  private final Filter<JMethod> filter = ThreadConfig.get(Options.METHOD_FILTER);

  private static class Visitor extends JVisitor {

    @Nonnull
    private final TransformationRequest tr;

    public Visitor(@Nonnull TransformationRequest tr) {
      this.tr = tr;
    }

    @Override
    public void endVisit(@Nonnull JAssertStatement assertSt) {
      tr.append(new Remove(assertSt));
    }
  }

  @Override
  public synchronized void run(@Nonnull JMethod method) throws Exception {
    if (method.isNative() || method.isAbstract() || !filter.accept(this.getClass(), method)) {
      return;
    }
    TransformationRequest tr = new TransformationRequest(method);
    Visitor visitor = new Visitor(tr);
    visitor.accept(method);
    tr.commit();
  }
}
