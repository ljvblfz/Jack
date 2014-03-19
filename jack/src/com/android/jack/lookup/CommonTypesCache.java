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

package com.android.jack.lookup;

import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JInterface;
import com.android.jack.ir.ast.JType;
import com.android.jack.lookup.CommonTypes.CommonType;

import javax.annotation.Nonnull;

class CommonTypesCache {

  private final JClassOrInterface[] commonTypes = new JClassOrInterface[CommonType.values().length];
  private final JClass[] commonClasses = new JClass[CommonType.values().length];
  private final JInterface[] commonInterfaces = new JInterface[CommonType.values().length];

  @Nonnull
  private final JLookup lookup;

  CommonTypesCache(@Nonnull JLookup lookup) {
    this.lookup = lookup;
  }

  public JClass getClass(@Nonnull CommonType type) {
    if (commonClasses[type.ordinal()] == null) {
      commonClasses[type.ordinal()] = lookup.getClass(type.getSignature());
    }
    return commonClasses[type.ordinal()];
  }

  public JInterface getInterface(@Nonnull CommonType type) {
    if (commonInterfaces[type.ordinal()] == null) {
      commonInterfaces[type.ordinal()] = lookup.getInterface(type.getSignature());
    }
    return commonInterfaces[type.ordinal()];
  }

  public JType getType(@Nonnull CommonType type) {
    if (commonTypes[type.ordinal()] == null) {
      commonTypes[type.ordinal()] = (JClassOrInterface) lookup.getType(type.getSignature());
    }
    return commonTypes[type.ordinal()];
  }

}
