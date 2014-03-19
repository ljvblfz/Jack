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
import com.android.jack.ir.SourceInfo;
import com.android.jack.ir.SourceOrigin;
import com.android.jack.ir.ast.JAnnotation;
import com.android.jack.ir.ast.JAnnotationLiteral;
import com.android.jack.ir.ast.JAnnotationMethod;
import com.android.jack.ir.ast.JDefinedAnnotation;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JLiteral;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodId;
import com.android.jack.ir.ast.JNameValuePair;
import com.android.jack.ir.ast.JRetentionPolicy;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.shrob.obfuscation.OriginalNames;
import com.android.jack.transformations.request.AddAnnotation;
import com.android.jack.transformations.request.AddNameValuePair;
import com.android.jack.transformations.request.Remove;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.util.filter.Filter;
import com.android.sched.item.Description;
import com.android.sched.item.Synchronized;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.BooleanPropertyId;

import java.util.Collections;

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
@HasKeyId
@Description("Add annotation methods default values as system annotation.")
@Synchronized
@Transform(remove = AnnotationMethodDefaultValue.class,
    add = {JAnnotationLiteral.class, JNameValuePair.class})
@Constraint(need = OriginalNames.class)
public class DefaultValueAnnotationAdder implements RunnableSchedulable<JMethod> {

  @Nonnull
  public static final BooleanPropertyId EMIT_ANNOTATION_DEFAULT = BooleanPropertyId.create(
      "jack.annotation.annotationdefault", "Emit annotation default")
      .addDefaultValue("true");

  @Nonnull
  private final Filter<JMethod> filter = ThreadConfig.get(Options.METHOD_FILTER);

  @CheckForNull
  private JAnnotation defaultAnnotation;

  private final boolean addAnnotationDefault =
      ThreadConfig.get(EMIT_ANNOTATION_DEFAULT).booleanValue();

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
        if (addAnnotationDefault) {
          SourceInfo sourceInfo = defaultValue.getSourceInfo();
          JAnnotationLiteral defaultAnnotation =
              getDefaultAnnotation((JDefinedAnnotation) enclosingType, tr);
          tr.append(new AddNameValuePair(defaultAnnotation,
              new JNameValuePair(sourceInfo, method.getMethodId(), defaultValue)));
        }
        tr.commit();
      }
    }
  }

  @Nonnull
  private JAnnotationLiteral getDefaultAnnotation(@Nonnull JDefinedAnnotation targetType,
      @Nonnull TransformationRequest tr) {
    JAnnotation defaultAnnotationType = getDefaultAnnotationType(targetType);
    JAnnotationLiteral defaultAnnotation =
        targetType.getAnnotation(defaultAnnotationType);
    if (defaultAnnotation == null) {
      defaultAnnotation = new JAnnotationLiteral(
          SourceOrigin.UNKNOWN, JRetentionPolicy.SYSTEM, defaultAnnotationType);
      JMethodId methodId = defaultAnnotationType.getOrCreateMethodId(
          "value", Collections.<JType>emptyList(), MethodKind.INSTANCE_VIRTUAL);
      defaultAnnotation
          .add(new JNameValuePair(SourceOrigin.UNKNOWN, methodId, new JAnnotationLiteral(
              SourceOrigin.UNKNOWN, targetType.getRetentionPolicy(), targetType)));
      tr.append(new AddAnnotation(defaultAnnotation, targetType));
    }
    return (JAnnotationLiteral) defaultAnnotation.getNameValuePairs().iterator().next().getValue();
  }

  /**
   * Get the JInterfaceType representing {@code dalvik.annotation.AnnotationDefault}.
   * @param type any JDeclaredType produced from source, it is only used to find the JLookup.
   */
  @Nonnull
  private JAnnotation getDefaultAnnotationType(@Nonnull JDefinedClassOrInterface type) {
    if (defaultAnnotation == null) {
      defaultAnnotation = type.getJProgram()
          .getPhantomLookup().getAnnotation(DexAnnotations.ANNOTATION_ANNOTATION_DEFAULT);
    }
    assert defaultAnnotation != null;
    return defaultAnnotation;
  }

}
