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


import com.android.jack.ir.ast.JField;
import com.android.jack.shrob.proguard.GrammarActions;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Class representing a {@code field} in a {@code class specification}
 */
public class FieldSpecification implements Specification<JField> {
  @CheckForNull
  private final AnnotationSpecification annotationType;

  @CheckForNull
  private final ModifierSpecification modifier;

  @Nonnull
  private final NameSpecification name;

  @CheckForNull
  private final NameSpecification type;

  public FieldSpecification(
      @Nonnull NameSpecification name,
      @CheckForNull ModifierSpecification modifier,
      @CheckForNull NameSpecification type,
      @CheckForNull AnnotationSpecification annotationType) {
    this.name = name;
    this.modifier = modifier;
    this.type = type;
    this.annotationType = annotationType;
  }

  @Override
  public boolean matches(@Nonnull JField f) {
    if (modifier != null && !modifier.matches(f)) {
      return false;
    }

    if (annotationType != null && !annotationType.matches(f.getAnnotations())) {
      return false;
    }

    if (!name.matches(f.getName())) {
      return false;
    }

    if (type != null
        && !type.matches(GrammarActions.getSignatureFormatter().getName(f.getType()))) {
      return false;
    }

    return true;
  }

  @Override
  @Nonnull
  public String toString() {
    StringBuilder sb = new StringBuilder("field: ");

    if (annotationType != null) {
      sb.append(annotationType);
      sb.append(' ');
    }

    if (modifier != null) {
      sb.append(modifier);
      sb.append(' ');
    }

    if (type != null) {
      sb.append(type);
      sb.append(' ');
    }

    sb.append(name);
    sb.append(';');

    return sb.toString();
  }
}