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

package com.android.jack.ecj.loader.jast;

import org.eclipse.jdt.internal.compiler.env.IBinaryElementValuePair;

import javax.annotation.Nonnull;

/**
 * A {@code IBinaryElementValuePair} for Jack.
 */
class JAstBinaryElementValuePair implements IBinaryElementValuePair {

  @Nonnull
  private final char[] name;

  @Nonnull
  private final Object value;

  JAstBinaryElementValuePair(@Nonnull char[] name, @Nonnull Object value) {
    this.name = name;
    this.value = value;
  }

  /**
   * {@inheritDoc}
   */
  @Nonnull
  @Override
  public char[] getName() {
    return name;
  }

  /**
   * {@inheritDoc}
   */
  @Nonnull
  @Override
  public Object getValue() {
    return value;
  }

  @Nonnull
  @Override
  public String toString() {
    return "name" + ": " + value;
  }

}
