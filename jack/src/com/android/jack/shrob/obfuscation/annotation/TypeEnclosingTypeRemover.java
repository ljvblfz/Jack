/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.jack.shrob.obfuscation.annotation;

import com.android.jack.backend.dex.annotations.tag.ReflectAnnotations;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JSession;
import com.android.jack.transformations.request.RemoveEnclosingType;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.ExclusiveAccess;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.schedulable.Transform;

import javax.annotation.Nonnull;

/**
 * A {@link RunnableSchedulable} that removes enclosing types from types.
 */
@Description("Removes enclosing types from types")
@Constraint(no = ReflectAnnotations.class)
@Transform(modify = JDefinedClass.class)
@Support(com.android.jack.shrob.obfuscation.annotation.RemoveEnclosingType.class)
// Modify member types of enclosing type.
@ExclusiveAccess(JSession.class)
public class TypeEnclosingTypeRemover implements RunnableSchedulable<JDefinedClassOrInterface> {

  @Override
  public void run(@Nonnull JDefinedClassOrInterface type) throws Exception {
    TransformationRequest tr = new TransformationRequest(type.getEnclosingPackage());
    tr.append(new RemoveEnclosingType(type));
    tr.commit();
  }

}
