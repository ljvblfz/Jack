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

package com.android.jack.shrob.obfuscation.nameprovider;

import javax.annotation.Nonnull;

/**
 * Class generating names in alphabetical order
 */
public abstract class AlphabeticalNameProvider implements NameProvider {
  @Nonnull
  private final StringBuilder sb = new StringBuilder();

  protected abstract boolean hasNextChar(char c);

  protected abstract char nextChar(char c);

  @Override
  @Nonnull
  public String getNewName(@Nonnull String oldName) {
    for (int index = sb.length() - 1; index >= 0; index--) {
      char c = sb.charAt(index);
      if (!hasNextChar(c)) {
        sb.setCharAt(index, getFirstChar());
      } else {
        sb.setCharAt(index, nextChar(c));
        return sb.toString();
      }
    }
    sb.insert(0, getFirstChar());
    return sb.toString();
  }

  protected abstract char getFirstChar();

  @Override
  public boolean hasAlternativeName(@Nonnull String oldName) {
    return true;
  }
}
