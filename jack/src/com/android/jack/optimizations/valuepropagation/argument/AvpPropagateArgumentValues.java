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

package com.android.jack.optimizations.valuepropagation.argument;

import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JParameterRef;
import com.android.jack.ir.ast.JValueLiteral;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.optimizations.common.ExpressionReplaceHelper;
import com.android.jack.optimizations.common.LiteralValueListTracker;
import com.android.jack.optimizations.common.OptimizerUtils;
import com.android.jack.transformations.LocalVarCreator;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.schedulable.Use;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

/** Propagate argument value when possible */
@Description("Argument value propagation, propagate argument value when possible")
@Constraint(need = { MethodCallArgumentsMarker.class,
                     AvpSchedulable.TaintedMethodMarker.class })
@Transform(remove = { MethodCallArgumentsMarker.class,
                      AvpSchedulable.TaintedMethodMarker.class })
@Use(ExpressionReplaceHelper.class)
@Name("ArgumentValuePropagation: PropagateArgumentValues")
public class AvpPropagateArgumentValues extends AvpSchedulable
    implements RunnableSchedulable<JMethod> {

  @Nonnull
  private final Tracer tracer = TracerFactory.getTracer();

  @Override
  public void run(@Nonnull final JMethod method) throws Exception {
    LiteralValueListTracker tracker =
        MethodCallArgumentsMarker.getTrackerAndRemoveMarker(method);
    boolean isTainted = TaintedMethodMarker.checkIfTaintedAndRemoveMarker(method);

    if (isTainted || tracker == null || !tracker.hasAtLeastOneLiteral() ||
        method.isAbstract() || method.isNative()) {
      return;
    }

    // All parameters with argument value eligible for propagation
    final Map<JParameter, JValueLiteral> paramValues = new HashMap<>();
    List<JParameter> params = method.getParams();
    for (int i = 0; i < params.size(); i++) {
      if (!tracker.isMultipleOrNonLiteralValue(i)) {
        JValueLiteral value = tracker.getConsolidatedValue(i);
        if (value != null) {
          paramValues.put(params.get(i), value);
        }
      }
    }

    // Detect parameters not being assigned to
    JVisitor asgAnalyzer = new JVisitor() {
      @Override public void endVisit(@Nonnull JParameterRef x) {
        JNode parent = x.getParent();
        if (parent instanceof JAsgOperation &&
            ((JAsgOperation) parent).getLhs() == x) {
          if (paramValues.remove(x.getParameter()) != null) {
            tracer.getStatistic(PARAMETER_IS_WRITTEN_TO).incValue();
          }
        }
      }
    };
    asgAnalyzer.accept(method);

    if (paramValues.size() == 0) {
      // All eligible parameters are mutated in the method
      return;
    }

    // Substitute parameter with value
    class Processor extends JVisitor {
      @Nonnull
      private TransformationRequest request =
          new TransformationRequest(method);
      @Nonnull
      private ExpressionReplaceHelper helper =
          new ExpressionReplaceHelper(new LocalVarCreator(method, "avp"));

      @Override public void endVisit(@Nonnull JParameterRef x) {
        JValueLiteral literal = paramValues.get(x.getParameter());
        if (literal != null) {
          literal = OptimizerUtils.cloneExpression(literal);
          literal.setSourceInfo(x.getSourceInfo());
          helper.replace(x, literal, request);
          tracer.getStatistic(ARGUMENT_VALUES_PROPAGATED).incValue();
        }
      }
    }

    Processor processor = new Processor();
    processor.accept(method);
    processor.request.commit();
  }
}
