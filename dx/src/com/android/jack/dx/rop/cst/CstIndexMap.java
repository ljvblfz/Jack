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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Maps {@link TypedConstant} index offsets from a dex file to those into another.
 */
public class CstIndexMap {

  /** Mapping between index and {@link CstString} value of a dex file.*/
  private final Map<Integer, CstString> stringsIndexMap = new HashMap<Integer, CstString>();

  /** Mapping between index and {@link CstType} value of a dex file.*/
  private final Map<Integer, CstType> typesIndexMap = new HashMap<Integer, CstType>();

  /** Mapping between index and {@link CstBaseMethodRef} value of a dex file.*/
  private final Map<Integer, CstBaseMethodRef> methodsIndexMap =
      new HashMap<Integer, CstBaseMethodRef>();

  /** Mapping between index and {@link CstFieldRef} value of a dex file.*/
  private final Map<Integer, CstFieldRef> fieldsIndexMap = new HashMap<Integer, CstFieldRef>();

  /**
   * Keeps string mapping of a dex file.
   * @param index String index.
   * @param cstString The string.
   */
  public void addStringMapping(int index, CstString cstString) {
    Integer key = new Integer(index);
    assert index >= 0;
    assert stringsIndexMap.get(key) == null || stringsIndexMap.get(key).compareTo(cstString) == 0;

    if (!stringsIndexMap.containsKey(key)) {
      stringsIndexMap.put(key, cstString);
    }
  }

  /**
   * Keeps type mapping of a dex file.
   * @param index Type index.
   * @param cstType The type.
   */
  public void addTypeMapping(int index, CstType cstType) {
    Integer key = new Integer(index);
    assert index >= 0;
    assert typesIndexMap.get(key) == null || typesIndexMap.get(key).compareTo(cstType) == 0;

    if (!typesIndexMap.containsKey(key)) {
      typesIndexMap.put(key, cstType);
    }
  }

  /**
   * Keeps method mapping of a dex file.
   * @param index Method index.
   * @param methodRef The method.
   */
  public void addMethodMapping(int index, CstBaseMethodRef methodRef) {
    Integer key = new Integer(index);
    assert index >= 0;
    assert methodsIndexMap.get(key) == null || methodsIndexMap.get(key).compareTo(methodRef) == 0;

    if (!methodsIndexMap.containsKey(key)) {
      methodsIndexMap.put(key, methodRef);
    }
  }

  /**
   * Keeps field mapping of a dex file.
   * @param index Field index.
   * @param fieldRef The Field.
   */
  public void addFieldMapping(int index, CstFieldRef fieldRef) {
    Integer key = new Integer(index);
    assert index >= 0;
    assert fieldsIndexMap.get(key) == null || fieldsIndexMap.get(key).compareTo(fieldRef) == 0;

    if (!fieldsIndexMap.containsKey(key)) {
      fieldsIndexMap.put(key, fieldRef);
    }
  }

  /**
   * Merge all {@link TypedConstant} of one dex file into another.
   * @param dex The dex file where values are merged.
   */
  public void mergeConstantsIntoDexFile(DexFile dex) {
    for (CstString cst : stringsIndexMap.values()) {
      dex.getStringIds().intern(cst);
    }

    for (CstBaseMethodRef cst : methodsIndexMap.values()) {
      dex.getMethodIds().intern(cst);
    }

    for (CstFieldRef cst : fieldsIndexMap.values()) {
      dex.getFieldIds().intern(cst);
    }

    for (CstType cst : typesIndexMap.values()) {
      dex.getTypeIds().intern(cst);
    }
  }


  /**
   * Return the remapped index of a {@code CstString} into {@code file}.
   * @param file The file where the remapped index apply to.
   * @param index The old index to remap into {@code file}.
   * @return The new index remapped into {@code file}.
   */
  public int getRemappedCstStringIndex(DexFile file, int index) {
    Integer key = new Integer(index);
    assert index >= 0;
    assert stringsIndexMap.containsKey(key);
    IndexedItem indexedItem = file.findItemOrNull(stringsIndexMap.get(key));
    assert indexedItem != null;
    return indexedItem.getIndex();
  }

  /**
   * Return the remapped index of a {@code CstType} into {@code file}.
   * @param file The file where the remapped index apply to.
   * @param index The old index to remap into {@code file}.
   * @return The new index remapped into {@code file}.
   */
  public int getRemappedCstTypeIndex(DexFile file, int index) {
    Integer key = new Integer(index);
    assert index >= 0;
    assert typesIndexMap.containsKey(key);
    IndexedItem indexedItem = file.findItemOrNull(typesIndexMap.get(key));
    assert indexedItem != null;
    return indexedItem.getIndex();
  }

  /**
   * Return the remapped index of a {@code CstBaseMethodRef} into {@code file}.
   * @param file The file where the remapped index apply to.
   * @param index The old index to remap into {@code file}.
   * @return The new index remapped into {@code file}.
   */
  public int getRemappedCstBaseMethodRefIndex(DexFile file, int index) {
    Integer key = new Integer(index);
    assert index >= 0;
    assert methodsIndexMap.containsKey(key);
    IndexedItem indexedItem = file.findItemOrNull(methodsIndexMap.get(key));
    assert indexedItem != null;
    return indexedItem.getIndex();
  }

  /**
   * Return the remapped index of a {@code CstFieldRef} into {@code file}.
   * @param file The file where the remapped index apply to.
   * @param index The old index to remap into {@code file}.
   * @return The new index remapped into {@code file}.
   */
  public int getRemappedCstFieldRefIndex(DexFile file, int index) {
    Integer key = new Integer(index);
    assert index >= 0;
    assert fieldsIndexMap.containsKey(key);
    IndexedItem indexedItem = file.findItemOrNull(fieldsIndexMap.get(key));
    assert indexedItem != null;
    return indexedItem.getIndex();
  }

  /**
   * Returns all strings that must be remapped.
   * @return All string to remap.
   */
  public Collection<CstString> getStrings() {
    return (stringsIndexMap.values());
  }
}
