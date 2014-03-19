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
import com.android.jack.ir.SourceOrigin;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JThisRef;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.transformations.threeaddresscode.ThreeAddressCodeForm;
import com.android.jack.util.filter.Filter;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.util.config.ThreadConfig;

import javax.annotation.Nonnull;
/**
 * Sets debug info of implicit {@code this} references.
 */
@Description("Sets debug info of implicit 'this' references.")
@Name("ThisRefDebugInfoAdder")
@Constraint(need = JavaSourceIr.class, no = ThreeAddressCodeForm.class)
@Support(LineDebugInfo.class)
public class ThisRefDebugInfoAdder implements RunnableSchedulable<JMethod> {

  @Nonnull
  private final Filter<JMethod> filter = ThreadConfig.get(Options.METHOD_FILTER);

  private static class Visitor extends JVisitor {

    public Visitor() {
    }

    @Override
    public boolean visit(@Nonnull JThisRef x) {
      if (x.getSourceInfo() == SourceOrigin.UNKNOWN) {
        x.setSourceInfo(x.getParent().getSourceInfo());
      }
      return super.visit(x);
    }
  }

  @Override
  public void run(@Nonnull JMethod method) throws Exception {
    if (method.getEnclosingType().isExternal() || method.isNative() || method.isAbstract()
        || !filter.accept(ThisRefDebugInfoAdder.class, method)) {
      return;
    }
    Visitor visitor = new Visitor();
    visitor.accept(method);
  }
}
