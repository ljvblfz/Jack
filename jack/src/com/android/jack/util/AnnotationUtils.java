/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.jack.util;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

import com.android.jack.ir.ast.JAnnotation;
import com.android.jack.ir.ast.JAnnotationType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Class providing tools to manipulate{@link JAnnotation}.
 */
public class AnnotationUtils {

  @Nonnull
  public static Collection<JAnnotationType> getAnnotationTypes(
      @Nonnull Collection<JAnnotation> annotations) {
    return Collections2.transform(annotations, new Function<JAnnotation, JAnnotationType>() {
     @Override
      public JAnnotationType apply(JAnnotation annotation) {
        return annotation.getType();
      }
    });
  }

  @Nonnull
  public static List<JAnnotation> getAnnotation(@Nonnull Collection<JAnnotation> annotations,
      @Nonnull JAnnotationType annotationType) {
    List<JAnnotation> foundAnnotations = new ArrayList<JAnnotation>();

    for (JAnnotation annotation : annotations) {
      if (annotation.getType().equals(annotationType)) {
        foundAnnotations.add(annotation);
      }
    }

    return foundAnnotations;
  }
}
