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
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JFieldRef;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JPrimitiveType;
import com.android.jack.ir.ast.JThisRef;
import com.android.jack.ir.ast.cfg.BasicBlockLiveProcessor;
import com.android.jack.ir.ast.cfg.JControlFlowGraph;
import com.android.jack.ir.ast.cfg.JSimpleBasicBlock;
import com.android.jack.ir.ast.cfg.JStoreBlockElement;
import com.android.jack.ir.ast.cfg.JThrowingExpressionBasicBlock;
import com.android.jack.ir.ast.cfg.mutations.CfgFragment;
import com.android.jack.ir.ast.cfg.mutations.ExceptionCatchBlocks;
import com.android.jack.lookup.JPhantomLookup;
import com.android.jack.optimizations.Optimizations;
import com.android.jack.optimizations.cfg.CfgJlsNullabilityChecker;
import com.android.jack.transformations.LocalVarCreator;
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

import javax.annotation.Nonnull;

/** Write-only field removal, second phase: remove field assignments */
@Description("Write-only field removal, field writes removal")
@Constraint(need = { FieldReadWriteCountsMarker.class, JFieldRef.class })
@Transform(modify = { FieldReadWriteCountsMarker.class, JControlFlowGraph.class })
@Name("WriteOnlyFieldRemoval: RemoveFieldWrites")
@Use({ CfgJlsNullabilityChecker.class, LocalVarCreator.class })
public class WofrRemoveFieldWrites extends WofrSchedulable
    implements RunnableSchedulable<JControlFlowGraph> {

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
    /** Field write can be removed */
    Remove,
    /** Field write can be removed, but a null-check need to be added */
    RemoveAndAddNullCheck
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
      return Action.None; // The field has reads
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
      return Action.Remove;
    }

    assert !field.isStatic();
    return preserveNullChecks ? Action.RemoveAndAddNullCheck : Action.Remove;
  }

  @Override
  public void run(@Nonnull final JControlFlowGraph cfg) {
    final TransformationRequest request = new TransformationRequest(cfg);
    final CfgJlsNullabilityChecker nullChecker =
        new CfgJlsNullabilityChecker(cfg,
            new LocalVarCreator(cfg.getMethod(), "wofr"), phantomLookup);

    // Create a processor to walk the cfg and remove the writes, note that
    // even though we insert the new basic blocks, we don't need to update
    // the processor to know about these newly created blocks, since they
    // don't have any field writes we might want to process.

    new BasicBlockLiveProcessor(/* stepIntoElements = */ true) {
      @Override
      public boolean visit(@Nonnull JStoreBlockElement element) {
        JFieldRef ref = element.getLhsAsFieldRef();
        if (ref == null) {
          return false;
        }
        JExpression value = element.getValueExpression();
        assert !value.canThrow();

        Action action = classify(cfg.getMethod(), ref);
        if (action == Action.None) {
          // Cannot remove field assignment
          return false;
        }

        // Field store operation must be the LAST element of
        // the throwing expression basic block.
        JThrowingExpressionBasicBlock block = element.getBasicBlock();
        assert block.getLastElement() == element;

        // We first split the basic block into two parts:
        //
        //        block { e0, e1, ...,  eLast (== element) }
        //                      |  |  |
        //                      V  V  V
        //    simple { e0, e1, ..., goto }  -->  block { eLast }
        //
        JSimpleBasicBlock simple = block.split(-1);

        if (action == Action.RemoveAndAddNullCheck) {
          // The null-check is needed, we insert it in between the two blocks
          JExpression instance = ref.getInstance();
          assert instance != null;
          assert !instance.canThrow();

          CfgFragment nullCheckFragment =
              nullChecker.createNullCheck(
                  ExceptionCatchBlocks.fromThrowingBlock(block), instance, request);

          nullCheckFragment.insert(simple, block);
        }

        // Delete the block, since we remove the write
        assert block.getElementCount() == 1;
        assert block.getLastElement() == element;
        block.delete();

        // Unmark the write
        JField field = ref.getFieldId().getField();
        assert field != null;
        FieldReadWriteCountsMarker.unmarkWrite(field);
        tracer.getStatistic(FIELD_WRITES_REMOVED).incValue();

        return false;
      }

      @Nonnull
      @Override
      public JControlFlowGraph getCfg() {
        return cfg;
      }
    }.process();

    request.commit();
  }
}
