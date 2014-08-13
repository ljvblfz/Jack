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

import java.util.regex.Pattern;

import javax.annotation.Nonnull;

/**
 * Compiled name pattern.
 */
public class NamePattern {

  @Nonnull
  private final Pattern pattern;

  @Nonnull
  private final String rawPattern;

  public NamePattern(@Nonnull String rawPattern) {
    this.rawPattern = rawPattern;
    String regExp = rawPattern.replace(".", "\\.").replace("*", ".*");

    this.pattern = Pattern.compile(regExp);
  }

  public boolean matches(@Nonnull String name) {
    return pattern.matcher(name).matches();
  }

  @Nonnull
  @Override
  public String toString() {
    return rawPattern;
  }

}
