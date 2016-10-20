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

import com.android.jack.ir.ast.JPackage;

import javax.annotation.Nonnull;

/**
 * A {@link Key} used to identify packages for renaming purposes.
 */
public class PackageKey extends Key {

  @Nonnull
  private final String name;

  public PackageKey(@Nonnull JPackage pack) {
    this(pack.getName());
  }

  public PackageKey(@Nonnull String name) {
    this.name = name;
  }

  @Nonnull
  public String getName() {
    return name;
  }

  @Override
  public final int hashCode() {
    return name.hashCode();
  }

  @Override
  public final boolean equals(Object obj) {
    if (obj instanceof PackageKey) {
      PackageKey toCompare = (PackageKey) obj;
      return toCompare.name.equals(name);
    }
    return false;
  }
}
