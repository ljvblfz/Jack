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

import com.android.jack.ir.ast.HasName;
import com.android.jack.ir.ast.JFieldId;
import com.android.jack.ir.ast.JMethodIdWide;
import com.android.jack.ir.ast.JPackage;
import com.android.jack.ir.ast.JType;

import javax.annotation.Nonnull;

/**
 * A key used to identify nodes for renaming purposes.
 */
public abstract class Key {
  @Nonnull
  public static Key getKey(@Nonnull HasName namedElement) {
    if (namedElement instanceof JFieldId) {
      return new FieldKey((JFieldId) namedElement);
    } else if (namedElement instanceof JMethodIdWide) {
      return new MethodKey((JMethodIdWide) namedElement);
    } else if (namedElement instanceof JType) {
      return new TypeKey((JType) namedElement);
    } else if (namedElement instanceof JPackage) {
      return new PackageKey((JPackage) namedElement);
    } else {
      throw new AssertionError();
    }
  }
}
