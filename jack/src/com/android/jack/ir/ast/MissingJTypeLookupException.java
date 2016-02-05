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
 * An {@code Exception} meaning that the lookup of a type failed to find the required type.
 */
public class MissingJTypeLookupException extends JTypeLookupException {

  private static final long serialVersionUID = 1L;
  @Nonnull
  private final String typeSignature;

  public MissingJTypeLookupException(@Nonnull String typeSignature) {
    super();
    this.typeSignature = typeSignature;
  }

  public MissingJTypeLookupException(@Nonnull String typeSignature, @Nonnull Exception cause) {
    super(cause);
    this.typeSignature = typeSignature;
  }

  public MissingJTypeLookupException(@Nonnull JPackage pack, @Nonnull String typeName) {
    super();
    typeSignature = Jack.getLookupFormatter().getName(pack, typeName);
  }

  public MissingJTypeLookupException(@Nonnull JPhantomClassOrInterface phantomType) {
    super();
    typeSignature = Jack.getUserFriendlyFormatter().getName(phantomType);
  }

  @Override
  @Nonnull
  public String getMessage() {
    return typeSignature + " not found";
  }

}
