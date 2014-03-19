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

import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JLiteral;
import com.android.jack.ir.ast.JModifier;

import org.eclipse.jdt.internal.compiler.env.IBinaryAnnotation;
import org.eclipse.jdt.internal.compiler.env.IBinaryField;
import org.eclipse.jdt.internal.compiler.impl.Constant;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A {@code IBinaryField} for jack.
 */
class JAstBinaryField implements IBinaryField {

  @Nonnull
  private final JField jField;
  @CheckForNull
  private final JLiteral initialValue;

  JAstBinaryField(
      @Nonnull JField jField,
      @CheckForNull JLiteral initialValue) {
    this.jField = jField;
    this.initialValue = initialValue;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getModifiers() {
    int modifiers = LoaderUtils.convertJAstModifiersToEcj(jField.getModifier() &
        (~JModifier.COMPILE_TIME_CONSTANT), jField);
    return modifiers;
  }

  /**
   * {@inheritDoc}
   */
  @CheckForNull
  @Override
  public IBinaryAnnotation[] getAnnotations() {
    return AnnotationUtils.convertJAstAnnotationToEcj(jField, true);
  }

  /**
   * {@inheritDoc}
   */
  @Nonnull
  @Override
  public Constant getConstant() {
    Constant constant;
    if (JModifier.isCompileTimeConstant(jField.getModifier())) {
      constant = LoaderUtils.convertJLiteralToEcj(initialValue);
    } else {
      constant = Constant.NotAConstant;
    }
    return constant;
  }

  /**
   * {@inheritDoc}
   */
  @CheckForNull
  @Override
  public char[] getGenericSignature() {
    return LoaderUtils.getGenericSignature(jField);
  }

  /**
   * {@inheritDoc}
   */
  @Nonnull
  @Override
  public char[] getName() {
    return jField.getName().toCharArray();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long getTagBits() {
    return AnnotationUtils.getTagBits(jField);
  }

  /**
   * {@inheritDoc}
   */
  @Nonnull
  @Override
  public char[] getTypeName() {
    return LoaderUtils.getSignatureFormatter().getName(jField.getType()).toCharArray();
  }

  @Nonnull
  @Override
  public String toString() {
    return jField.toString();
  }
}
