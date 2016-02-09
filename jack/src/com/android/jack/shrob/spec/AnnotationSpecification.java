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

package com.android.jack.shrob.spec;

import com.android.jack.ir.ast.JAnnotation;
import com.android.jack.shrob.proguard.GrammarActions;

import java.util.Collection;

import javax.annotation.Nonnull;

/**
 * Class representing the specification of an annotation in a {@code keep} rule
 */
public class AnnotationSpecification implements Specification<Collection<JAnnotation>> {
  @Nonnull
  private final NameSpecification annotationType;

  public AnnotationSpecification(@Nonnull NameSpecification name) {
    this.annotationType = name;
  }

  @Override
  public boolean matches(@Nonnull Collection<JAnnotation> t) {
    boolean annotationFound = false;

    for (JAnnotation annotation : t) {
      if (annotationType.matches(
          GrammarActions.getBinaryNameFormatter().getName(annotation.getType()))) {
        annotationFound = true;
      }
    }

    if (!annotationFound) {
      return false;
    }

    return true;
  }

  @Override
  @Nonnull
  public String toString() {
    return "@" + annotationType.toString();
  }
}
