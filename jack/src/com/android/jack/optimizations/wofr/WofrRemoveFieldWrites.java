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

package com.android.jack.optimizations.wofr;

import com.android.jack.Jack;
import com.android.jack.cfg.BasicBlock;
import com.android.jack.cfg.ControlFlowGraph;
import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JExpressionStatement;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JFieldRef;
import com.android.jack.ir.ast.JLocal;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JPrimitiveType;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JThisRef;
import com.android.jack.lookup.JPhantomLookup;
import com.android.jack.optimizations.Optimizations;
import com.android.jack.optimizations.common.JlsNullabilityChecker;
import com.android.jack.transformations.LocalVarCreator;
import com.android.jack.transformations.request.AppendBefore;
import com.android.jack.transformations.request.PrependAfter;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.schedulable.Use;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/** Write-only field removal, second phase: remove field assignments */
@Description("Write-only field removal, field writes removal")
@Constraint(need = { ControlFlowGraph.class,
                     JFieldRef.class,
                     FieldReadWriteCountsMarker.class })
@Transform(modify = FieldReadWriteCountsMarker.class,
    add = JExpressionStatement.class)
@Name("WriteOnlyFieldRemoval: RemoveFieldWrites")
@Use({ JlsNullabilityChecker.class,
       LocalVarCreator.class })
public class WofrRemoveFieldWrites extends WofrSchedulable
    implements RunnableSchedulable<JMethod> {

  private final boolean preserveObjectLifetime =
      ThreadConfig.get(Optimizations.WriteOnlyFieldRemoval.PRESERVE_OBJECT_LIFETIME).booleanValue();

  private final boolean ensureTypeInitializers =
      ThreadConfig.get(Optimizations.WriteOnlyFieldRemoval.ENSURE_TYPE_INITIALIZERS).booleanValue();

  public final boolean preserveNullChecks =
      ThreadConfig.get(Optimizations.WriteOnlyFieldRemoval.PRESERVE_NULL_CHECKS).booleanValue();

  @Nonnull
  private final JPhantomLookup phantomLookup = Jack.getSession().getPhantomLookup();

  @Nonnull
  private final Tracer tracer = TracerFactory.getTracer();

  /** Classifies field assignment action */
  private enum Action {
    /** Field is either not eligible for optimization or write cannot be removed */
    None,
    /** Field write can be replaced with just expression */
    Expression,
    /** Field write can be replaced with receiver and expression */
    ReceiverAndExpression,
    /** Field write can be replaced with receiver, expression and null-check */
    ReceiverExpressionAndNullCheck
  }

  /** Classify field assignment */
  private Action classify(@Nonnull JMethod method, @Nonnull JFieldRef ref) {

    JField field = ref.getFieldId().getField();
    if (field == null ||
        !field.getAnnotations(disablingAnnotationType).isEmpty() ||
        !field.getEnclosingType().getAnnotations(disablingAnnotationType).isEmpty()) {
      return Action.None;
    }

    if (field.isVolatile()) {
      return Action.None; // Volatile field has a special semantic declared in JMM
    }

    if (FieldReadWriteCountsMarker.hasReads(field)) {
      return Action.None; // The field is read
    }

    JDefinedClassOrInterface fieldOwningType = field.getEnclosingType();

    if (!fieldOwningType.isToEmit()) {
      return Action.None; // Not a field of a type to be emitted
    }

    if (preserveObjectLifetime &&
        !(field.getType() instanceof JPrimitiveType ||
            !FieldReadWriteCountsMarker.hasNonLiteralWrites(field))) {
      // Field having non-primitive type that is assigned a non-constant value
      // (an instance of JValueLiteral) at least once is not eligible: may affect
      // object's live-time
      return Action.None;
    }

    if (ensureTypeInitializers && field.isStatic() &&
        !fieldOwningType.isSameType(method.getEnclosingType())) {
      // Static field is accessed outside the scope of the field containing type:
      // may affect class initialization
      return Action.None;
    }

    if (field.isStatic() || ref.getInstance() instanceof JThisRef) {
      // The field is static OR the field is an instance field accessed
      // via this reference: no side-effects while calculating receiver
      return Action.Expression;
    }

    assert !field.isStatic();

    return preserveNullChecks
        ? Action.ReceiverExpressionAndNullCheck
        : Action.ReceiverAndExpression;
  }

  @Override
  public void run(@Nonnull final JMethod method) {
    if (method.isAbstract() || method.isNative()) {
      return;
    }

    final TransformationRequest request = new TransformationRequest(method);
    final LocalVarCreator varCreator = new LocalVarCreator(method, "wofr");
    final JlsNullabilityChecker nullChecker = new JlsNullabilityChecker(varCreator, phantomLookup);

    class Processor {
      private void handleExprStmt(@Nonnull JExpressionStatement stmt) {
        JExpression expr = stmt.getExpr();
        if (expr instanceof JAsgOperation) {
          JAsgOperation asg = (JAsgOperation) expr;
          JExpression lhs = asg.getLhs();
          if (lhs instanceof JFieldRef) {
            JFieldRef ref = (JFieldRef) lhs;

            switch (classify(method, ref)) {
              case None:
                // Cannot remove field assignment
                return;

              case ReceiverExpressionAndNullCheck:
                // Replace received with temp local
                JLocal local = handleFieldReceiver(asg, true);
                assert local != null;
                // Replace assignment expression with rhs
                handleAssignmentRhs(asg);
                // Append null-check
                JStatement nullCheck =
                    nullChecker.createNullCheck(
                        local.makeRef(local.getSourceInfo()), request);
                request.append(new PrependAfter(stmt, nullCheck));
                break;

              case ReceiverAndExpression:
                // Prepend statement with expression statement representing <receiver>
                handleFieldReceiver(asg, false);
                // Replace assignment expression with rhs
                handleAssignmentRhs(asg);
                break;

              case Expression:
                // Replace assignment expression with rhs
                handleAssignmentRhs(asg);
                break;
            }

            JField field = ref.getFieldId().getField();
            assert field != null;
            FieldReadWriteCountsMarker.unmarkWrite(field);
            tracer.getStatistic(FIELD_WRITES_REMOVED).incValue();
          }
        }
      }

      private void handleAssignmentRhs(@Nonnull JAsgOperation asg) {
        JExpression rhs = asg.getRhs();
        if (rhs instanceof JMethodCall) {
          request.append(new Replace(asg, rhs));
        } else {
          JExpression lhs = asg.getLhs();
          JLocal local =
              varCreator.createTempLocal(
                  lhs.getType(), lhs.getSourceInfo(), request);
          request.append(new Replace(lhs, local.makeRef(local.getSourceInfo())));
        }
      }

      @CheckForNull
      private JLocal handleFieldReceiver(@Nonnull JAsgOperation asg, boolean forceLocal) {
        JExpression receiver = ((JFieldRef) asg.getLhs()).getInstance();
        assert receiver != null;

        JLocal local = null;

        if (!(receiver instanceof JMethodCall) || forceLocal) {
          // Need a temp local
          local = varCreator.createTempLocal(
              receiver.getType(), receiver.getSourceInfo(), request);
          receiver = new JAsgOperation(
              receiver.getSourceInfo(), local.makeRef(receiver.getSourceInfo()), receiver);
        }

        JExpressionStatement stmt =
            new JExpressionStatement(asg.getSourceInfo(), receiver);
        request.append(new AppendBefore(asg.getParent(), stmt));
        return local;
      }
    }

    Processor processor = new Processor();
    ControlFlowGraph cfg = method.getMarker(ControlFlowGraph.class);
    assert cfg != null;

    for (BasicBlock bb : cfg.getNodes()) {
      for (JStatement stmt : bb.getStatements()) {
        if (stmt instanceof JExpressionStatement) {
          processor.handleExprStmt((JExpressionStatement) stmt);
        }
      }
    }

    request.commit();
  }
}
