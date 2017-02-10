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

package com.android.jack.transformations.ast.string;

import com.android.jack.Jack;
import com.android.jack.JackAbortException;
import com.android.jack.Options;
import com.android.jack.ir.ast.JAbstractStringLiteral;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JStringLiteral;
import com.android.jack.ir.ast.JTypeStringLiteral;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.lookup.JMethodLookupException;
import com.android.jack.reporting.Reporter.Severity;
import com.android.jack.transformations.TransformationException;
import com.android.jack.transformations.ast.string.parameterrefiners.AtomicLongIntUpdaterParameterRefiner;
import com.android.jack.transformations.ast.string.parameterrefiners.AtomicReferenceUpdaterParameterRefiner;
import com.android.jack.transformations.ast.string.parameterrefiners.ForNameParameterRefiner;
import com.android.jack.transformations.ast.string.parameterrefiners.GetDeclaredFieldsParameterRefiner;
import com.android.jack.transformations.ast.string.parameterrefiners.GetDeclaredMethodParameterRefiner;
import com.android.jack.transformations.ast.string.parameterrefiners.GetFieldParameterRefiner;
import com.android.jack.transformations.ast.string.parameterrefiners.GetMethodParameterRefiner;
import com.android.jack.transformations.ast.string.parameterrefiners.StringParameterRefiner;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.util.filter.Filter;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Access;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.schedulable.Use;
import com.android.sched.util.config.ThreadConfig;

import javax.annotation.Nonnull;

/**
 * Refine {@code JStringLiteral} used into reflection call into more specific string literals.
 */
@Description("Refine JStringLiteral used into reflection call into more specific string literals.")
@Name("ReflectionStringLiteralRefiner")
@Constraint(need = JStringLiteral.class)
@Transform(add = JTypeStringLiteral.class)
@Use({ForNameParameterRefiner.class,
    GetFieldParameterRefiner.class,
    GetDeclaredFieldsParameterRefiner.class,
    AtomicLongIntUpdaterParameterRefiner.class,
    AtomicReferenceUpdaterParameterRefiner.class,
    GetMethodParameterRefiner.class,
    GetDeclaredMethodParameterRefiner.class})
// Uses refiners which look up types.
@Access(JSession.class)
public class ReflectionStringLiteralRefiner implements RunnableSchedulable<JMethod> {

  @Nonnull
  private final Filter<JMethod> filter = ThreadConfig.get(Options.METHOD_FILTER);

  @Nonnull
  private final StringParameterRefiner[] refiners = new StringParameterRefiner[] {
      new ForNameParameterRefiner() ,
      new GetFieldParameterRefiner(),
      new GetDeclaredFieldsParameterRefiner(),
      new AtomicLongIntUpdaterParameterRefiner(),
      new AtomicReferenceUpdaterParameterRefiner(),
      new GetMethodParameterRefiner(),
      new GetDeclaredMethodParameterRefiner()
  };


  private class Visitor extends JVisitor {

    @Nonnull
    private final TransformationRequest tr;

    public Visitor(@Nonnull TransformationRequest tr) {
      this.tr = tr;
    }

    @Override
    public boolean visit(@Nonnull JMethodCall call) {
      try {
        processCall(call);
      } catch (JMethodLookupException e) {
        TransformationException te = new TransformationException(e);
        Jack.getSession().getReporter().report(Severity.FATAL, te);
        throw new JackAbortException(te);
      }
      return true;
    }

    private void processCall(@Nonnull JMethodCall call) throws JMethodLookupException {
      for (StringParameterRefiner refiner : refiners) {
        if (refiner.isApplicable(call))  {
          JStringLiteral paramToRefine = refiner.getExpressionToRefine(call);
          if (paramToRefine != null) {
            JAbstractStringLiteral refinedParameter =
                refiner.getRefinedExpression(call, paramToRefine);
            if (refinedParameter != null) {
              tr.append(new Replace(paramToRefine, refinedParameter));
            }
          }
        }
      }
    }
  }

  @Override
  public void run(@Nonnull JMethod method) {
    if (method.isNative() || method.isAbstract() || !filter.accept(this.getClass(), method)) {
      return;
    }

    TransformationRequest tr = new TransformationRequest(method);
    Visitor visitor = new Visitor(tr);
    visitor.accept(method);
    tr.commit();
  }
}
