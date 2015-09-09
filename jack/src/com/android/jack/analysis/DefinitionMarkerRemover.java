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
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JVariable;
import com.android.jack.ir.ast.JVariableRef;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.util.filter.Filter;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.ThreadConfig;

import javax.annotation.Nonnull;

/**
 * Remove definition marker on IR.
 */
@Description("Remove definition marker on IR.")
@Constraint(need = {DefinitionMarker.class})
@Transform(remove = {DefinitionMarker.class})
public class DefinitionMarkerRemover implements RunnableSchedulable<JMethod> {

  @Nonnull
  private final Filter<JMethod> filter = ThreadConfig.get(Options.METHOD_FILTER);
  private static class Visitor extends JVisitor {

    @Override
    public boolean visit(@Nonnull JMethod jmethod) {
      if (!jmethod.isStatic() && jmethod.getEnclosingType() instanceof JDefinedClass) {
        JVariable thisVar = jmethod.getThis();
        assert thisVar != null;
        thisVar.removeMarker(DefinitionMarker.class);
      }
      return super.visit(jmethod);
    }

    @Override
    public boolean visit(@Nonnull JParameter parameter) {
      parameter.removeMarker(DefinitionMarker.class);
      return super.visit(parameter);
    }

    @Override
    public boolean visit(@Nonnull JBinaryOperation binary) {
      if (binary instanceof JAsgOperation) {
        JAsgOperation assign = (JAsgOperation) binary;
        if (assign.getLhs() instanceof JVariableRef) {
          binary.removeMarker(DefinitionMarker.class);
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
