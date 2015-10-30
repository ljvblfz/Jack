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

package com.android.jack.shrob.spec;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

/**
 * Class representing the name of a field, method or class in a {@code class specification}.
 */
public class NameSpecification extends SpecificationWithNegator<String> {
  @Nonnull
  private final Pattern name;

  public NameSpecification(@Nonnull Pattern name) {
    this.name = name;
  }

  public NameSpecification(@Nonnull Pattern name, boolean negator) {
    this.name = name;
    setNegator(negator);
  }

  @Override
  protected boolean matchesWithoutNegator(@Nonnull String t) {
    Matcher matcher = name.matcher(t);
    return matcher.find();
  }

  @Override
  public String toString() {
    return super.toString() + name.toString();
  }
}