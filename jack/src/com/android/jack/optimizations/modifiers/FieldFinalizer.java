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

package com.android.jack.optimizations.modifiers;

import com.google.common.collect.Maps;

import com.android.jack.Jack;
import com.android.jack.analysis.common.ReachabilityAnalyzer;
import com.android.jack.annotations.DisableFieldFinalizerOptimization;
import com.android.jack.cfg.ControlFlowGraph;
import com.android.jack.ir.ast.JAnnotationType;
import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JConstructor;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JExpressionStatement;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JFieldInitializer;
import com.android.jack.ir.ast.JFieldRef;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JThisRef;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.optimizations.Optimizations;
import com.android.jack.transformations.threeaddresscode.ThreeAddressCodeForm;
import com.android.jack.util.NamingTools;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;
import com.android.sched.schedulable.Access;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;
import com.android.sched.util.log.stats.Counter;
import com.android.sched.util.log.stats.CounterImpl;
import com.android.sched.util.log.stats.StatisticId;

import java.util.BitSet;
import java.util.List;
import java.util.Map;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/** Make class fields final when possible */
public class FieldFinalizer {
  @Nonnull
  public static final StatisticId<Counter> FIELDS_FINALIZED = new StatisticId<>(
      "jack.optimization.fields-finalizer", "Fields made final",
      CounterImpl.class, Counter.class);

  /** Marker is used to mark fields which are NOT effectively final. */
  @Description("Marker is used to mark fields which are NOT effectively final.")
  @ValidOn(JField.class)
  public enum NotEffectivelyFinalField implements Marker {
    NOT_EFFECTIVELY_FINAL;

    @Override
    @Nonnull
    public Marker cloneIfNeeded() {
      throw new AssertionError();
    }

    public static boolean checkIfCanBeFinalAndRemoveMarker(@Nonnull JField field) {
      return field.removeMarker(NotEffectivelyFinalField.class) == null;
    }

    public static boolean checkIfCanBeFinal(@Nonnull JField field) {
      return !field.containsMarker(NotEffectivelyFinalField.class);
    }

    public static void markAsNotFinal(@Nonnull JField field) {
      assert !field.isVolatile() && !field.isFinal();
      field.addMarkerIfAbsent(NOT_EFFECTIVELY_FINAL);
    }
  }

  /** First phase: track all field assignments */
  @Description("Field finalizer, collecting assignment information phase")
  @Constraint(need = ThreeAddressCodeForm.class)
  @Transform(add = NotEffectivelyFinalField.class)
  public static class CollectionPhase
      implements RunnableSchedulable<JMethod> {

    @Override
    public void run(@Nonnull JMethod method) {
      if (!method.isNative() && !method.isAbstract()) {
        new Visitor(method).accept(method);
      }
    }

    private static class Visitor extends JVisitor {
      @CheckForNull
      final JMethod constructor;

      private Visitor(@Nonnull JMethod method) {
        this.constructor = isConstructor(method) ? method : null;
      }

      private boolean isAssignment(@Nonnull JFieldRef fieldRef) {
        JNode parent = fieldRef.getParent();
        return (parent instanceof JAsgOperation) &&
            ((JAsgOperation) parent).getLhs() == fieldRef;
      }

      @Override
      public void endVisit(@Nonnull JFieldRef fieldRef) {
        JField field = fieldRef.getFieldId().getField();
        // It must be an assignment to the field
        if (field != null && isAssignment(fieldRef)) {
          JDefinedClassOrInterface enclosingType = field.getEnclosingType();
          // Only for fields of types to be emitted
          if (!field.isVolatile() && !field.isFinal() && enclosingType.isToEmit()) {
            boolean isAllowedReference =
                constructor != null &&  // we are inside a constructor of the same type
                    constructor.getEnclosingType() == enclosingType &&
                    constructor.isStatic() == field.isStatic(); // and the same 'static-ness'

            if (!isAllowedReference) {
              NotEffectivelyFinalField.markAsNotFinal(field);
            }
          }
        }
        super.endVisit(fieldRef);
      }
    }
  }

  /** Second phase: analyze constructors if needs to preserve JLS */
  @Description("Field finalizer, analyze constructors if needs to preserve JLS")
  @Constraint(need = ControlFlowGraph.class,
      no = JFieldInitializer.class)
  @Access(JDefinedClassOrInterface.class)
  @Transform(add = NotEffectivelyFinalField.class)
  public static class ConstructorsAnalysisPhase
      implements RunnableSchedulable<JMethod> {

    private final boolean enforceInitSemantic =
        ThreadConfig.get(Optimizations.FieldFinalizer.ENFORCE_INIT_SEMANTIC).booleanValue();

    private static class State {
      final BitSet maybeAssigned;
      final BitSet definitelyAssigned;

      private State(@Nonnegative int size) {
        maybeAssigned = new BitSet(size);
        definitelyAssigned = new BitSet(size);
      }

      @Override public final int hashCode() {
        throw new AssertionError();
      }

      @Override public final boolean equals(Object o) {
        return (o instanceof State) &&
            ((State) o).maybeAssigned.equals(this.maybeAssigned) &&
            ((State) o).definitelyAssigned.equals(this.definitelyAssigned);
      }
    }

    private static class Analyzer extends ReachabilityAnalyzer<State> {
      @Nonnull
      private JMethod constructor;
      @Nonnull
      private final Map<JField, Integer> field2id;

      Analyzer(@Nonnull List<JField> fields, @Nonnull JMethod constructor) {
        this.constructor = constructor;
        this.field2id = Maps.newHashMap();
        for (JField field : fields) {
          if (!field.isFinal() && !field.isVolatile() &&
              NotEffectivelyFinalField.checkIfCanBeFinal(field)) {
            this.field2id.put(field, Integer.valueOf(this.field2id.size()));
          }
        }
      }

      public boolean isEmpty() {
        return field2id.isEmpty();
      }

      @Nonnull
      @Override public ControlFlowGraph getCfg() {
        ControlFlowGraph cfg = constructor.getMarker(ControlFlowGraph.class);
        assert cfg != null;
        return cfg;
      }

      @Override public void finalize(
          @Nonnull List<State> in, @Nonnull List<State> out, @Nonnull List<State> outException) {

        // Recalculate 'in' set for exit block
        State exitBlockState = newState(false);
        recalculateInSet(
            getCfg().getExitNode(), /* ignoreExceptionPath: */ true,
            exitBlockState, out, outException);

        // Make sure the fields are definitely assigned in exit block
        for (Map.Entry<JField, Integer> entry : field2id.entrySet()) {
          JField field = entry.getKey();
          if (field.isStatic() == constructor.isStatic()) {
            if (!exitBlockState.definitelyAssigned.get(entry.getValue().intValue())) {
              NotEffectivelyFinalField.markAsNotFinal(field);
            }
          }
        }
      }

      @Nonnull
      @Override public State newState(boolean entry) {
        // NOTE: if a field has an initializer, it will already be inserted
        //       into the constructor in form of an assignment
        return new State(this.field2id.size());
      }

      @Override public void copyState(@Nonnull State src, @Nonnull State dest) {
        dest.maybeAssigned.clear();
        dest.maybeAssigned.or(src.maybeAssigned);
        dest.definitelyAssigned.clear();
        dest.definitelyAssigned.or(src.definitelyAssigned);
      }

      @Override public void mergeState(@Nonnull State state, @Nonnull State otherState) {
        state.maybeAssigned.or(otherState.maybeAssigned);
        state.definitelyAssigned.and(otherState.definitelyAssigned);
      }

      @Override public void processStatement(@Nonnull State outBs, @Nonnull JStatement stmt) {
        // This method is called to process the next statement ('stmt') in a basic
        // block with 'outBs' specifying pre-collected set of outs before this statement.
        // In our case the goal is to see if the field is being assigned in this
        // statement, and if it is, make sure this field was not assigned before.
        if (stmt instanceof JExpressionStatement) {
          JExpression expr = ((JExpressionStatement) stmt).getExpr();
          if (expr instanceof JAsgOperation) {
            JExpression lhs = ((JAsgOperation) expr).getLhs();
            if (lhs instanceof JFieldRef) {
              JField field = ((JFieldRef) lhs).getFieldId().getField();
              if (field == null) {
                return;
              }
              Integer index = this.field2id.get(field);
              if (index == null) {
                return;
              }

              int idx = index.intValue();
              if (outBs.maybeAssigned.get(idx)) {
                // The field is not definitely unassigned
                NotEffectivelyFinalField.markAsNotFinal(field);
              }

              outBs.maybeAssigned.set(idx);
              outBs.definitelyAssigned.set(idx);
            }

          } else if (expr instanceof JMethodCall) {
            // If an instance constructor delegates to another constructor,
            // we consider all the instance effectively final fields initialized.
            if (constructor.isStatic()) {
              return;
            }

            JMethodCall call = (JMethodCall) expr;
            // Must be a constructor call on 'this'
            if (!call.getMethodId().isInit() ||
                !(call.getInstance() instanceof JThisRef)) {
              return;
            }

            assert call.getDispatchKind() == JMethodCall.DispatchKind.DIRECT;
            assert constructor.getMethodIdWide().getKind() == MethodKind.INSTANCE_NON_VIRTUAL;

            // Well, we assume that if the type of the receiver is not the type of the
            // constructor we are analyzing, this must be a call to a super constructor
            if (call.getReceiverType() != constructor.getEnclosingType()) {
              return;
            }

            for (Map.Entry<JField, Integer> e : field2id.entrySet()) {
              JField field = e.getKey();
              if (field.isStatic()) {
                continue;
              }

              int index = e.getValue().intValue();
              if (outBs.maybeAssigned.get(index)) {
                // An instance field assigned at this point is going to
                // be reassigned inside the constructor during delegation
                NotEffectivelyFinalField.markAsNotFinal(field);
              }

              // It is definitely assigned after the call
              outBs.maybeAssigned.set(index);
              outBs.definitelyAssigned.set(index);
            }
          }
        }
      }

      @Nonnull
      @Override public State cloneState(@Nonnull State state) {
        State clone = new State(this.field2id.size());
        clone.maybeAssigned.or(state.maybeAssigned);
        clone.definitelyAssigned.or(state.definitelyAssigned);
        return clone;
      }
    }

    @Override
    public void run(@Nonnull JMethod method) {
      if (!enforceInitSemantic || !isConstructor(method)) {
        return; // We only analyze constructors and only if preserve JLS is true
      }

      JDefinedClassOrInterface type = method.getEnclosingType();
      if (!(type instanceof JDefinedClass)) {
        return; // Only fields of classes are finalized
      }

      Analyzer analyzer = new Analyzer(type.getFields(), method);
      if (!analyzer.isEmpty()) {
        // According to JLS each constructor must definitely assign the value field
        // exactly once, in case the constructor delegates to another constructor
        // of the same type, there must be no assignments to the field since it
        // must be assigned inside the other constructor.

        // The analyzer will mark non-JLS compliant fields with
        // NotEffectivelyFinalField
        analyzer.analyze();
      }
    }
  }

  /** Third phase: mark fields effectively final */
  @Description("Field finalizer, finalizing phase")
  @Constraint(need = NotEffectivelyFinalField.class)
  @Transform(remove = NotEffectivelyFinalField.class,
      add = EffectivelyFinalFieldMarker.class)
  public static class FinalizingPhase
      implements RunnableSchedulable<JField> {

    private final boolean addFinalModifier =
        ThreadConfig.get(Optimizations.FieldFinalizer.ADD_FINAL_MODIFIER).booleanValue();

    @Nonnull
    private final JAnnotationType disablingAnnotationType =
        Jack.getSession().getPhantomLookup().getAnnotationType(
            NamingTools.getTypeSignatureName(DisableFieldFinalizerOptimization.class.getName()));
    @Nonnull
    private final Tracer tracer = TracerFactory.getTracer();

    @Override
    public void run(@Nonnull JField field) {
      if (field.isFinal() || field.isVolatile() ||
          !NotEffectivelyFinalField.checkIfCanBeFinalAndRemoveMarker(field) ||
          !(field.getEnclosingType() instanceof JDefinedClass)) {

        // Non-final, non-volatile fields of defined classes are finalized
        // if they are not marked with NotEffectivelyFinalField
        return;
      }

      EffectivelyFinalFieldMarker.markAsEffectivelyFinal(field);
      if (addFinalModifier &&
          field.getAnnotations(disablingAnnotationType).isEmpty() &&
          field.getEnclosingType().getAnnotations(disablingAnnotationType).isEmpty()) {
        field.setFinal();
        tracer.getStatistic(FIELDS_FINALIZED).incValue();
      }
    }
  }

  private static boolean isConstructor(@Nonnull JMethod method) {
    return method instanceof JConstructor || JMethod.isClinit(method);
  }
}
