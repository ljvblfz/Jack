/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.android.jack.shrob.obfuscation;

import com.android.jack.ir.ast.JPackage;
import com.android.jack.shrob.obfuscation.key.TypeKey;
import com.android.sched.item.Description;
import com.android.sched.marker.ValidOn;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * {@code Marker} that represents the new names of types in a package.
 */
@Description("Represents the new names of types in a package.")
@ValidOn(JPackage.class)
public class NewTypeKeyMarker extends NewKeyMarker {
  @Nonnull
  private final Set<TypeKey> newTypeKeys;

  public NewTypeKeyMarker() {
    newTypeKeys = new HashSet<TypeKey>();
  }

  public NewTypeKeyMarker(@Nonnull Set<TypeKey> existingKeys) {
    this.newTypeKeys = existingKeys;
  }

  public void add(@Nonnull TypeKey key) {
    newTypeKeys.add(key);
  }

  @Override
  @Nonnull
  public Collection<TypeKey> getNewKeys() {
    return newTypeKeys;
  }

}