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

import com.android.jack.Options;
import com.android.jack.backend.dex.DexAnnotations;
import com.android.jack.ir.ast.JAnnotationLiteral;
import com.android.jack.ir.ast.JAnnotationMethod;
import com.android.jack.ir.ast.JAnnotationType;
import com.android.jack.ir.ast.JDefinedAnnotationType;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JLiteral;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodId;
import com.android.jack.ir.ast.JNameValuePair;
import com.android.jack.ir.ast.JRetentionPolicy;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.transformations.request.AddAnnotation;
import com.android.jack.transformations.request.AddNameValuePair;
import com.android.jack.transformations.request.Remove;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.util.filter.Filter;
import com.android.sched.item.Description;
import com.android.sched.item.Synchronized;
import com.android.sched.schedulable.RunnableSchedulable;
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
 * @dalvik.annotation.AnnotationDefault(value = @Annotation1(v2 = 2))
 * public @interface Annotation1 {
 *  int v1;
 *  int v2;
 * }
 * </pre>
 */
@Description("Add annotation methods default values as system annotation.")
@Synchronized
@Transform(remove = AnnotationMethodDefaultValue.class,
    add = {JAnnotationLiteral.class, JNameValuePair.class})
public class DefaultValueAnnotationAdder implements RunnableSchedulable<JMethod> {

  @Nonnull
  private final Filter<JMethod> filter = ThreadConfig.get(Options.METHOD_FILTER);

  @CheckForNull
  private JAnnotationType defaultAnnotationType;

  @Override
  public synchronized void run(@Nonnull JMethod method) throws Exception {
    final JDefinedClassOrInterface enclosingType = method.getEnclosingType();
    if (enclosingType.isExternal()
        || !filter.accept(this.getClass(), method)) {
      return;
    }

    if (method instanceof JAnnotationMethod) {
      JAnnotationMethod annotationMethod = (JAnnotationMethod) method;
      JLiteral defaultValue = annotationMethod.getDefaultValue();
      if (defaultValue != null) {
        TransformationRequest tr = new TransformationRequest(enclosingType);
        tr.append(new Remove(defaultValue));
        SourceInfo sourceInfo = defaultValue.getSourceInfo();
        JAnnotationLiteral defaultAnnotation =
            getDefaultAnnotation((JDefinedAnnotationType) enclosingType, tr);
        tr.append(new AddNameValuePair(defaultAnnotation,
            new JNameValuePair(sourceInfo, method.getMethodId(), defaultValue)));
        tr.commit();
      }
    }
  }

  @Nonnull
  private JAnnotationLiteral getDefaultAnnotation(@Nonnull JDefinedAnnotationType targetType,
      @Nonnull TransformationRequest tr) {
    JAnnotationType defaultAnnotationType = getDefaultAnnotationType(targetType);
    JAnnotationLiteral defaultAnnotation = null;
    List<JAnnotationLiteral> defaultAnnotations = targetType.getAnnotations(defaultAnnotationType);
    if (defaultAnnotations.isEmpty()) {
      defaultAnnotation = new JAnnotationLiteral(SourceInfo.UNKNOWN, JRetentionPolicy.SYSTEM,
          defaultAnnotationType);
      JMethodId methodId = defaultAnnotationType.getOrCreateMethodId("value",
          Collections.<JType>emptyList(), MethodKind.INSTANCE_VIRTUAL);
      defaultAnnotation.add(new JNameValuePair(SourceInfo.UNKNOWN, methodId,
          new JAnnotationLiteral(SourceInfo.UNKNOWN, targetType.getRetentionPolicy(), targetType)));
      tr.append(new AddAnnotation(defaultAnnotation, targetType));
    } else {
      assert defaultAnnotations.size() == 1;
      defaultAnnotation = defaultAnnotations.get(0);
    }
    return (JAnnotationLiteral) defaultAnnotation.getNameValuePairs().iterator().next().getValue();
  }

  /**
   * Get the JInterfaceType representing {@code dalvik.annotation.AnnotationDefault}.
   * @param type any JDeclaredType produced from source, it is only used to find the JLookup.
   */
  @Nonnull
  private JAnnotationType getDefaultAnnotationType(@Nonnull JDefinedClassOrInterface type) {
    if (defaultAnnotationType == null) {
      defaultAnnotationType = type.getSession()
          .getPhantomLookup().getAnnotationType(DexAnnotations.ANNOTATION_ANNOTATION_DEFAULT);
    }
    assert defaultAnnotationType != null;
    return defaultAnnotationType;
  }

}
