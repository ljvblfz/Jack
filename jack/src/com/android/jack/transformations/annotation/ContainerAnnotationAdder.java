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
import com.android.jack.ir.ast.JArrayLiteral;
import com.android.jack.ir.ast.JClassLiteral;
import com.android.jack.ir.ast.JDefinedAnnotationType;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JLiteral;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodId;
import com.android.jack.ir.ast.JNameValuePair;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JRetentionPolicy;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.transformations.request.AddAnnotation;
import com.android.jack.transformations.request.Remove;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.schedulable.Use;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Add a container annotation when same annotations are used several times on an annotable.
 */
@Transform(add = {JAnnotation.class, JNameValuePair.class, JArrayLiteral.class})
public class ContainerAnnotationAdder {

  /**
   * Add a container annotation when same annotations are used several times on a type.
   */
  @Description("Add a container annotation when same annotations are used several times on a type")
  @Name("TypeContainerAnnotationAdder")
  @Constraint(need = JAnnotation.RepeatedAnnotationOnType.class)
  @Transform(remove = {JAnnotation.RepeatedAnnotationOnType.class})
  @Use(ContainerAnnotationAdder.class)
  public static class TypeContainerAnnotationAdder extends ContainerAnnotationAdder
      implements RunnableSchedulable<JDefinedClassOrInterface> {
    @Override
    public void run(JDefinedClassOrInterface type) throws Exception {
      TransformationRequest tr = new TransformationRequest(type);

      addContainerAnnotationIfNeeded(tr, type);

      tr.commit();
    }
  }

  /**
   * Add a container annotation when same annotations are used several times on a field.
   */
  @Description("Add a container annotation when same annotations are used several times "
      + "on a field")
  @Name("FieldContainerAnnotationAdder")
  @Constraint(need = JAnnotation.RepeatedAnnotationOnField.class)
  @Transform(remove = {JAnnotation.RepeatedAnnotationOnField.class})
  @Use(ContainerAnnotationAdder.class)
  public static class FieldContainerAnnotationAdder extends ContainerAnnotationAdder
      implements RunnableSchedulable<JField> {
    @Override
    public void run(JField type) throws Exception {
      TransformationRequest tr = new TransformationRequest(type);

      addContainerAnnotationIfNeeded(tr, type);

      tr.commit();
    }
  }

  /**
   * Add a container annotation when same annotations are used several times on a method or on
   * method parameters.
   */
  @Description("Add a container annotation when same annotations are used several times on a"
      + " method or on method parameters")
  @Name("MethodContainerAnnotationAdder")
  @Constraint(need = JAnnotation.RepeatedAnnotationOnMethod.class)
  @Transform(remove = {JAnnotation.RepeatedAnnotationOnMethod.class})
  @Use(ContainerAnnotationAdder.class)
  public static class MethodContainerAnnotationAdder extends ContainerAnnotationAdder
      implements RunnableSchedulable<JMethod> {
    @Override
    public void run(JMethod jMethod) throws Exception {
      TransformationRequest tr = new TransformationRequest(jMethod);

      addContainerAnnotationIfNeeded(tr, jMethod);

      for (JParameter parameter : jMethod.getParams()) {
        addContainerAnnotationIfNeeded(tr, parameter);
      }

      tr.commit();
    }
  }

  @Nonnull
  private final JAnnotationType repeatableAnnotationType =
      Jack.getSession().getPhantomLookup().getAnnotationType("Ljava/lang/annotation/Repeatable;");

  protected void addContainerAnnotationIfNeeded(@Nonnull TransformationRequest tr,
      @Nonnull Annotable annotable) {
    for (JAnnotationType annotationType : annotable.getAnnotationTypes()) {
      Collection<JAnnotation> annotationsOfSameType = annotable.getAnnotations(annotationType);

      if (annotationsOfSameType.size() > 1) {
        assert annotationType instanceof JDefinedAnnotationType;

        List<JAnnotation> repeatableAnnotation =
            ((JDefinedAnnotationType) annotationType).getAnnotations(repeatableAnnotationType);
        assert repeatableAnnotation.size() == 1;

        JNameValuePair jnvp = repeatableAnnotation.get(0).getNameValuePair("value");
        assert jnvp != null;

        JClassLiteral containerLiteral = (JClassLiteral) jnvp.getValue();
        JAnnotationType containerAnnotationType = (JAnnotationType) containerLiteral.getRefType();
        assert containerAnnotationType instanceof JDefinedAnnotationType;

        JRetentionPolicy retentionPolicy =
            ((JDefinedAnnotationType) containerAnnotationType).getRetentionPolicy();

        JAnnotation containerAnnotation =
            new JAnnotation(SourceInfo.UNKNOWN, retentionPolicy, containerAnnotationType);

        JMethodId methodId = containerAnnotationType.getMethodId("value",
            Collections.<JType>emptyList(), MethodKind.INSTANCE_VIRTUAL);
        containerAnnotation.add(new JNameValuePair(SourceInfo.UNKNOWN, methodId,
            new JArrayLiteral(SourceInfo.UNKNOWN, new ArrayList<JLiteral>(annotationsOfSameType))));

        for (JAnnotation annotation : annotationsOfSameType) {
          // Remove old annotations before to add the new one since we move existing annotations
          // to the new annotation.
          tr.append(new Remove(annotation));
        }

        tr.append(new AddAnnotation(containerAnnotation, annotable));
      }
    }
  }
}
