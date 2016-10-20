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

import com.android.jack.ir.ast.JFieldId;
import com.android.jack.ir.ast.JType;

import javax.annotation.Nonnull;

/**
 * A {@link Key} used to identify fields for renaming purposes.
 */
public class FieldKey extends Key {

  @Nonnull
  private final String name;

  @Nonnull
  private final JType type;

  public FieldKey(@Nonnull JFieldId fieldId) {
    this(fieldId.getName(), fieldId.getType());
  }

  public FieldKey(@Nonnull String name, @Nonnull JType type) {
    this.name = name;
    this.type = type;
  }

  @Nonnull
  public String getName() {
    return name;
  }

  @Nonnull
  public JType getType() {
    return type;
  }

  @Override
  public final int hashCode() {
    return name.hashCode() ^ type.hashCode();
  }

  @Override
  public final boolean equals(Object obj) {
    if (obj instanceof FieldKey) {
      FieldKey toCompare = (FieldKey) obj;
      return toCompare.name.equals(name) && toCompare.type.equals(type);
    }
    return false;
  }
}
