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

import com.android.jack.ir.ast.JAnnotationLiteral;
import com.android.jack.ir.ast.JNameValuePair;

import org.eclipse.jdt.internal.compiler.env.IBinaryAnnotation;
import org.eclipse.jdt.internal.compiler.env.IBinaryElementValuePair;

import java.util.Collection;

import javax.annotation.Nonnull;

/**
 * A {@code IBinaryAnnotation} for jack.
 */
class JAstBinaryAnnotation implements IBinaryAnnotation {

  @Nonnull
  private final JAnnotationLiteral jAnnotation;

  JAstBinaryAnnotation(@Nonnull JAnnotationLiteral annotation) {
    this.jAnnotation = annotation;
  }

  /**
   * {@inheritDoc}
   */
  @Nonnull
  @Override
  public char[] getTypeName() {
    return LoaderUtils.getSignatureFormatter().getName(jAnnotation.getType()).toCharArray();
  }

  /**
   * {@inheritDoc}
   */
  @Nonnull
  @Override
  public IBinaryElementValuePair[] getElementValuePairs() {
    Collection<JNameValuePair> jPairs = jAnnotation.getNameValuePairs();
    int pairCount = jPairs.size();
    IBinaryElementValuePair[] pairs = new IBinaryElementValuePair[pairCount];

    int pairIndex = 0;
    for (JNameValuePair jPair : jPairs) {
      Object value = AnnotationUtils.getEcjAnnotationValue(jPair.getValue());
      pairs[pairIndex] = new JAstBinaryElementValuePair(
          jPair.getName().toCharArray(), value);
      pairIndex++;
    }
    return pairs;
  }

  @Nonnull
  @Override
  public String toString() {
    return jAnnotation.toString();
  }


}
