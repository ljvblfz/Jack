/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.jack.preprocessor;

import com.android.jack.ir.ast.Annotable;
import com.android.jack.ir.ast.JAnnotationLiteral;
import com.android.jack.ir.ast.JDefinedAnnotation;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.transformations.request.TransformationStep;

import java.util.Collection;

import javax.annotation.Nonnull;

/**
 * {@link TransformationStep} for adding some {@link JAnnotationLiteral} on one {@link Annotable}.
 */
public class AddAnnotationStep implements TransformationStep {

  @Nonnull
  private final JDefinedAnnotation annotation;

  @Nonnull
  private final Collection<?> toAnnotate;

  public AddAnnotationStep(@Nonnull JDefinedAnnotation annotation,
      @Nonnull Collection<?> toAnnotate) {
    this.annotation = annotation;
    this.toAnnotate = toAnnotate;
  }

  @Override
  public void apply() {
    for (Object candidate : toAnnotate) {
      if (candidate instanceof Annotable) {
        Annotable annotable = (Annotable) candidate;
        // Do not override existing annotation
        if (annotable.getAnnotations(annotation).isEmpty()) {
          JAnnotationLiteral literal = new JAnnotationLiteral(
              SourceInfo.UNKNOWN, annotation.getRetentionPolicy(), annotation);
          annotable.addAnnotation(literal);
          literal.updateParents((JNode) annotable);
        }
      }
    }
  }
}
