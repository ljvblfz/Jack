/*
 * Copyright (C) 2007 The Android Open Source Project
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
package com.android.jack.dx.rop.cst;

import com.android.jack.dx.dex.file.DexFile;
import com.android.jack.dx.dex.file.IndexedItem;
import com.android.jack.dx.io.DexBuffer;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Maps {@link TypedConstant} index offsets from a dex file to those into another.
 */
public class CstIndexMap {

  /** Mapping between index and {@link CstString} value of a dex file.*/
  private final CstString[] strings;

  /** Mapping between index and {@link CstType} value of a dex file.*/
  private final CstType[] types;

  /** Mapping between index and {@link CstMethodRef} value of a dex file.*/
  private final CstMethodRef[] methods;

  /** Mapping between index and {@link CstFieldRef} value of a dex file.*/
  private final CstFieldRef[] fields;

  public CstIndexMap(DexBuffer dexBuffer) {
    strings = new CstString[dexBuffer.strings().size()];
    types = new CstType[dexBuffer.typeNames().size()];
    methods = new CstMethodRef[dexBuffer.methodIds().size()];
    fields = new CstFieldRef[dexBuffer.fieldIds().size()];
  }
  /**
   * Keeps string mapping of a dex file.
   * @param index String index.
   * @param cstString The string.
   */
  public void addStringMapping(@Nonnegative int index, CstString cstString) {
    assert strings[index] == null || strings[index].compareTo(cstString) == 0;

    if (strings[index] == null) {
      strings[index] = cstString;
    }
  }

  /**
   * Keeps type mapping of a dex file.
   * @param index Type index.
   * @param cstType The type.
   */
  public void addTypeMapping(@Nonnegative int index, CstType cstType) {
    assert types[index] == null || types[index].compareTo(cstType) == 0;

    if (types[index] == null) {
      types[index] = cstType;
    }
  }

  /**
   * Keeps method mapping of a dex file.
   * @param index Method index.
   * @param methodRef The method.
   */
  public void addMethodMapping(@Nonnegative int index, CstMethodRef methodRef) {
    assert methods[index] == null || methods[index].compareTo(methodRef) == 0;

    if (methods[index] == null) {
      methods[index] = methodRef;
    }
  }

  /**
   * Keeps field mapping of a dex file.
   * @param index Field index.
   * @param fieldRef The Field.
   */
  public void addFieldMapping(@Nonnegative int index, CstFieldRef fieldRef) {
    assert fields[index] == null || fields[index].compareTo(fieldRef) == 0;

    if (fields[index] == null) {
      fields[index] = fieldRef;
    }
  }

  /**
   * Merge all {@link TypedConstant} of one dex file into another.
   * @param dex The dex file where values are merged.
   */
  public void mergeConstantsIntoDexFile(DexFile dex) {
    for (CstString cst : strings) {
      dex.getStringIds().intern(cst);
    }

    for (CstBaseMethodRef cst : methods) {
      dex.getMethodIds().intern(cst);
    }

    for (CstFieldRef cst : fields) {
      dex.getFieldIds().intern(cst);
    }

    for (CstType cst : types) {
      dex.getTypeIds().intern(cst);
    }
  }


  /**
   * Return the remapped index of a {@code CstString} into {@code file}.
   * @param file The file where the remapped index apply to.
   * @param index The old index to remap into {@code file}.
   * @return The new index remapped into {@code file}.
   */
  public int getRemappedCstStringIndex(DexFile file, @Nonnegative int index) {
    IndexedItem indexedItem = file.findItemOrNull(strings[index]);
    assert indexedItem != null;
    return indexedItem.getIndex();
  }

  /**
   * Return the remapped index of a {@code CstType} into {@code file}.
   * @param file The file where the remapped index apply to.
   * @param index The old index to remap into {@code file}.
   * @return The new index remapped into {@code file}.
   */
  public int getRemappedCstTypeIndex(DexFile file, @Nonnegative int index) {
    IndexedItem indexedItem = file.findItemOrNull(types[index]);
    assert indexedItem != null;
    return indexedItem.getIndex();
  }

  /**
   * Return the remapped index of a {@code CstBaseMethodRef} into {@code file}.
   * @param file The file where the remapped index apply to.
   * @param index The old index to remap into {@code file}.
   * @return The new index remapped into {@code file}.
   */
  public int getRemappedCstBaseMethodRefIndex(DexFile file, @Nonnegative int index) {
    IndexedItem indexedItem = file.findItemOrNull(methods[index]);
    assert indexedItem != null;
    return indexedItem.getIndex();
  }

  /**
   * Return the remapped index of a {@code CstFieldRef} into {@code file}.
   * @param file The file where the remapped index apply to.
   * @param index The old index to remap into {@code file}.
   * @return The new index remapped into {@code file}.
   */
  public int getRemappedCstFieldRefIndex(DexFile file, @Nonnegative int index) {
    IndexedItem indexedItem = file.findItemOrNull(fields[index]);
    assert indexedItem != null;
    return indexedItem.getIndex();
  }

  @Nonnull
  public CstMethodRef getCstMethodRef(@Nonnegative int index) {
    CstMethodRef cstMethodRef = methods[index];
    assert cstMethodRef != null;
    return cstMethodRef;
  }

  @Nonnull
  public CstFieldRef getCstFieldRef(@Nonnegative int index) {
    CstFieldRef cstFieldRef = fields[index];
    assert cstFieldRef != null;
    return cstFieldRef;
  }

  @Nonnull
  public CstString getCstString(@Nonnegative int index) {
    CstString cstString = strings[index];
    assert cstString != null;
    return cstString;
  }

  @Nonnull
  public CstType getCstType(@Nonnegative int index) {
    CstType cstType = types[index];
    assert cstType != null;
    return cstType;
  }
}
