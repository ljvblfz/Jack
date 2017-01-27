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
import com.android.jack.backend.dex.annotations.tag.ParameterMetadataAnnotation;
import com.android.jack.backend.dex.annotations.tag.ParameterMetadataFeature;
import com.android.jack.backend.dex.rop.RopHelper;
import com.android.jack.dx.dex.file.ClassDefItem;
import com.android.jack.dx.rop.annotation.Annotations;
import com.android.jack.dx.rop.annotation.AnnotationsList;
import com.android.jack.ir.ast.JAnnotation;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.scheduling.feature.SourceVersion8;
import com.android.jack.scheduling.filter.TypeWithoutPrebuiltFilter;
import com.android.jack.scheduling.marker.ClassDefItemMarker;
import com.android.sched.item.Description;
import com.android.sched.item.Synchronized;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.Optional;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.ToSupport;
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
@Constraint(need = {ClassDefItemMarker.class, ClassDefItemMarker.Method.class})
@Transform(add = ClassDefItemMarker.MethodAnnotation.class, modify = ClassDefItemMarker.class)
@Use(AnnotationBuilder.class)
@Optional({@ToSupport(feature = SourceVersion8.class,
               add = @Constraint(no = JAnnotation.RepeatedAnnotation.class)),
           @ToSupport(feature = ParameterMetadataFeature.class,
               add = @Constraint(need = ParameterMetadataAnnotation.class))})
@Filter(TypeWithoutPrebuiltFilter.class)
public class MethodAnnotationBuilder implements RunnableSchedulable<JMethod> {

  @Nonnull
  private final com.android.jack.util.filter.Filter<JMethod> filter =
      ThreadConfig.get(Options.METHOD_FILTER);

  @Override
  public synchronized void run(@Nonnull JMethod method) {
    JDefinedClassOrInterface declaringClass = method.getEnclosingType();
    if (!filter.accept(this.getClass(), method)) {
      return;
    }

    Collection<JAnnotation> annotations = method.getAnnotations();
    if (!annotations.isEmpty()) {
      Annotations ropAnnotations = new AnnotationBuilder().createAnnotations(annotations);
      if (ropAnnotations.size() > 0) {
        ClassDefItemMarker classDefItemMarker = declaringClass.getMarker(ClassDefItemMarker.class);
        assert classDefItemMarker != null;

        ClassDefItem classDefItem = classDefItemMarker.getClassDefItem();
        classDefItem.addMethodAnnotations(RopHelper.createMethodRef(method), ropAnnotations);
      }
    }

    AnnotationsList annotationsList = new AnnotationsList(method.getParams().size());
    int annotationIndex = 0;
    boolean hasParamAnnotations = false;
    for (JParameter param : method.getParams()) {
      Collection<JAnnotation> paramAnnotations = param.getAnnotations();
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
