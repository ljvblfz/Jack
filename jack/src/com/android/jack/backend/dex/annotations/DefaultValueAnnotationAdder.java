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

package com.android.jack.backend.dex.annotations;

import com.android.jack.Jack;
import com.android.jack.Options;
import com.android.jack.backend.dex.DexAnnotations;
import com.android.jack.ir.ast.JAnnotation;
import com.android.jack.ir.ast.JAnnotationMethod;
import com.android.jack.ir.ast.JAnnotationType;
import com.android.jack.ir.ast.JDefinedAnnotationType;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JLiteral;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodIdWide;
import com.android.jack.ir.ast.JNameValuePair;
import com.android.jack.ir.ast.JRetentionPolicy;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.scheduling.feature.SourceVersion8;
import com.android.jack.scheduling.filter.TypeWithoutPrebuiltFilter;
import com.android.jack.transformations.request.AddAnnotation;
import com.android.jack.transformations.request.AddNameValuePair;
import com.android.jack.transformations.request.Remove;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.sched.item.Description;
import com.android.sched.item.Synchronized;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.Optional;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.ToSupport;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.ThreadConfig;

import java.util.Collections;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Add default values of annotation methods as system annotation.
 *
 * Remove default values of methods and add the system annotation
 * {@code dalvik.annotation.AnnotationDefault} on the class with its {@code value} set to an
 * annotation instance of the current type. The current annotation instance has its methods values
 * <li> set to their default value if available,
 * <li> absent otherwise.<br>
 * For example the annotation: <br>
 * <pre>
 * public @interface Annotation1 {
 *  int v1;
 *  int v2 = 2;
 * }
 * </pre>
 * will be transformed to: <br>
 * <pre>
 * {@literal @}dalvik.annotation.AnnotationDefault(value = {@literal @}Annotation1(v2 = 2))
 * public @interface Annotation1 {
 *  int v1;
 *  int v2;
 * }
 * </pre>
 */
@Description("Add annotation methods default values as system annotation.")
@Synchronized
@Transform(remove = AnnotationMethodDefaultValue.class,
    add = {JAnnotation.class, JNameValuePair.class})
@Optional(@ToSupport(feature = SourceVersion8.class,
    add = @Constraint(no = JAnnotation.RepeatedAnnotation.class)))
@Filter(TypeWithoutPrebuiltFilter.class)
public class DefaultValueAnnotationAdder implements RunnableSchedulable<JMethod> {

  @Nonnull
  private final com.android.jack.util.filter.Filter<JMethod> filter =
      ThreadConfig.get(Options.METHOD_FILTER);

  @CheckForNull
  private JAnnotationType defaultAnnotationType;

  @Override
  public synchronized void run(@Nonnull JMethod method) throws Exception {
    final JDefinedClassOrInterface enclosingType = method.getEnclosingType();

    if (!filter.accept(this.getClass(), method)) {
      return;
    }

    if (method instanceof JAnnotationMethod) {
      JAnnotationMethod annotationMethod = (JAnnotationMethod) method;
      JLiteral defaultValue = annotationMethod.getDefaultValue();
      if (defaultValue != null) {
        TransformationRequest tr = new TransformationRequest(enclosingType);
        tr.append(new Remove(defaultValue));
        SourceInfo sourceInfo = defaultValue.getSourceInfo();
        JAnnotation defaultAnnotation =
            getDefaultAnnotation((JDefinedAnnotationType) enclosingType, tr);
        tr.append(new AddNameValuePair(defaultAnnotation,
            new JNameValuePair(sourceInfo, method.getMethodIdWide(), defaultValue)));
        tr.commit();
      }
    }
  }

  @Nonnull
  private JAnnotation getDefaultAnnotation(@Nonnull JDefinedAnnotationType targetAnnotationType,
      @Nonnull TransformationRequest tr) {
    JAnnotationType defaultAnnotationType = getDefaultAnnotationType(targetAnnotationType);
    JAnnotation defaultAnnotation = null;
    List<JAnnotation> defaultAnnotations =
        targetAnnotationType.getAnnotations(defaultAnnotationType);
    if (defaultAnnotations.isEmpty()) {
      defaultAnnotation = new JAnnotation(SourceInfo.UNKNOWN, JRetentionPolicy.SYSTEM,
          defaultAnnotationType);
      JMethodIdWide methodId = defaultAnnotationType.getOrCreateMethodIdWide("value",
          Collections.<JType>emptyList(), MethodKind.INSTANCE_VIRTUAL);
      defaultAnnotation.add(new JNameValuePair(SourceInfo.UNKNOWN, methodId,
 new JAnnotation(
          SourceInfo.UNKNOWN, targetAnnotationType.getRetentionPolicy(), targetAnnotationType)));
      tr.append(new AddAnnotation(defaultAnnotation, targetAnnotationType));
    } else {
      assert defaultAnnotations.size() == 1;
      defaultAnnotation = defaultAnnotations.get(0);
    }
    return (JAnnotation) defaultAnnotation.getNameValuePairs().iterator().next().getValue();
  }

  /**
   * Get the JInterfaceType representing {@code dalvik.annotation.AnnotationDefault}.
   * @param type any JDeclaredType produced from source, it is only used to find the JLookup.
   */
  @Nonnull
  private JAnnotationType getDefaultAnnotationType(@Nonnull JDefinedClassOrInterface type) {
    if (defaultAnnotationType == null) {
      defaultAnnotationType = Jack.getSession()
          .getPhantomLookup().getAnnotationType(DexAnnotations.ANNOTATION_ANNOTATION_DEFAULT);
    }
    assert defaultAnnotationType != null;
    return defaultAnnotationType;
  }

}
