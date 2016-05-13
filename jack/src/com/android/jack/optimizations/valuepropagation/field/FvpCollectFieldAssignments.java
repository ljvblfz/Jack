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

package com.android.jack.optimizations.valuepropagation.field;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import com.android.jack.analysis.UseDefsMarker;
import com.android.jack.analysis.common.ReachabilityAnalyzer;
import com.android.jack.cfg.BasicBlock;
import com.android.jack.cfg.ControlFlowGraph;
import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JConstructor;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JExpressionStatement;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JFieldRef;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JThisRef;
import com.android.jack.ir.ast.JValueLiteral;
import com.android.jack.optimizations.common.OptimizerUtils;
import com.android.jack.optimizations.common.TypeToBeEmittedMarker;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Access;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

/**
 * Field value propagation, second phase: analyze all the methods and constructors and find
 * out all the values that can be assigned to fields.
 */
@Description("Field value propagation, field value collection")
@Constraint(need = { ControlFlowGraph.class,
                     TypeToBeEmittedMarker.class,
                     UseDefsMarker.class })
@Transform(add = FieldSingleValueMarker.class)
@Access(JDefinedClassOrInterface.class)
@Name("FieldValuePropagation: CollectFieldAssignments")
public class FvpCollectFieldAssignments extends FvpSchedulable
    implements RunnableSchedulable<JMethod> {

  private static class ConstructorAnalyzer
      extends ReachabilityAnalyzer<Multimap<JField, JExpression>> {

    /**
     * Note that the state only contains fields with the
     * same static-ness as the method being analyzed
     */
    @Nonnull
    final Multimap<JField, JExpression> defaultState = HashMultimap.create();

    @Nonnull
    private final JMethod constructor;

    ConstructorAnalyzer(@Nonnull JMethod constructor) {
      this.constructor = constructor;
      for (JField field : constructor.getEnclosingType().getFields()) {
        if (field.isStatic() == constructor.isStatic()) {
          FieldSingleValueMarker marker = FieldSingleValueMarker.getOrCreate(field);
          if (marker != null) {
            defaultState.put(field, createDefaultValue(field));
          }
        }
      }
    }

    @Nonnull
    @Override public ControlFlowGraph getCfg() {
      ControlFlowGraph cfg = constructor.getMarker(ControlFlowGraph.class);
      assert cfg != null;
      return cfg;
    }

    @Nonnull
    @Override protected Multimap<JField, JExpression> newState(boolean entry) {
      return entry ? cloneState(defaultState)
          : HashMultimap.<JField, JExpression>create();
    }

    @Nonnull
    @Override protected Multimap<JField, JExpression> cloneState(
        @Nonnull Multimap<JField, JExpression> state) {
      return HashMultimap.create(state);
    }

    @Override protected void copyState(
        @Nonnull Multimap<JField, JExpression> src,
        @Nonnull Multimap<JField, JExpression> dest) {
      dest.clear();
      dest.putAll(src);
    }

    @Override protected void mergeState(
        @Nonnull Multimap<JField, JExpression> state,
        @Nonnull Multimap<JField, JExpression> otherState) {
      state.putAll(otherState);
    }

    @Override protected void processStatement(
        @Nonnull Multimap<JField, JExpression> outBs, @Nonnull JStatement stmt) {
      // Check if the field is being assigned a value and mark the value in the state.
      if (stmt instanceof JExpressionStatement) {
        JExpression expr = ((JExpressionStatement) stmt).getExpr();
        if (expr instanceof JAsgOperation) {
          JAsgOperation assignment = (JAsgOperation) expr;

          JExpression lhs = assignment.getLhs();
          if (lhs instanceof JFieldRef) {
            JFieldRef fieldRef = (JFieldRef) lhs;
            JField field = fieldRef.getFieldId().getField();
            if (field == null ||
                !TypeToBeEmittedMarker.isToBeEmitted(field.getEnclosingType())) {
              return;
            }

            // Field should be tracked in this initializer, and in case it is
            // an instance field, the received should also be 'this' reference
            JExpression expression = assignment.getRhs();
            if (defaultState.containsKey(field) &&  // is tracked in this initializer
                (field.isStatic() || fieldRef.getInstance() instanceof JThisRef)) {
              // Mark the field being (re-)assigned in this basic block
              outBs.removeAll(field);
              outBs.put(field, expression);

            } else {
              // Otherwise it is a regular field reference outside the constructor
              // which initializes this field, just mark the field assignment
              FieldSingleValueMarker.markValue(field, expression);
            }
          }

        } else if (expr instanceof JMethodCall) {
          // If an instance constructor delegates to another constructor,
          // we check that the all the tracked fields have no value or have only
          // default values assigned so far, we invalidate the fields which do not
          // satisfy this condition since we don't know if the value is reassigned
          // in the constructor call.
          if (!constructor.isStatic() &&
              OptimizerUtils.isConstructorDelegation(
                  (JMethodCall) expr, (JConstructor) constructor)) {
            // If the field has anything but default value, assign to it a complex
            // expression to make sure if it is not considered a constant if not reassigned.
            Iterator<Map.Entry<JField, JExpression>> iterator = outBs.entries().iterator();
            while (iterator.hasNext()) {
              Map.Entry<JField, JExpression> entry = iterator.next();
              JExpression value = entry.getValue();
              if (value instanceof JValueLiteral &&
                  ((JValueLiteral) value).isTypeValue()) {
                // Just remove it
                iterator.remove();
              } else {
                // Otherwise mark it as a complex expression, it does
                // not really matter which expression it is as long as
                // it is not a value value literal.
                entry.setValue(expr);
              }
            }
          }
        }
      }
    }

    @Override
    protected void finalize(
        @Nonnull List<Multimap<JField, JExpression>> in,
        @Nonnull List<Multimap<JField, JExpression>> out,
        @Nonnull List<Multimap<JField, JExpression>> outException) {

      // Recalculate 'in' set for exit block
      Multimap<JField, JExpression> exitBlockState = newState(false);
      recalculateInSet(
          getCfg().getExitNode(), /* ignoreExceptionPath: */ true,
          exitBlockState, out, outException);

      for (Map.Entry<JField, Collection<JExpression>> entry :
          exitBlockState.asMap().entrySet()) {
        assert entry.getKey().isStatic() == constructor.isStatic();
        FieldSingleValueMarker.markValues(entry.getKey(), entry.getValue());
      }
    }
  }

  private void analyzeRegularMethod(@Nonnull JMethod method) {
    ControlFlowGraph cfg = method.getMarker(ControlFlowGraph.class);
    assert cfg != null;
    for (BasicBlock block : cfg.getNodes()) {
      for (JStatement stmt : block.getStatements()) {
        // Check if the field is being assigned a value and mark the value in the state.
        if (stmt instanceof JExpressionStatement) {
          JExpression expr = ((JExpressionStatement) stmt).getExpr();
          if (expr instanceof JAsgOperation) {
            JAsgOperation assignment = (JAsgOperation) expr;
            JExpression lhs = assignment.getLhs();
            if (lhs instanceof JFieldRef) {
              JField field = ((JFieldRef) lhs).getFieldId().getField();
              if (field != null &&
                  TypeToBeEmittedMarker.isToBeEmitted(field.getEnclosingType())) {
                // The field is being assigned outside its construction,
                // just mark the field assignment
                FieldSingleValueMarker.markValue(field, assignment.getRhs());
              }
            }
          }
        }
      }
    }
  }

  @Override
  public void run(@Nonnull JMethod method) throws Exception {
    if (!preserveJls && !method.isAbstract() && !method.isNative()) {
      if (OptimizerUtils.isConstructor(method)) {
        // Each constructor may assign the values of this type several (zero, one or
        // multiple) times, we collect this information across all the constructors.
        // The analyzer calculate which values reach the constructor's exit block and
        // add only them to the marker, for the rest of the fields it just marks
        // assignments without reachability analysis
        new ConstructorAnalyzer(method).analyze();

      } else {
        analyzeRegularMethod(method);
      }
    }
  }
}
