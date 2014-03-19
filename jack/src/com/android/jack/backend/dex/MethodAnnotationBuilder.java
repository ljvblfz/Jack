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

package com.android.jack.backend.dex;

import com.android.jack.Options;
import com.android.jack.backend.dex.rop.RopHelper;
import com.android.jack.dx.dex.file.ClassDefItem;
import com.android.jack.dx.rop.annotation.Annotations;
import com.android.jack.dx.rop.annotation.AnnotationsList;
import com.android.jack.ir.ast.JAnnotationLiteral;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.scheduling.marker.ClassDefItemMarker;
import com.android.jack.scheduling.marker.DexFileMarker;
import com.android.jack.util.filter.Filter;
import com.android.sched.item.Description;
import com.android.sched.item.Synchronized;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.schedulable.Use;
import com.android.sched.util.config.ThreadConfig;

import java.util.Collection;

import javax.annotation.Nonnull;

/**
 * Builds the {@link com.android.dx.rop.annotation.Annotations Annotations} of a method.
 */
@Description("Builds the rop annotations of a method")
@Synchronized
@Constraint(need = {ClassDefItemMarker.class, DexFileMarker.class, DexFileMarker.Method.class})
@Transform(add = DexFileMarker.MethodAnnotation.class,
    modify = {ClassDefItemMarker.class, DexFileMarker.class})
@Use(AnnotationBuilder.class)
public class MethodAnnotationBuilder implements RunnableSchedulable<JMethod> {

  @Nonnull
  private final Filter<JMethod> filter = ThreadConfig.get(Options.METHOD_FILTER);

  @Override
  public synchronized void run(@Nonnull JMethod method) throws Exception {
    JDefinedClassOrInterface declaringClass = method.getEnclosingType();
    // Ignore method declared by external type
    if (declaringClass.isExternal() || !filter.accept(this.getClass(), method)) {
      return;
    }

    Collection<JAnnotationLiteral> literals = method.getAnnotations();
    if (!literals.isEmpty()) {
      Annotations annotations = new AnnotationBuilder().createAnnotations(literals);
      if (annotations.size() > 0) {
        ClassDefItemMarker classDefItemMarker = declaringClass.getMarker(ClassDefItemMarker.class);
        assert classDefItemMarker != null;

        ClassDefItem classDefItem = classDefItemMarker.getClassDefItem();
        classDefItem.addMethodAnnotations(RopHelper.createMethodRef(method), annotations);
      }
    }

    AnnotationsList annotationsList = new AnnotationsList(method.getParams().size());
    int annotationIndex = 0;
    boolean hasParamAnnotations = false;
    for (JParameter param : method.getParams()) {
      Collection<JAnnotationLiteral> paramAnnotations = param.getAnnotations();
      hasParamAnnotations |= paramAnnotations.size() > 0;
      Annotations annotation = new AnnotationBuilder().createAnnotations(paramAnnotations);
      annotationsList.set(annotationIndex++, annotation);
    }

    if (hasParamAnnotations) {
      assert annotationsList.size() > 0;
      ClassDefItemMarker classDefItemMarker = declaringClass.getMarker(ClassDefItemMarker.class);
      assert classDefItemMarker != null;

      ClassDefItem classDefItem = classDefItemMarker.getClassDefItem();
      classDefItem.addParameterAnnotations(RopHelper.createMethodRef(method), annotationsList);
    }
  }
}
