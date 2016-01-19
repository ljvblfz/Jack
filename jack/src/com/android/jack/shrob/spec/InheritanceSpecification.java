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

import com.android.jack.Jack;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JInterface;
import com.android.jack.shrob.proguard.GrammarActions;
import com.android.sched.util.log.LoggerFactory;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Class representing the inheritance in a {@code class specification}
 */
public class InheritanceSpecification implements Specification<JDefinedClassOrInterface>{

  @Nonnull
  private static final Logger logger = LoggerFactory.getLogger();

  @CheckForNull
  private final AnnotationSpecification annotationType;

  @Nonnull
  private final NameSpecification className;

  public InheritanceSpecification(
      @Nonnull NameSpecification className, @CheckForNull AnnotationSpecification annotationType) {
    this.className = className;
    this.annotationType = annotationType;
  }

  private boolean checkNameAndAnnotations(@Nonnull JDefinedClassOrInterface type) {
    if (annotationType != null && !annotationType.matches(type.getAnnotations())) {
      return false;
    }

    return (className.matches(GrammarActions.getSourceFormatter().getName(type)));
  }

  @Override

  public boolean matches(@Nonnull JDefinedClassOrInterface type) {
    for (JInterface implement : type.getImplements()) {
      if (!(implement instanceof JDefinedClassOrInterface)) {
        logger.log(Level.WARNING, "Super interface {0} of {1} is missing from classpath.",
            new Object[]{
            Jack.getUserFriendlyFormatter().getName(implement),
            Jack.getUserFriendlyFormatter().getName(type)
        });
        continue;
      }
      JDefinedClassOrInterface classOrInterface = (JDefinedClassOrInterface) implement;
      if (checkNameAndAnnotations(classOrInterface) || matches(classOrInterface)) {
        return true;
      }
    }
    JClass superclass = type.getSuperClass();
    if (superclass != null) {
      if (!(superclass instanceof JDefinedClassOrInterface)) {
        logger.log(Level.WARNING, "Super class {0} of {1} is missing from classpath.",
            new Object[]{
            Jack.getUserFriendlyFormatter().getName(superclass),
            Jack.getUserFriendlyFormatter().getName(type)
        });
      } else {
        JDefinedClassOrInterface definedSuper = (JDefinedClassOrInterface) superclass;
        return checkNameAndAnnotations(definedSuper) || matches(definedSuper);
      }
    }
    return false;
  }

  @Override
  @Nonnull
  public String toString() {
    StringBuilder sb = new StringBuilder("extends ");

    if (annotationType != null) {
      sb.append(annotationType);
      sb.append(' ');
    }

    sb.append(className);

    return sb.toString();
  }
}
