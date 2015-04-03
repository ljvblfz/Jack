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

package com.android.jack.ecj.loader.jast;

import com.android.jack.ir.ast.Annotable;
import com.android.jack.ir.ast.JAnnotation;
import com.android.jack.ir.ast.JArrayLiteral;
import com.android.jack.ir.ast.JClassLiteral;
import com.android.jack.ir.ast.JEnumLiteral;
import com.android.jack.ir.ast.JLiteral;
import com.android.jack.ir.ast.JNameValuePair;
import com.android.jack.ir.ast.JRetentionPolicy;

import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.env.ClassSignature;
import org.eclipse.jdt.internal.compiler.env.EnumConstantSignature;
import org.eclipse.jdt.internal.compiler.env.IBinaryAnnotation;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

class AnnotationUtils {

  static final String DEFAULT_VALUE_ANNOTATION = "Ldalvik/annotation/AnnotationDefault;";
  static final String ENCLOSING_CLASS_ANNOTATION = "Ldalvik/annotation/EnclosingClass;";
  static final String ENCLOSING_METHOD_ANNOTATION = "Ldalvik/annotation/EnclosingMethod;";
  static final String INNER_CLASS_ANNOTATION = "Ldalvik/annotation/InnerClass;";
  static final String MEMBER_CLASSES_ANNOTATION = "Ldalvik/annotation/MemberClasses;";
  static final String SIGNATURE_ANNOTATION = "Ldalvik/annotation/Signature;";
  static final String DECLARED_THROWS = "Ldalvik/annotation/Throws;";

  static final String DEPRECATED_ANNOTATION = "Ljava/lang/Deprecated;";
  static final String RETENTION_POLICY_ANNOTATION = "Ljava/lang/annotation/Retention;";
  static final String TARGET_ANNOTATION = "Ljava/lang/annotation/Target;";
  static final String DOCUMENTED_ANNOTATION = "Ljava/lang/annotation/Documented;";
  static final String INHERITED_ANNOTATION = "Ljava/lang/annotation/Inherited;";
  static final String ELEMENT_TYPE = "Ljava/lang/annotation/ElementType;";

  static final String RETENTION_POLICY_SOURCE = "SOURCE";
  static final String RETENTION_POLICY_CLASS = "CLASS";
  static final String RETENTION_POLICY_RUNTIME = "RUNTIME";

  static final String DEFAULT_ANNOTATION_FIELD = "value";
  static final String INNERCLASS_ACCFLAGS_FIELD = "accessFlags";
  static final String INNERCLASS_NAME_FIELD = "name";

  private static final Set<String> dalvikAnnotations = new HashSet<String>();

  private static final Set<String> tagbitsAnnotations = new HashSet<String>();

  static {
    dalvikAnnotations.add(DEFAULT_VALUE_ANNOTATION);
    dalvikAnnotations.add(ENCLOSING_CLASS_ANNOTATION);
    dalvikAnnotations.add(ENCLOSING_METHOD_ANNOTATION);
    dalvikAnnotations.add(INNER_CLASS_ANNOTATION);
    dalvikAnnotations.add(MEMBER_CLASSES_ANNOTATION);
    dalvikAnnotations.add(SIGNATURE_ANNOTATION);
    dalvikAnnotations.add(DECLARED_THROWS);

    tagbitsAnnotations.add(DEPRECATED_ANNOTATION);
    tagbitsAnnotations.add(RETENTION_POLICY_ANNOTATION);
    tagbitsAnnotations.add(TARGET_ANNOTATION);
    tagbitsAnnotations.add(DOCUMENTED_ANNOTATION);
    tagbitsAnnotations.add(INHERITED_ANNOTATION);
  }

  @CheckForNull
  static IBinaryAnnotation[] convertJAstAnnotationToEcj(@Nonnull Annotable annotable,
      boolean filterTagbitsAnnotations) {

    Collection<JAnnotation> annotations = annotable.getAnnotations();
    ArrayList<IBinaryAnnotation> list = new ArrayList<IBinaryAnnotation>(annotations.size());

    for (JAnnotation annotation : annotations) {
      boolean isFilteredOut = isDalvikAnnotation(annotation)
              || (filterTagbitsAnnotations && isTagbitsAnnotation(annotation));
      isFilteredOut |= (annotation.getRetentionPolicy() == JRetentionPolicy.SOURCE);
      if (!isFilteredOut) {
        list.add(new JAstBinaryAnnotation(annotation));
      }
    }

    IBinaryAnnotation[] ecjAnnotations = null;
    if (!list.isEmpty()) {
      ecjAnnotations = list.toArray(new IBinaryAnnotation[list.size()]);
    }
    return ecjAnnotations;
  }

  /**
   * @see org.eclipse.jdt.internal.compiler.env.IBinaryElementValuePair#getValue
   */
  @Nonnull
  static Object getEcjAnnotationValue(@Nonnull JLiteral literal) {

    Object ecjValue;
    Constant constant = LoaderUtils.convertJLiteralToEcj(literal);
    if (constant == Constant.NotAConstant) {
      if (literal instanceof JAnnotation) {
        JAnnotation subAnnotation = (JAnnotation) literal;
        ecjValue = new JAstBinaryAnnotation(subAnnotation);
      } else if (literal instanceof JArrayLiteral) {
        JArrayLiteral array = (JArrayLiteral) literal;
        List<JLiteral> subValues = array.getValues();
        int arraySize = subValues.size();
        Object[] ecjValues = new Object[arraySize];
        for (int i = 0; i < arraySize; i++) {
          ecjValues[i] = getEcjAnnotationValue(subValues.get(i));
        }
        ecjValue = ecjValues;
      } else if (literal instanceof JEnumLiteral) {
        JEnumLiteral enumValue = (JEnumLiteral) literal;
        String classBinaryName = LoaderUtils.getSignatureFormatter().getName(enumValue.getType());
        String fieldName = enumValue.getFieldId().getName();
        EnumConstantSignature ecjEnumValue =
            new EnumConstantSignature(classBinaryName.toCharArray(), fieldName.toCharArray());
        ecjValue = ecjEnumValue;

      } else if (literal instanceof JClassLiteral) {
        JClassLiteral type = (JClassLiteral) literal;
        ecjValue = new ClassSignature(
            LoaderUtils.getSignatureFormatter().getName(type.getRefType()).toCharArray());

      } else {
        // primitive values should be handled by convertJLiteralToEcj
        // field, method and null do not seem to be supported by ecj API
        throw new AssertionError();

      }
    } else {
      ecjValue = constant;
    }
    return ecjValue;
  }

  static long getTagBits(@CheckForNull Annotable annotable) {
    if (annotable == null)  {
      return 0;
    }

    long tagBits = 0;
    JAnnotation targetAnnotation = getAnnotation(annotable, TARGET_ANNOTATION);
    if (targetAnnotation != null) {
      JNameValuePair pair = targetAnnotation.getNameValuePair(DEFAULT_ANNOTATION_FIELD);
      if ((pair != null) && ((JArrayLiteral) pair.getValue()).getValues().size() != 0) {
        JArrayLiteral targetValueArray = (JArrayLiteral) pair.getValue();
        for (JLiteral value : targetValueArray.getValues()) {
          JEnumLiteral targetValue = (JEnumLiteral) value;
          assert LoaderUtils.getSignatureFormatter()
              .getName(targetValue.getType()).equals(ELEMENT_TYPE);
          String target = targetValue.getFieldId().getName();
          tagBits |= Annotation.getTargetElementType(target.toCharArray());
        }

      } else {
        tagBits |= TagBits.AnnotationTarget;
      }
    }
    JAnnotation retentionPolicyAnnotation =
        getAnnotation(annotable, RETENTION_POLICY_ANNOTATION);
    if (retentionPolicyAnnotation != null) {
      JNameValuePair retentionPolicy =
          retentionPolicyAnnotation.getNameValuePair(DEFAULT_ANNOTATION_FIELD);
      assert retentionPolicy != null;
      assert !(((JEnumLiteral) retentionPolicy.getValue()).getFieldId().getName().equals("SYSTEM")
          || ((JEnumLiteral) retentionPolicy.getValue()).getFieldId()
              .getName().equals("UNKNOWN"));

      tagBits |= Annotation.getRetentionPolicy(
          ((JEnumLiteral) retentionPolicy.getValue()).getFieldId().getName().toCharArray());
    }
    if (getAnnotation(annotable, DEPRECATED_ANNOTATION) != null) {
      tagBits |= TagBits.AnnotationDeprecated;
    }
    if (getAnnotation(annotable, DOCUMENTED_ANNOTATION) != null) {
      tagBits |= TagBits.AnnotationDocumented;
    }
    if (getAnnotation(annotable, INHERITED_ANNOTATION) != null) {
      tagBits |= TagBits.AnnotationInherited;
    }
    return tagBits;
  }

  @CheckForNull
  static JAnnotation getAnnotation(@Nonnull Annotable annotable,
      @Nonnull String annotationType) {
    for (JAnnotation annotation: annotable.getAnnotations()) {
      if (annotationType.equals(
          LoaderUtils.getSignatureFormatter().getName(annotation.getType()))) {
        return annotation;
      }
    }
    return null;
  }

  private static boolean isDalvikAnnotation(@Nonnull JAnnotation annotation) {
    String annotationType = LoaderUtils.getSignatureFormatter().getName(annotation.getType());
    return dalvikAnnotations.contains(annotationType);
  }

  private static boolean isTagbitsAnnotation(@Nonnull JAnnotation annotation) {
    String annotationType = LoaderUtils.getSignatureFormatter().getName(annotation.getType());
    return tagbitsAnnotations.contains(annotationType);
  }

}
