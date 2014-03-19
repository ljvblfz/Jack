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

package com.android.jack.transformations.debug;

import com.android.jack.Options;
import com.android.jack.ir.JavaSourceIr;
import com.android.jack.ir.SourceInfo;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.util.filter.Filter;
import com.android.sched.item.Description;
import com.android.sched.item.Feature;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.util.config.ThreadConfig;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
/**
 * Sets parameter debug info to method debug info, and expression debug info to statement debug
 * info.
 */
@Description("Sets parameter debug info to method debug info, and expression debug info to "
    + "statement debug info")
@Name("DebugInfoNormalizer")
@Constraint(need = JavaSourceIr.class)
@Support(DebugInfoNormalizer.class)
public class DebugInfoNormalizer implements RunnableSchedulable<JMethod>, Feature {

  @Nonnull
  private final Filter<JMethod> filter = ThreadConfig.get(Options.METHOD_FILTER);

  private static class Visitor extends JVisitor {

    @CheckForNull
    private SourceInfo currentInfo;

    public Visitor() {
    }

    @Override
    public boolean visit(@Nonnull JMethod x) {
      currentInfo = x.getSourceInfo();
      return super.visit(x);
    }

    @Override
    public boolean visit(@Nonnull JParameter x) {
      assert currentInfo != null;
      x.setSourceInfo(currentInfo);
      return super.visit(x);
    }

    @Override
    public boolean visit(@Nonnull JStatement x) {
      currentInfo = x.getSourceInfo();
      return super.visit(x);
    }

    @Override
    public boolean visit(@Nonnull JExpression x) {
      assert currentInfo != null;
      x.setSourceInfo(currentInfo);
      return super.visit(x);
    }
  }

  @Override
  public void run(@Nonnull JMethod method) throws Exception {
    if (method.getEnclosingType().isExternal() || method.isNative() || method.isAbstract()
        || !filter.accept(this.getClass(), method)) {
      return;
    }
    Visitor visitor = new Visitor();
    visitor.accept(method);
  }
}
