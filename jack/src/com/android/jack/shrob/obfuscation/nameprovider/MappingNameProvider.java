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

import java.util.Map;

import javax.annotation.Nonnull;

/**
 * A class that provide unique names using a mapping file
 */
public class MappingNameProvider implements NameProvider {

  @Nonnull
  private final NameProvider defaultNameProvider;

  @Nonnull
  private final Map<String, String> names;

  public MappingNameProvider(
      @Nonnull NameProvider defaultNameProvider, @Nonnull Map<String, String> names) {
    this.defaultNameProvider = defaultNameProvider;
    this.names = names;
  }

  @Override
  @Nonnull
  public String getNewName(@Nonnull String oldName) {
    String newName = names.get(oldName);
    if (newName != null) {
      return newName;
    }
    return defaultNameProvider.getNewName(oldName);
  }
}