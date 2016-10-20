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
import com.android.jack.ir.ast.JMethodIdWide;
import com.android.jack.shrob.obfuscation.key.MethodKey;
import com.android.sched.item.Description;
import com.android.sched.marker.ValidOn;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * {@code Marker} that represents the existing method's signature in a type.
 */
@Description("Represents the existing method's signature in a type.")
@ValidOn(JDefinedClassOrInterface.class)
public class NewMethodKeyMarker extends NewKeyMarker {

  @Nonnull
  public Map<JMethodIdWide, MethodKey> newMethodKeys;

  public NewMethodKeyMarker() {
    newMethodKeys = new HashMap<JMethodIdWide, MethodKey>();
  }

  public NewMethodKeyMarker(@Nonnull Map<JMethodIdWide, MethodKey> newMethodKeys) {
    this.newMethodKeys = newMethodKeys;
  }

  public void add(@Nonnull JMethodIdWide methodId, @Nonnull MethodKey newKey) {
    newMethodKeys.put(methodId, newKey);
  }

  @Nonnull
  public MethodKey getNewKey(@Nonnull JMethodIdWide id) {
    return newMethodKeys.get(id);
  }

  @Override
  @Nonnull
  public Collection<MethodKey> getNewKeys() {
    return newMethodKeys.values();
  }
}
