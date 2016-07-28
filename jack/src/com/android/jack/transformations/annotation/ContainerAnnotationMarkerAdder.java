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

package com.android.jack.transformations.annotation;

import com.android.jack.Jack;
import com.android.jack.ir.ast.Annotable;
import com.android.jack.ir.ast.JAnnotation;
import com.android.jack.ir.ast.JAnnotationType;
import com.android.jack.ir.ast.JClassLiteral;
import com.android.jack.ir.ast.JDefinedAnnotationType;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JNameValuePair;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JSession;
import com.android.jack.scheduling.filter.SourceTypeFilter;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Access;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.schedulable.Use;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Add a {@link ContainerAnnotationMarker} on {@link JAnnotation} when {@link JAnnotation} with the
 * same {@link JAnnotationType} are used on the same {@link Annotable}.
 */
@Transform(add = {ContainerAnnotationMarker.class})
// Reads retention policy of not visited ContainerAnnotationType and adds marker on JAnnotationType
// while visiting annotated nodes.
@Access(JSession.class)
public class ContainerAnnotationMarkerAdder {

  /**
   * Add a {@link ContainerAnnotationMarker} on {@link JAnnotation} when {@link JAnnotation} with
   * the same {@link JAnnotationType} are used on the same {@link JDefinedClassOrInterface}.
   */
  @Description("Add a container annotation marker when annotations with the same annotations type "
      + "are on the same type")
  @Constraint(need = JAnnotation.RepeatedAnnotationOnType.class)
  @Use(ContainerAnnotationMarkerAdder.class)
  @Access(JSession.class)
  @Filter(SourceTypeFilter.class)
  public static class TypeContainerAnnotationMarkerAdder extends ContainerAnnotationMarkerAdder
      implements RunnableSchedulable<JDefinedClassOrInterface> {
    @Override
    public void run(@Nonnull JDefinedClassOrInterface type) {
      addContainerAnnotationIfNeeded(type);
    }
  }

  /**
   * Add a {@link ContainerAnnotationMarker} on {@link JAnnotation} when {@link JAnnotation} with
   * the same {@link JAnnotationType} are used on the same {@link JField}.
   */
  @Description("Add a container annotation marker when annotations with the same annotations type "
      + "are on the same field")
  @Constraint(need = JAnnotation.RepeatedAnnotationOnField.class)
  @Use(ContainerAnnotationMarkerAdder.class)
  @Access(JSession.class)
  @Filter(SourceTypeFilter.class)
  public static class FieldContainerAnnotationMarkerAdder extends ContainerAnnotationMarkerAdder
      implements RunnableSchedulable<JField> {
    @Override
    public void run(@Nonnull JField type) {
      addContainerAnnotationIfNeeded(type);
    }
  }

  /**
   * Add a {@link ContainerAnnotationMarker} on {@link JAnnotation} when {@link JAnnotation} with
   * the same {@link JAnnotationType} are used on the same {@link JMethod} or {@link JParameter}.
   */
  @Description("Add a container annotation marker when annotations with the same annotations type "
      + "are on the same method or parameter")
  @Constraint(need = JAnnotation.RepeatedAnnotationOnMethod.class)
  @Use(ContainerAnnotationMarkerAdder.class)
  @Access(JSession.class)
  @Filter(SourceTypeFilter.class)
  public static class MethodContainerAnnotationMarkerAdder extends ContainerAnnotationMarkerAdder
      implements RunnableSchedulable<JMethod> {
    @Override
    public void run(@Nonnull JMethod jMethod) {
      addContainerAnnotationIfNeeded(jMethod);

      for (JParameter parameter : jMethod.getParams()) {
        addContainerAnnotationIfNeeded(parameter);
      }
    }
  }

  @Nonnull
  private final JAnnotationType repeatableAnnotationType =
      Jack.getSession().getPhantomLookup().getAnnotationType("Ljava/lang/annotation/Repeatable;");

  protected void addContainerAnnotationIfNeeded(@Nonnull Annotable annotable) {
    for (JAnnotationType annotationType : annotable.getAnnotationTypes()) {
      Collection<JAnnotation> annotationsOfSameType = annotable.getAnnotations(annotationType);

      if (annotationsOfSameType.size() > 1) {
        List<JAnnotation> repeatableAnnotation =
            ((JDefinedAnnotationType) annotationType).getAnnotations(repeatableAnnotationType);
        assert repeatableAnnotation.size() == 1;

        JNameValuePair jnvp = repeatableAnnotation.get(0).getNameValuePair("value");
        assert jnvp != null;

        JClassLiteral containerLiteral = (JClassLiteral) jnvp.getValue();
        JAnnotationType containerAnnotationType = (JAnnotationType) containerLiteral.getRefType();

        ContainerAnnotationMarker cam = new ContainerAnnotationMarker(containerAnnotationType,
            ((JDefinedAnnotationType) containerAnnotationType).getRetentionPolicy());

        for (JAnnotation annotation : annotationsOfSameType) {
          annotation.addMarker(cam);
        }
      }
    }
  }
}
