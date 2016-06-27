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

import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JField;
import com.android.jack.transformations.request.Remove;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.item.Synchronized;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.ExclusiveAccess;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;

import javax.annotation.Nonnull;

/** Write-only field removal, third phase: remove fields */
@Description("Write-only field removal, fields removal")
@Constraint(need = FieldReadWriteCountsMarker.class)
@Transform(modify = JField.class,
    remove = FieldReadWriteCountsMarker.class)
@ExclusiveAccess(JDefinedClassOrInterface.class)
@Synchronized
@Name("WriteOnlyFieldRemoval: RemoveFields")
public class WofrRemoveFields extends WofrSchedulable
    implements RunnableSchedulable<JField> {

  @Nonnull
  private final Tracer tracer = TracerFactory.getTracer();

  @Override
  public synchronized void run(@Nonnull final JField field) {
    if (!preserveReflections &&
        !FieldReadWriteCountsMarker.hasReads(field) &&
        !FieldReadWriteCountsMarker.hasWrites(field)) {

      TransformationRequest request = new TransformationRequest(field);
      request.append(new Remove(field));
      request.commit();
      tracer.getStatistic(FIELDS_REMOVED).incValue();
    }
    field.removeMarker(FieldReadWriteCountsMarker.class);
  }
}
