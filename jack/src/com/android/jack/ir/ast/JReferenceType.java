/*
 * Copyright 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.android.jack.ir.ast;

import com.android.sched.item.Description;

import javax.annotation.Nonnull;

/**
 * Any java reference type.
 */
@Description("Reference type")
public interface JReferenceType extends JType {

  /**
   * Check if an instance of {@code JReferenceType} can be upcast to {@code castTo}.
   * True means that the cast can be done, but false does not mean that the cast is not
   * possible but only that it is unknown due to {@code JPhantomClassOrInterface} instances.
   * @param castTo The {@code JReferenceType} we want to cast to.
   * @return true if this instance can be upcast to {@code castTo} or false otherwise.
   */
  public abstract boolean canBeSafelyUpcast(@Nonnull JReferenceType castTo);
}
