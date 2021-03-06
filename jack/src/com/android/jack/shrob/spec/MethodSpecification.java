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

import com.android.jack.ir.ast.JConstructor;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.shrob.proguard.GrammarActions;
import com.android.jack.util.NamingTools;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Class representing a {@code method} in a {@code class specification}
 */
public class MethodSpecification implements Specification<JMethod>{
  @CheckForNull
  private final AnnotationSpecification annotationType;

  @CheckForNull
  private final ModifierSpecification modifier;

  @CheckForNull
  private final NameSpecification type;

  @Nonnull
  private final NameSpecification fullSourceName;

  public MethodSpecification(
      @Nonnull NameSpecification sigPattern,
      @CheckForNull ModifierSpecification modifier,
      @CheckForNull NameSpecification type,
      @CheckForNull AnnotationSpecification annotationType) {
    this.fullSourceName = sigPattern;
    this.modifier = modifier;
    this.annotationType = annotationType;
    this.type = type;
  }

  @Override
  public boolean matches(@Nonnull JMethod t) {
    if (modifier != null && !modifier.matches(t)) {
      return false;
    }

    if (annotationType != null && !annotationType.matches(t.getAnnotations())) {
      return false;
    }

    if (type != null
        && !type.matches(GrammarActions.getSourceFormatter().getName(t.getType()))) {
      return false;
    }

    String signature =
        GrammarActions.getSourceFormatter().getNameWithoutReturnType(t.getMethodIdWide());
    if (t instanceof JConstructor) {
      String methodName = signature.replace(NamingTools.INIT_NAME, t.getEnclosingType().getName());
      if (fullSourceName.matches(methodName)) {
        return true;
      }
      methodName = signature.replace(NamingTools.INIT_NAME,
          GrammarActions.getSourceFormatter().getName(t.getEnclosingType()));
      if (fullSourceName.matches(methodName)) {
        return true;
      }
    }

    return fullSourceName.matches(signature);
  }

  @Override
  @Nonnull
  public String toString() {
    StringBuilder sb = new StringBuilder("method: ");

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

    sb.append(fullSourceName);
    sb.append(';');

    return sb.toString();
  }
}
