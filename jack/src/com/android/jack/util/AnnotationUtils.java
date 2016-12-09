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

import com.android.jack.ir.ast.JAnnotation;
import com.android.jack.ir.ast.JAnnotationType;
import com.android.jack.ir.ast.JBooleanLiteral;
import com.android.jack.ir.ast.JNameValuePair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * Class providing tools to manipulate{@link JAnnotation}.
 */
public class AnnotationUtils {

  @Nonnull
  public static Set<JAnnotationType> getAnnotationTypes(
      @Nonnull Collection<JAnnotation> annotations) {
    Set<JAnnotationType> annotationTypes = new HashSet<JAnnotationType>();
    for (JAnnotation annotation : annotations) {
      annotationTypes.add(annotation.getType());
    }
    return annotationTypes;
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

  public static boolean getBooleanValueFromAnnotation(@Nonnull JAnnotation annotation,
      @Nonnull String name, boolean defaultValue) {
    boolean value = defaultValue;
    JNameValuePair removeAfterValuePair = annotation.getNameValuePair(name);
    if (removeAfterValuePair != null
        && removeAfterValuePair.getValue() instanceof JBooleanLiteral) {
      value = ((JBooleanLiteral) removeAfterValuePair.getValue()).getValue();
    }
    return value;
  }
}
