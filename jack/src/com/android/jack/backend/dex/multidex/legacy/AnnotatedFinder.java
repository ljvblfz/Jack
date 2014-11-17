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

package com.android.jack.backend.dex.multidex.legacy;

import com.android.jack.backend.dex.MainDexMarker;
import com.android.jack.backend.dex.MultiDexLegacy;
import com.android.jack.ir.ast.Annotable;
import com.android.jack.ir.ast.JAnnotation;
import com.android.jack.ir.ast.JDefinedAnnotation;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JRetentionPolicy;
import com.android.sched.item.Description;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.schedulable.Transform;

import javax.annotation.Nonnull;

/**
 * Mark classes as main dex if they are annotated by runtime visible annotation or if one of their
 * member is.
 * See {@link RuntimeAnnotationFinder} for details.
 */
@Description("Mark classes annotated by runtime annotation as main dex")
@Transform(add = MainDexMarker.class)
@Support(MultiDexLegacy.class)
public class AnnotatedFinder implements RunnableSchedulable<JDefinedClassOrInterface> {

  @Override
  public void run(@Nonnull JDefinedClassOrInterface type) throws Exception {
    // Ignore external types
    if (type.isExternal()) {
      return;
    }

    if (hasRuntimeAnnotation(type)) {
      type.addMarker(MainDexMarker.INSTANCE);
    }
  }

  private boolean hasRuntimeAnnotation(@Nonnull JDefinedClassOrInterface type) {
    if (isAnnotatedByRuntimeAnnotation(type)) {
      return true;
    }
    for (JField field : type.getFields()) {
      if (isAnnotatedByRuntimeAnnotation(field)) {
        return true;
      }
    }
    for (JMethod method : type.getMethods()) {
      if (isAnnotatedByRuntimeAnnotation(method)) {
        return true;
      }
      for (JParameter param : method.getParams()) {
        if (isAnnotatedByRuntimeAnnotation(param)) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean isAnnotatedByRuntimeAnnotation(@Nonnull Annotable annotable) {
    for (JAnnotation annotation: annotable.getAnnotationTypes()) {
      if (annotation instanceof JDefinedAnnotation
          && (((JDefinedAnnotation) annotation).getRetentionPolicy() == JRetentionPolicy.RUNTIME)) {
        return true;
      }
    }
    return false;
  }

}
