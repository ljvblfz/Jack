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


import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.shrob.proguard.GrammarActions;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Class representing the specification of a class in a {@code keep} rule
 */
public class ClassSpecification implements Specification<JDefinedClassOrInterface>{

  @Nonnull
  private KeepModifier keepModifier;

  @CheckForNull
  private AnnotationSpecification annotationType;

  @CheckForNull
  private ModifierSpecification modifier;

  @Nonnull
  private final ClassTypeSpecification classType;

  @Nonnull
  private final List<NameSpecification> nameSpecs;

  @CheckForNull
  private InheritanceSpecification inheritance;

  @Nonnull
  private final List<FieldSpecification> fieldSpecs = new ArrayList<FieldSpecification>();

  @Nonnull
  private final List<MethodSpecification> methodSpecs = new ArrayList<MethodSpecification>();

  public ClassSpecification(
      @Nonnull List<NameSpecification> names,
      @Nonnull ClassTypeSpecification classType,
      @CheckForNull AnnotationSpecification annotation) {
    this.classType = classType;
    this.annotationType = annotation;
    this.keepModifier = new KeepModifier();
    assert !names.isEmpty();
    this.nameSpecs = names;
  }

  @Nonnull
  public List<FieldSpecification> getFieldSpecs() {
    return fieldSpecs;
  }

  @Nonnull
  public List<MethodSpecification> getMethodSpecs() {
    return methodSpecs;
  }

  public void setAnnotationType(@CheckForNull AnnotationSpecification annotationType) {
    this.annotationType = annotationType;
  }

  public void setKeepModifier(@Nonnull KeepModifier keepModifier) {
    this.keepModifier = keepModifier;
  }

  @Nonnull
  public KeepModifier getKeepModifier() {
    return keepModifier;
  }

  @Override
  public boolean matches(@Nonnull JDefinedClassOrInterface type) {
    if (annotationType != null && !annotationType.matches(type.getAnnotations())) {
      return false;
    }

    if (modifier != null && !modifier.matches(type)) {
      return false;
    }

    if (!classType.matches(type)) {
      return false;
    }

    boolean matchedName = false;
    for (NameSpecification name : nameSpecs) {
      if (name.matches(GrammarActions.getSourceFormatter().getName(type))) {
        matchedName = true;
        break;
      }
    }

    if (!matchedName) {
      return false;
    }

    if (inheritance != null && !inheritance.matches(type)) {
      return false;
    }

    return true;
  }

  public void setModifier(@CheckForNull ModifierSpecification modifier) {
    this.modifier = modifier;
  }

  @CheckForNull
  public ModifierSpecification getModifier() {
    return modifier;
  }

  public void add(@Nonnull NameSpecification nameSpecification) {
    nameSpecs.add(nameSpecification);
  }

  public void add(@Nonnull MethodSpecification methodSpecification) {
    methodSpecs.add(methodSpecification);
  }

  public void add(@Nonnull FieldSpecification fieldSpecification) {
    fieldSpecs.add(fieldSpecification);
  }

  public void setInheritance(@CheckForNull InheritanceSpecification inheritanceSpec) {
    this.inheritance = inheritanceSpec;
  }

  @Override
  @Nonnull
  public String toString() {
    StringBuilder sb = new StringBuilder();

    if (annotationType != null) {
      sb.append(annotationType);
      sb.append(' ');
    }

    if (modifier != null) {
      sb.append(modifier);
      sb.append(' ');
    }

    sb.append(classType);

    for (NameSpecification name : nameSpecs) {
      sb.append(' ');
      sb.append(name);
    }

    if (inheritance != null) {
      sb.append(' ');
      sb.append(inheritance.toString());
    }

    sb.append("\n{\n");
    for (FieldSpecification fieldSpec : fieldSpecs) {
      sb.append(fieldSpec);
      sb.append('\n');
    }

    for (MethodSpecification methodSpec : methodSpecs) {
      sb.append(methodSpec);
      sb.append('\n');
    }

    sb.append('}');

    return sb.toString();
  }
}
