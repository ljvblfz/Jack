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

package com.android.jack.analysis;

import com.android.jack.Options;
import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JBinaryOperation;
import com.android.jack.ir.ast.JExceptionRuntimeValue;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JVariable;
import com.android.jack.ir.ast.JVariableRef;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.scheduling.filter.TypeWithoutPrebuiltFilter;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.Protect;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.schedulable.With;
import com.android.sched.util.config.ThreadConfig;

import javax.annotation.Nonnull;

/**
 * Add definition marker on IR.
 */
@Description("Add definition marker on IR.")
@Constraint(need = {JExceptionRuntimeValue.class})
@Transform(add = {DefinitionMarker.class})
@Protect(add = {JParameter.class, JAsgOperation.class, JAsgOperation.NonReusedAsg.class},
    unprotect = @With(remove = DefinitionMarker.class))
@Filter(TypeWithoutPrebuiltFilter.class)
public class DefinitionMarkerAdder implements RunnableSchedulable<JMethod> {

  @Nonnull
  private final com.android.jack.util.filter.Filter<JMethod> filter =
      ThreadConfig.get(Options.METHOD_FILTER);

  private static class Visitor extends JVisitor {


    @Override
    public boolean visit(@Nonnull JMethod jmethod) {
      JVariable thisVar = jmethod.getThis();
      if (thisVar != null) {
        if (!thisVar.containsMarker(DefinitionMarker.class)) {
          thisVar.addMarkerIfAbsent(new DefinitionMarker(thisVar));
        }
      }
      return super.visit(jmethod);
    }

    @Override
    public boolean visit(@Nonnull JParameter parameter) {
      parameter.addMarker(new DefinitionMarker(parameter));
      return super.visit(parameter);
    }

    @Override
    public boolean visit(@Nonnull JBinaryOperation binary) {
      if (binary instanceof JAsgOperation) {
        JAsgOperation assign = (JAsgOperation) binary;
        if (assign.getLhs() instanceof JVariableRef) {
          binary.addMarker(new DefinitionMarker(binary));
        }
      }

      return super.visit(binary);
    }
  }

  @Override
  public void run(@Nonnull JMethod method) throws Exception {
    if (method.isNative() || method.isAbstract() || !filter.accept(this.getClass(), method)) {
      return;
    }

    Visitor visitor = new Visitor();
    visitor.accept(method);
  }
}
