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

package com.android.jack.transformations.request;

import com.android.jack.ir.ast.JAnnotation;
import com.android.jack.ir.ast.JNameValuePair;
import com.android.jack.ir.ast.JNode;

import javax.annotation.Nonnull;

/**
 * A {@link TransformationStep} allowing to put a {@link JNameValuePair}
 * in a {@link JAnnotation} (replaces existing pair with same name).
 *
 * @see AddNameValuePair
 */
public class PutNameValuePair implements TransformationStep {
  @Nonnull
  private final JAnnotation annotation;
  @Nonnull
  private final JNameValuePair pair;

  /**
   * Constructor specifying a name-value pair to put to an annotation.
   *
   * @param annotation the annotation to update
   * @param nameValuePair the name-value pair to add to the annotation
   */
  public PutNameValuePair(@Nonnull JAnnotation annotation,
      @Nonnull JNameValuePair nameValuePair) {
    this.annotation = annotation;
    this.pair = nameValuePair;
  }

  @Override
  public void apply() throws UnsupportedOperationException {
    annotation.put(pair);
    pair.updateParents(annotation);
  }

  @Override
  @Nonnull
  public String toString() {
    final JNode annotationParent = annotation.getParent();
    assert annotationParent != null;
    StringBuilder sb = new StringBuilder("Put ");
    sb.append(pair.toSource());
    sb.append(" in ");
    sb.append(annotation.toSource());
    sb.append(" on ");
    sb.append(annotationParent.toSource());
    return sb.toString();
  }
}
