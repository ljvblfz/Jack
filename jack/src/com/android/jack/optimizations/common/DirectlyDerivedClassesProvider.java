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

package com.android.jack.optimizations.common;

import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;

import javax.annotation.Nonnull;

/** Provides DirectlyDerivedClassesMarker on defined classes */
@Description("Provides DirectlyDerivedClassesMarker on defined classes")
@Constraint(need = TypeToBeEmittedMarker.class)
@Transform(add = DirectlyDerivedClassesMarker.class)
public class DirectlyDerivedClassesProvider
    implements RunnableSchedulable<JDefinedClassOrInterface> {

  @Override
  public void run(@Nonnull JDefinedClassOrInterface type) throws Exception {
    if (type instanceof JDefinedClass) {
      JDefinedClass thisClass = (JDefinedClass) type;
      JClass superClass = thisClass.getSuperClass();
      if (superClass instanceof JDefinedClass) {
        if (TypeToBeEmittedMarker.isToBeEmitted(superClass)) {
          DirectlyDerivedClassesMarker.
              markDirectlyDerivedClass((JDefinedClass) superClass, thisClass);
        }
      }
    }
  }
}
