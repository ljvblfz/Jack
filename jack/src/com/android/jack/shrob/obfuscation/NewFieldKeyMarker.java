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

package com.android.jack.shrob.obfuscation;

import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.shrob.obfuscation.key.FieldKey;
import com.android.sched.item.Description;
import com.android.sched.marker.ValidOn;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * {@code Marker} that represents the new names of fields in a type.
 */
@Description("Represents the new names of fields in a type.")
@ValidOn(JDefinedClassOrInterface.class)
public class NewFieldKeyMarker extends NewKeyMarker {
  @Nonnull
  private final Set<FieldKey> newFieldKeys;

  public NewFieldKeyMarker() {
    newFieldKeys = new HashSet<FieldKey>();
  }

  public NewFieldKeyMarker(@Nonnull Set<FieldKey> existingKeys) {
    this.newFieldKeys = existingKeys;
  }

  public void add(@Nonnull FieldKey key) {
    newFieldKeys.add(key);
  }

  @Override
  @Nonnull
  public Collection<FieldKey> getNewKeys() {
    return newFieldKeys;
  }

}