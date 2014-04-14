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

package com.android.jack.ir.naming;

import com.android.jack.ir.ast.JField;

import javax.annotation.Nonnull;

/**
 * An {@link AbstractName} referencing a field. This implementation is not thread-safe.
 * If multiple threads modify the referenced field, it must be synchronized externally.
 */
public class FieldName extends AbstractName {

  @Nonnull
  private final JField field;

  public FieldName(@Nonnull JField field) {
    this.field = field;
  }

  @Override
  @Nonnull
  public String toString() {
    return field.getName();
  }

  @Nonnull
  public JField getField() {
    return field;
  }
}
