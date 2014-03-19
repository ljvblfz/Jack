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

import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedEnum;
import com.android.jack.ir.ast.JDefinedInterface;
import com.android.jack.ir.ast.JType;

import javax.annotation.Nonnull;

/**
 * Class representing the declaredType type (interface, class or enum) in
 * a {@code class specification}
 */
public class ClassTypeSpecification extends SpecificationWithNegator<JType> {

  @Nonnull
  private final TypeEnum type;

  /**
   * Enum indicating whether the type in a interface, class or enum
   */
  public enum TypeEnum {
    INTERFACE, CLASS, ENUM
  }

  public ClassTypeSpecification(@Nonnull TypeEnum type) {
    this.type = type;
  }

  public ClassTypeSpecification(@Nonnull TypeEnum type, boolean negator) {
    this.type = type;
    this.setNegator(negator);
  }

  @Override
  protected boolean matchesWithoutNegator(@Nonnull JType t) {
    switch (type) {
      case INTERFACE:
        return t instanceof JDefinedInterface;
      case CLASS:
        return t instanceof JDefinedClass || t instanceof JDefinedInterface;
      case ENUM:
        return t instanceof JDefinedEnum;
    }
    return true;
  }

  @Override
  @Nonnull
  public String toString() {
    StringBuilder sb = new StringBuilder(super.toString());

    switch (type) {
      case CLASS:
        sb.append("class");
        break;
      case ENUM:
        sb.append("enum");
        break;
      case INTERFACE:
        sb.append("interface");
        break;
    }
    return sb.toString();
  }
}
