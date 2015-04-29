/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.jack.shrob.shrink;

import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JPhantomClassOrInterface;
import com.android.sched.item.Description;
import com.android.sched.marker.DynamicValidOn;
import com.android.sched.marker.Marker;

import javax.annotation.Nonnull;

/**
 * Indicates that the type hierarchy is partial, {@link JDefinedClassOrInterface} contains reference
 * to types which does not belong to classpath.
 */
@Description("Indicates that the type hierarchy is partial.")
public class PartialTypeHierarchy implements Marker {

  @Nonnull
  private final JPhantomClassOrInterface unknownType;

  public PartialTypeHierarchy(@Nonnull JPhantomClassOrInterface unknownType) {
    this.unknownType = unknownType;
  }

  @DynamicValidOn
  public boolean isValidOn(@Nonnull JDefinedClassOrInterface type) {
    return !type.isExternal();
  }

  @Override
  public Marker cloneIfNeeded() {
    return this;
  }

  @Nonnull
  public JPhantomClassOrInterface getUnknownType() {
    return unknownType;
  }
}
