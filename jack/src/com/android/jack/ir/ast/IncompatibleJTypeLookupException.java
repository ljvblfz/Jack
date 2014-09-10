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

package com.android.jack.ir.ast;

import com.android.jack.Jack;

import javax.annotation.Nonnull;

/**
 * An {@code Exception} meaning that the lookup of a type found a result incompatible with the
 * request.
 */
public class IncompatibleJTypeLookupException extends JTypeLookupException {

  private static final long serialVersionUID = 1L;
  @Nonnull
  private final JType found;

  @Nonnull
  private final Class<? extends JClassOrInterface> expectedClass;

  public IncompatibleJTypeLookupException(
      @Nonnull JType found,
      @Nonnull Class<? extends JClassOrInterface> expectedClass) {
    super();
    this.found = found;
    this.expectedClass = expectedClass;
  }

  public IncompatibleJTypeLookupException(
      @Nonnull JDefinedClassOrInterface found,
      @Nonnull Class<? extends JClassOrInterface> expectedClass,
      @Nonnull Exception cause) {
    super(cause);
    this.found = found;
    this.expectedClass = expectedClass;
  }

  @Override
  @Nonnull
  public String getMessage() {
    return Jack.getUserFriendlyFormatter().getName(found) +
        " found as " + getTypeKind(found.getClass()) +
        " instead of " + getTypeKind(expectedClass);
  }

  @Nonnull
  private static String getTypeKind(@Nonnull Class<? extends JType> typeClass) {
    if (JEnum.class.isAssignableFrom(typeClass)) {
      return "enum";
    } else if (JAnnotation.class.isAssignableFrom(typeClass)) {
      return "annotation";
    } else if (JClass.class.isAssignableFrom(typeClass)) {
      return "class";
    } else if (JInterface.class.isAssignableFrom(typeClass)) {
      return "interface";
    } else {
      return "<undefined>";
    }
  }

}
