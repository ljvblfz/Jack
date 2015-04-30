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

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

import com.android.jack.Jack;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JPhantomClassOrInterface;
import com.android.jack.reporting.Reportable;
import com.android.sched.item.Description;
import com.android.sched.marker.DynamicValidOn;
import com.android.sched.marker.Marker;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * Indicates that the type hierarchy is partial, {@link JDefinedClassOrInterface} contains
 * references to types which does not belong to classpath.
 */
@Description("Indicates that the type hierarchy is partial.")
public class PartialTypeHierarchy implements Marker, Reportable {

  @Nonnull
  private static final Joiner typeNameJoiner = Joiner.on(", ");

  @Nonnull
  private final List<JPhantomClassOrInterface> unknownTypes;

  @Nonnull
  private final JDefinedClassOrInterface definedType;

  public PartialTypeHierarchy(@Nonnull JDefinedClassOrInterface definedType,
      @Nonnull List<JPhantomClassOrInterface> unknownTypes) {
    this.definedType = definedType;
    this.unknownTypes = unknownTypes;
  }

  @DynamicValidOn
  public boolean isValidOn(@Nonnull JDefinedClassOrInterface type) {
    return !type.isExternal();
  }

  @Override
  public Marker cloneIfNeeded() {
    return this;
  }

  @Override
  @Nonnull
  public String getMessage() {
    return "Shrinking: force to keep members of '"
        + Jack.getUserFriendlyFormatter().getName(definedType)
        + "' due to unknown referenced types "
        + typeNameJoiner.join(
            Iterables.transform(unknownTypes, new Function<JPhantomClassOrInterface, String>() {
              @Override
              public String apply(JPhantomClassOrInterface arg0) {
                return Jack.getUserFriendlyFormatter().getName(arg0);
              }
            }));
  }

  @Override
  @Nonnull
  public ProblemLevel getDefaultProblemLevel() {
    return ProblemLevel.WARNING;
  }
}
