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

package com.android.jack.lookup;

import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JInterface;
import com.android.jack.ir.ast.JType;
import com.android.jack.lookup.CommonTypes.CommonType;

import javax.annotation.Nonnull;

/**
 * Common interface for {@code NNode} and {@code JNode} lookup.
 */
public interface NodeLookup {

  /**
   * Find a {@link JType} from his name.
   *
   * @param signature Name of the searched type. The type name must have the following form
   *        Ljava/jang/String;.
   * @return The {@link JType} found.
   */
  @Nonnull
  public JType getType(@Nonnull String signature) throws JLookupException;

  @Nonnull
  public JClass getClass(@Nonnull String signature) throws JLookupException;

  @Nonnull
  public JInterface getInterface(@Nonnull String signature) throws JLookupException;

  @Nonnull
  public JType getType(@Nonnull CommonType type) throws JLookupException;

  @Nonnull
  public JClass getClass(@Nonnull CommonType type) throws JLookupException;

  @Nonnull
  public JInterface getInterface(@Nonnull CommonType type) throws JLookupException;

}
