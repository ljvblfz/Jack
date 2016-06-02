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
import com.android.sched.transform.TransformStep;

import javax.annotation.Nonnull;

/**
 * A {@link TransformationStep} allowing to add a {@link JNameValuePair} in a {@link JAnnotation}.
 *
 * @see PutNameValuePair
 */
public class AddNameValuePair implements TransformationStep, TransformStep {
  @Nonnull
  private final JAnnotation annotation;
  @Nonnull
  private final JNameValuePair pair;

  /**
   * Constructor specifying a name-value pair to add to an annotation.
   *
   * @param annotation the annotation to update
   * @param nameValuePair the name-value pair to add to the annotation
   */
  public AddNameValuePair(@Nonnull JAnnotation annotation,
      @Nonnull JNameValuePair nameValuePair) {
    this.annotation = annotation;
    this.pair = nameValuePair;
  }

  @Override
  public void apply() throws UnsupportedOperationException {
    annotation.add(pair);
    pair.updateParents(annotation);
  }

  @Override
  @Nonnull
  public String toString() {
    StringBuilder sb = new StringBuilder("Add ");
    sb.append(pair.toSource());
    sb.append(" in ");
    sb.append(annotation.toSource());
    sb.append(" on ");
    sb.append(annotation.getParent().toSource());
    return sb.toString();
  }
}
