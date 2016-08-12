/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.jack.scheduling.marker;

import com.android.jack.dx.dex.file.LazyCstIndexMap;
import com.android.jack.dx.io.ClassDef;
import com.android.jack.dx.io.DexBuffer;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;

import javax.annotation.Nonnull;

/**
 * A marker which contains information about prebuilt dex code of a class or interface.
 */
@Description("A marker which contains information about prebuilt dex code of a class or interface")
@ValidOn(JDefinedClassOrInterface.class)
public final class ImportedDexClassMarker implements Marker {

  @Nonnull
  private final LazyCstIndexMap indexMap;

  @Nonnull
  private final DexBuffer dexBuffer;

  @Nonnull
   private final ClassDef classDef;

  public ImportedDexClassMarker(@Nonnull LazyCstIndexMap indexMap,
      @Nonnull DexBuffer dexBuffer,
      @Nonnull ClassDef classDef) {
    this.indexMap = indexMap;
    this.dexBuffer = dexBuffer;
    this.classDef = classDef;
  }

  @Nonnull
  public LazyCstIndexMap getIndexMap() {
    return indexMap;
  }

  @Nonnull
  public DexBuffer getDexBuffer() {
    return dexBuffer;
  }

  @Nonnull
  public ClassDef getClassDef() {
    return classDef;
  }

  @Override
  @Nonnull
  public Marker cloneIfNeeded() {
    return this;
  }

}
