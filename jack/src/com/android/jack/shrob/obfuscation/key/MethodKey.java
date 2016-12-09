/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.jack.shrob.obfuscation.key;

import com.android.jack.ir.ast.JMethodIdWide;
import com.android.jack.ir.ast.JType;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * A {@link Key} used to identify methods for renaming purposes.
 */
public class MethodKey extends Key {

  @Nonnull
  private final String name;

  @Nonnull
  private final List<JType> parameterTypes;

  public MethodKey(@Nonnull JMethodIdWide methodId) {
    this(methodId.getName(), methodId.getParamTypes());
  }

  public MethodKey(@Nonnull String name, @Nonnull List<JType> parameterTypes) {
    this.name = name;
    this.parameterTypes = parameterTypes;
  }

  @Nonnull
  public String getName() {
    return name;
  }

  @Nonnull
  public List<JType> getParameterTypes() {
    return parameterTypes;
  }

  @Override
  public final int hashCode() {
    return name.hashCode() ^ parameterTypes.hashCode();
  }

  @Override
  public final boolean equals(Object obj) {
    if (obj instanceof MethodKey) {
      MethodKey toCompare = (MethodKey) obj;
      return toCompare.name.equals(name) && toCompare.parameterTypes.equals(parameterTypes);
    }
    return false;
  }
}
