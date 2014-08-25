/*
 * Copyright (C) 2013 The Android Open Source Project
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
import com.android.sched.marker.LocalMarkerManager;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Description of a field.
 */
public class JFieldId extends LocalMarkerManager
  implements HasName, CanBeRenamed, HasType {

  @Nonnull
  private String name;
  @Nonnull
  private final JType type;
  @Nonnull
  private final FieldKind kind;
  @CheckForNull
  private JField field;

  public JFieldId(@Nonnull String name, @Nonnull JType type, @Nonnull FieldKind kind) {
    this.name = name;
    this.type = type;
    this.kind = kind;
  }

  public JFieldId(@Nonnull String name, @Nonnull JType type, @Nonnull FieldKind kind,
      @Nonnull JField field) {
    this(name, type, kind);
    this.field = field;
  }

  @Override
  @Nonnull
  public String getName() {
    return name;
  }

  @Override
  @Nonnull
  public JType getType() {
    return type;
  }

  @Override
  public void setName(@Nonnull String name) {
    this.name = name;
  }

  @Nonnull
  public FieldKind getKind() {
    return kind;
  }

  @Override
  public String toString() {
    return name + " : " + Jack.getUserFriendlyFormatter().getName(type);
  }

  @CheckForNull
  public JField getField() {
    return field;
  }

  public boolean equals(@Nonnull String otherName, @Nonnull JType otherType,
      @Nonnull FieldKind otherKind) {
    if ((this.kind != otherKind) || (!this.name.equals(otherName))) {
      return false;
    }
    return type.isSameType(otherType);
  }

  @Override
  public final int hashCode() {
    return super.hashCode();
  }

  @Override
  public final boolean equals(@CheckForNull Object obj) {
    return obj == this;
  }

}
