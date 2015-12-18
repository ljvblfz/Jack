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

import com.android.jack.ir.ast.JAnnotation;
import com.android.jack.ir.ast.JAnnotationMethod;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JConstructor;
import com.android.jack.ir.ast.JLiteral;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JModifier;
import com.android.jack.ir.ast.JNameValuePair;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.marker.ThrownExceptionMarker;
import com.android.jack.ir.formatter.TypeFormatter;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.IBinaryAnnotation;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;
import org.eclipse.jdt.internal.compiler.env.IBinaryTypeAnnotation;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A {@code IBinaryMethod} for jack.
 */
class JAstBinaryMethod implements IBinaryMethod {

  @Nonnull
  private static final char[][] NO_EXCEPTION = new char[0][];

  @Nonnull
  private static final char[][] NO_ARGUMENTS_NAME = new char[0][];

  @Nonnull
  private final JMethod jMethod;

  JAstBinaryMethod(@Nonnull JMethod jMethod) {
    this.jMethod = jMethod;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getModifiers() {
    int modifier = LoaderUtils.convertJAstModifiersToEcj(jMethod.getModifier(),
        jMethod);
    if (getDefaultValue() != null) {
      modifier |= ClassFileConstants.AccAnnotationDefault;
    }
    return modifier;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isConstructor() {
    return jMethod instanceof JConstructor;
  }

  /**
   * {@inheritDoc} <p>
   * {@link IBinaryMethod#getArgumentNames()} documentation says this should return null when there
   * is no available arguments names, but reference seems to return a zero length array. So this
   * implementation never returns null.
   */
  @CheckForNull // as the interface method doc says
  @Override
  public char[][] getArgumentNames() {
    char[][] argumentsNames = NO_ARGUMENTS_NAME;
    List<JParameter> params = jMethod.getParams();
    if (params.size() != 0) {
      argumentsNames = new char[params.size()][];
    }
    int argIndex = 0;
    for (JParameter jParameter : params) {
      String name = jParameter.getName();
      argumentsNames[argIndex++] = name == null ? null : name.toCharArray();
    }
    return argumentsNames;
  }

  /**
   * {@inheritDoc}
   */
  @CheckForNull
  @Override
  public IBinaryAnnotation[] getAnnotations() {
    return AnnotationUtils.convertJAstAnnotationToEcj(jMethod, true);
  }

  /**
   * {@inheritDoc}
   */
  @CheckForNull
  @Override
  public Object getDefaultValue() {
    Object defaultValue = null;
    if (jMethod instanceof JAnnotationMethod) {
      JAnnotationMethod annotationMethod = (JAnnotationMethod) jMethod;
      JLiteral jLiteral = annotationMethod.getDefaultValue();
      if (jLiteral != null) {
        defaultValue = AnnotationUtils.getEcjAnnotationValue(jLiteral);
      } else {
        JAnnotation annotation = AnnotationUtils.getAnnotation(jMethod.getEnclosingType(),
            AnnotationUtils.DEFAULT_VALUE_ANNOTATION);
        if (annotation != null) {
          JNameValuePair defaultAnnotationPair = annotation.getNameValuePair(
              AnnotationUtils.DEFAULT_ANNOTATION_FIELD);
          assert defaultAnnotationPair != null;
          JAnnotation defaultAnnotation =
              (JAnnotation) defaultAnnotationPair.getValue();
          JNameValuePair defaultValuePair = defaultAnnotation.getNameValuePair(jMethod.getName());
          if (defaultValuePair != null) {
            defaultValue = AnnotationUtils.getEcjAnnotationValue(defaultValuePair.getValue());
          }
        }
      }
    }
    return defaultValue;
  }

  /**
   * {@inheritDoc} <p>
   *
   * {@link IBinaryMethod#getExceptionTypeNames()} documentation says this should return null when
   * there is no exception, but reference seems to return a zero length array. So this
   * implementation never returns null.
   */
  @CheckForNull // as the interface method doc says
  @Override
  public char[][] getExceptionTypeNames() {
    char[][] exceptionsBinaryNames = NO_EXCEPTION;
    ThrownExceptionMarker marker = jMethod.getMarker(ThrownExceptionMarker.class);
    if (marker != null) {
      List<JClass> throwns = marker.getThrownExceptions();
      if (throwns.size() != 0) {
        exceptionsBinaryNames = new char[throwns.size()][];
      }
      int argIndex = 0;
      TypeFormatter formatter = LoaderUtils.getQualifiedNameFormatter();
      for (JClass thrown : throwns) {
        exceptionsBinaryNames[argIndex++] = formatter.getName(thrown).toCharArray();
      }
    }
    return exceptionsBinaryNames;
  }

  /**
   * {@inheritDoc}
   */
  @CheckForNull
  @Override
  public char[] getGenericSignature() {
    return LoaderUtils.getGenericSignature(jMethod);
  }

  /**
   * {@inheritDoc}
   */
  @Nonnull
  @Override
  public char[] getMethodDescriptor() {
    StringBuilder sb = new StringBuilder();
    sb.append('(');

    for (JParameter p : jMethod.getParams()) {
      sb.append(LoaderUtils.getSignatureFormatter().getName(p.getType()));
    }

    sb.append(')');
    sb.append(LoaderUtils.getSignatureFormatter().getName(jMethod.getType()));

    return sb.toString().toCharArray();
  }

  /**
   * {@inheritDoc}
   */
  @CheckForNull
  @Override
  public IBinaryAnnotation[] getParameterAnnotations(int index, char[] classFileName) {
    JParameter param = jMethod.getParams().get(index);
    return AnnotationUtils.convertJAstAnnotationToEcj(param, false);
  }

  /**
   * {@inheritDoc}
   */
  @Nonnull
  @Override
  public char[] getSelector() {
    return getMethodName().toCharArray();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long getTagBits() {
    long tagBits = AnnotationUtils.getTagBits(jMethod);
    return tagBits;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isClinit() {
    return JModifier.isStaticInitializer(jMethod.getModifier());
  }

  @Nonnull
  @Override
  public String toString() {
    return jMethod.toString();
  }

  @Nonnull
  private String getMethodName() {
    return jMethod.getName();
  }

  @Override
  public int getAnnotatedParametersCount() {
    int result = 0;
    for (JParameter param : jMethod.getParams()) {
      if (param.getAnnotations().size() > 0) {
        result++;
      }
    }
    return result;
  }

  @Override
  public IBinaryTypeAnnotation[] getTypeAnnotations() {
    return null;
  }
}
