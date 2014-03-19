/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.jack.transformations;

import com.android.jack.Options;
import com.android.jack.ir.ast.JLocal;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodBody;
import com.android.jack.ir.ast.JVariable;
import com.android.jack.ir.ast.JVariableRef;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.util.filter.Filter;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.util.config.ThreadConfig;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Remove unused locals.
 */
@Description("Remove unused locals.")
@Constraint(need = JLocal.class)
public class UnusedLocalRemover implements RunnableSchedulable<JMethod> {

  @Nonnull
  private final Filter<JMethod> filter = ThreadConfig.get(Options.METHOD_FILTER);

  private static class Visitor extends JVisitor {

    @CheckForNull
    private List<JLocal> localsToDelete;

    @Override
    public boolean visit(@Nonnull JMethodBody methodBody) {
      localsToDelete = new LinkedList<JLocal>(methodBody.getLocals());
      return !localsToDelete.isEmpty();
    }

    @Override
    public boolean visit(@Nonnull JVariableRef varRef) {
      JVariable variable = varRef.getTarget();

      assert localsToDelete != null;
      localsToDelete.remove(variable);

      return super.visit(varRef);
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

    JMethodBody body = (JMethodBody) method.getBody();
    assert body != null;
    assert visitor.localsToDelete != null;
    body.removeLocals(visitor.localsToDelete);
  }
}
