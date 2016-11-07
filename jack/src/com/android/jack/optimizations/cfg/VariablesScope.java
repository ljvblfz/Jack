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

package com.android.jack.optimizations.cfg;

import com.android.sched.util.HasDescription;
import com.android.sched.util.codec.VariableName;

import javax.annotation.Nonnull;

/** Defines the variable scope (i.e. a set of the variables) relevant to a context */
@VariableName("scope")
public enum VariablesScope implements HasDescription {
  NONE("does not apply to any variables"),
  SYNTHETIC("only applies to synthetic variables"),
  ALL("applies to all variables");

  @Nonnull
  private final String description;

  VariablesScope(@Nonnull String description) {
    this.description = description;
  }

  @Override
  @Nonnull
  public String getDescription() {
    return description;
  }
}
