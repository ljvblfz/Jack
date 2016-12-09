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

package com.android.jack.shrob.obfuscation.nameprovider;

import com.android.jack.shrob.obfuscation.key.Key;

import java.util.Set;

import javax.annotation.Nonnull;

/**
 * A class that provide unique names
 */
public class UniqueNameProvider implements NameProvider {

  @Nonnull
  private final Set<String> names;

  @Nonnull
  private final NameProvider nameProvider;

  public UniqueNameProvider(
      @Nonnull NameProvider nameProvider, @Nonnull Set<String> existingNames) {
    this.nameProvider = nameProvider;
    this.names = existingNames;
  }

  @Override
  @Nonnull
  public String getNewName(@Nonnull Key key) {
    String newName = nameProvider.getNewName(key);
    while (names.contains(newName)) {
      String candidate = nameProvider.getNewName(key);
      assert !candidate.equals(newName);
      newName = candidate;
    }
    names.add(newName);
    return newName;
  }

  @Override
  public boolean hasAlternativeName(@Nonnull Key key) {
    return nameProvider.hasAlternativeName(key);
  }
}
