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

package com.android.jack.transformations.ast.removeinit;

import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.lookup.JLookupException;
import com.android.jack.scheduling.filter.SourceTypeFilter;
import com.android.jack.shrob.obfuscation.OriginalNames;
import com.android.jack.transformations.request.Remove;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;

import javax.annotation.Nonnull;

/**
 * Remove method initializing field values.
 */
@Description("Remove method initializing field values.")
@Name("FieldInitMethodRemover")
@Constraint(need = {FieldInitMethod.class, OriginalNames.class}, no = FieldInitMethodCall.class)
@Transform(modify = JDefinedClass.class, remove = FieldInitMethod.class)
@Filter(SourceTypeFilter.class)
public class FieldInitMethodRemover implements RunnableSchedulable<JDefinedClassOrInterface> {

  @Nonnull
  static final String VAR_INIT_METHOD_NAME = "$init";

  @Override
  public void run(@Nonnull JDefinedClassOrInterface declaredType) {
    if (declaredType instanceof JDefinedClass) {
      try {
        JMethod varInitMethod =
            declaredType.getMethod(VAR_INIT_METHOD_NAME, JPrimitiveTypeEnum.VOID.getType());
        TransformationRequest tr = new TransformationRequest(declaredType);
        tr.append(new Remove(varInitMethod));
        tr.commit();
      } catch (JLookupException e) {
        // Nothing to do.
      }
    }
  }
}
