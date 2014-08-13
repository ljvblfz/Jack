/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.jack.annotationadder;

import java.util.Collection;

import javax.annotation.Nonnull;

/**
 * A rule in an annotation adder file.
 */
public class Rule {

  @Nonnull
  private final String name;

  @Nonnull
  private final Expression<Collection<?>, Scope> set;

  public Rule(@Nonnull String name, @Nonnull Expression<Collection<?>, Scope> set) {
    this.name = name;
    this.set = set;
  }

  @Nonnull
  public String getName() {
    return name;
  }

  @Nonnull
  public Expression<Collection<?>, Scope> getSet() {
    return set;
  }

}
