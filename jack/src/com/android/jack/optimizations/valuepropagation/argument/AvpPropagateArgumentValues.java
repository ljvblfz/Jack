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

import com.android.jack.Jack;
import com.android.jack.annotations.DisableArgumentValuePropagationOptimization;
import com.android.jack.ir.ast.JAnnotationType;
import com.android.jack.ir.ast.JLocalRef;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JParameterRef;
import com.android.jack.ir.ast.JValueLiteral;
import com.android.jack.ir.ast.JVariable;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.ast.cfg.JBasicBlock;
import com.android.jack.ir.ast.cfg.JBasicBlockElement;
import com.android.jack.ir.ast.cfg.JControlFlowGraph;
import com.android.jack.ir.ast.cfg.JSimpleBasicBlock;
import com.android.jack.ir.ast.cfg.JThrowingExpressionBasicBlock;
import com.android.jack.ir.ast.cfg.JVariableAsgBlockElement;
import com.android.jack.ir.ast.cfg.mutations.BasicBlockBuilder;
import com.android.jack.optimizations.cfg.CfgVarUtils;
import com.android.jack.optimizations.common.LiteralValueListTracker;
import com.android.jack.optimizations.common.OptimizerUtils;
import com.android.jack.transformations.LocalVarCreator;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.util.NamingTools;
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
@Use(CfgVarUtils.ReplaceWithLocal.class)
@Name("ArgumentValuePropagation: PropagateArgumentValues")
public class AvpPropagateArgumentValues extends AvpSchedulable
    implements RunnableSchedulable<JControlFlowGraph> {

  @Nonnull
  public final JAnnotationType disablingAnnotationType =
      Jack.getSession().getPhantomLookup().getAnnotationType(
          NamingTools.getTypeSignatureName(
              DisableArgumentValuePropagationOptimization.class.getName()));

  @Nonnull
  private final Tracer tracer = TracerFactory.getTracer();

  @Override
  public void run(@Nonnull final JControlFlowGraph cfg) {
    final JMethod method = cfg.getMethod();

    LiteralValueListTracker tracker =
        MethodCallArgumentsMarker.getTrackerAndRemoveMarker(method);
    boolean isTainted = TaintedMethodMarker.checkIfTaintedAndRemoveMarker(method);

    if (isTainted || tracker == null || !tracker.hasAtLeastOneLiteral() ||
        method.isAbstract() || method.isNative() ||
        !method.getAnnotations(disablingAnnotationType).isEmpty() ||
        !method.getEnclosingType().getAnnotations(disablingAnnotationType).isEmpty()) {
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
      @Override public void endVisit(@Nonnull JVariableAsgBlockElement x) {
        JVariable variable = x.getVariable();
        if (variable instanceof JParameter) {
          if (paramValues.remove(variable) != null) {
            tracer.getStatistic(PARAMETER_IS_WRITTEN_TO).incValue();
          }
        }
      }
    };
    asgAnalyzer.accept(cfg);

    if (paramValues.size() == 0) {
      // All eligible parameters are mutated in the method
      return;
    }

    // Substitute parameter with value
    class Processor extends JVisitor {
      @Nonnull
      private final TransformationRequest request = new TransformationRequest(method);
      @Nonnull
      private final CfgVarUtils helper = new CfgVarUtils();
      @Nonnull
      private final LocalVarCreator varCreator = new LocalVarCreator(method, "avp");

      @Override
      public void endVisit(@Nonnull JParameterRef x) {
        assert !x.canThrow();
        JParameter parameter = x.getParameter();
        JValueLiteral literal = paramValues.get(parameter);

        if (literal != null && parameter.getAnnotations(disablingAnnotationType).isEmpty()) {
          literal = OptimizerUtils.cloneExpression(literal);
          literal.setSourceInfo(x.getSourceInfo());

          if (literal.canThrow()) {
            // If the literal can throw, we are replacing non-throwing expression
            // with throwing one. Thus we need to split the basic block and make it
            // throwing

            // First we replace the param reference with a temp local
            JLocalRef tmpRef = helper.replaceWithLocal(varCreator, x);

            // The local initialization is inserted before the element where
            // the local is being used. We split the basic block such that
            // The beginning of the block up to the initialization becomes a
            // new simple basic block
            JBasicBlockElement element = tmpRef.getParent(JBasicBlockElement.class);
            JBasicBlock basicBlock = element.getBasicBlock();
            JSimpleBasicBlock preBlock = basicBlock.split(basicBlock.indexOf(element));

            // Create a new throwing expression basic block and new JThrowingEx
            JThrowingExpressionBasicBlock newBlock =
                new BasicBlockBuilder(cfg).append(preBlock).removeLast()
                    .createThrowingExprBlock(preBlock.getPrimarySuccessor());
            preBlock.detach(newBlock);
          }

          // Finally, replace the parameter reference with the literal
          request.append(new Replace(x, literal));
          tracer.getStatistic(ARGUMENT_VALUES_PROPAGATED).incValue();
        }
      }
    }

    Processor processor = new Processor();
    processor.accept(method);
    processor.request.commit();
  }
}
