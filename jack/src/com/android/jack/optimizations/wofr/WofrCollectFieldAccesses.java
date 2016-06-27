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

import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JFieldInitializer;
import com.android.jack.ir.ast.JFieldRef;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.optimizations.common.OptimizerUtils;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;

import javax.annotation.Nonnull;

/**
 * Write-only field removal, first phase: analyze all the methods and constructors
 * and collect number of reads and writes to the field.
 */
@Description("Write-only field removal, field usage collection")
@Constraint(need = JFieldRef.class,
    no = JFieldInitializer.class)
@Transform(add = FieldReadWriteCountsMarker.class)
@Name("WriteOnlyFieldRemoval: CollectFieldAccesses")
public class WofrCollectFieldAccesses extends WofrSchedulable
    implements RunnableSchedulable<JMethod> {

  /** A singleton instance of method analyzer */
  @Nonnull
  private static final JVisitor ANALYZER = new JVisitor() {
    @Override
    public void endVisit(@Nonnull JFieldRef ref) {
      JField field = ref.getFieldId().getField();
      if (field != null && field.getEnclosingType().isToEmit()) {
        if (OptimizerUtils.isAssigned(ref)) {
          boolean isNonLiteral =
              OptimizerUtils.asLiteralOrDefault(
                  OptimizerUtils.getAssignedValue(ref), null) == null;
          FieldReadWriteCountsMarker.markWrite(field, isNonLiteral);
        } else {
          FieldReadWriteCountsMarker.markRead(field);
        }
      }
      super.endVisit(ref);
    }
  };

  @Override
  public void run(@Nonnull JMethod method) {
    if (!method.isAbstract() && !method.isNative()) {
      ANALYZER.accept(method);
    }
  }
}
