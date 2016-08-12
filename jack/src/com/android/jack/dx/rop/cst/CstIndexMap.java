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
import com.android.jack.dx.io.MethodHandleId;
import com.android.jack.dx.io.TypeList;
import com.android.jack.dx.rop.cst.CstMethodHandleRef.MethodHandleKind;
import com.android.jack.dx.rop.type.StdTypeList;
import com.android.jack.dx.rop.type.Type;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Maps {@link TypedConstant} index offsets from a dex file to those into another.
 */
public class CstIndexMap {

  /** Mapping between index and {@link CstString} value of a dex file.*/
  @Nonnull
  protected final CstString[] strings;

  /** Mapping between index and {@link Type} value of a dex file.*/
  @Nonnull
  protected final Type[] types;

  /** Mapping between index and {@link CstMethodRef} value of a dex file.*/
  @Nonnull
  protected final CstMethodRef[] methods;

  /** Mapping between index and {@link CstFieldRef} value of a dex file.*/
  @Nonnull
  protected final CstFieldRef[] fields;

  /** Mapping between index and {@link CstPrototypeRef} value of a dex file. */
  @Nonnull
  protected final CstPrototypeRef[] prototypes;

  /** Mapping between index and {@link CstMethodHandleRef} value of a dex file. */
  @Nonnull
  protected final CstMethodHandleRef[] methodHandles;

  /** Mapping between index and {@link CstCallSiteRef} value of a dex file. */
  protected final CstCallSiteRef[] callSites;

  public CstIndexMap(@Nonnull DexBuffer dexBuffer) {
    strings = new CstString[dexBuffer.strings().size()];
    types = new Type[dexBuffer.typeNames().size()];
    methods = new CstMethodRef[dexBuffer.methodIds().size()];
    fields = new CstFieldRef[dexBuffer.fieldIds().size()];
    prototypes = new CstPrototypeRef[dexBuffer.protoIds().size()];
    methodHandles = new CstMethodHandleRef[dexBuffer.methodHandleIds().size()];
    callSites = new CstCallSiteRef[dexBuffer.callSiteIds().size()];
  }
  /**
   * Keeps string mapping of a dex file.
   * @param index String index.
   * @param cstString The string.
   */
  public void addStringMapping(@Nonnegative int index, @Nonnull CstString cstString) {
    assert strings[index] == null || strings[index].compareTo(cstString) == 0;

    if (strings[index] == null) {
      strings[index] = cstString;
    }
  }

  /**
   * Keeps type mapping of a dex file.
   * @param index Type index.
   * @param type The type.
   */
  public void addTypeMapping(@Nonnegative int index, @Nonnull Type type) {
    assert types[index] == null || types[index].compareTo(type) == 0;

    if (types[index] == null) {
      types[index] = type;
    }
  }

  /**
   * Keeps method mapping of a dex file.
   * @param index Method index.
   * @param methodRef The method.
   */
  public void addMethodMapping(@Nonnegative int index, @Nonnull CstMethodRef methodRef) {
    assert methods[index] == null || methods[index].compareTo(methodRef) == 0;

    if (methods[index] == null) {
      methods[index] = methodRef;
    }
  }

  /**
   * Keeps prototype mapping of a dex file.
   * @param index Protototype index.
   * @param prototypeRef The prototype.
   */
  public void addPrototypeMapping(@Nonnegative int index, @Nonnull CstPrototypeRef prototypeRef) {
    assert prototypes[index] == null || prototypes[index].compareTo(prototypeRef) == 0;

    if (prototypes[index] == null) {
      prototypes[index] = prototypeRef;
    }
  }

  /**
   * Keeps field mapping of a dex file.
   * @param index Field index.
   * @param fieldRef The Field.
   */
  public void addFieldMapping(@Nonnegative int index, @Nonnull CstFieldRef fieldRef) {
    assert fields[index] == null || fields[index].compareTo(fieldRef) == 0;

    if (fields[index] == null) {
      fields[index] = fieldRef;
    }
  }

  /**
   * Keeps method handle mapping of a dex file.
   * @param index Method handle index.
   * @param methodHandleRef The method handle.
   */
  public void addMethodHandleMapping(@Nonnegative int index,
      @Nonnull CstMethodHandleRef methodHandleRef) {
    assert methodHandles[index] == null || methodHandles[index].compareTo(methodHandleRef) == 0;

    if (methodHandles[index] == null) {
      methodHandles[index] = methodHandleRef;
    }
  }

  /**
   * Keeps call site mapping of a dex file.
   * @param index Call site index.
   * @param callSiteRef The call site.
   */
  public void addCallSiteMapping(@Nonnegative int index, @Nonnull CstCallSiteRef callSiteRef) {
    assert callSites[index] == null || callSites[index].compareTo(callSiteRef) == 0;

    if (callSites[index] == null) {
      callSites[index] = callSiteRef;
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

  /**
   * Return the remapped index of a {@code CstPrototypeRef} into {@code file}.
   * @param file The file where the remapped index apply to.
   * @param index The old index to remap into {@code file}.
   * @return The new index remapped into {@code file}.
   */
  public int getRemappedCstPrototypeRefIndex(DexFile file, @Nonnegative int index) {
    IndexedItem indexedItem = file.findItemOrNull(prototypes[index]);
    assert indexedItem != null;
    return indexedItem.getIndex();
  }

  /**
   * Return the remapped index of a {@code CstCallSiteRef} into {@code file}.
   * @param file The file where the remapped index apply to.
   * @param index The old index to remap into {@code file}.
   * @return The new index remapped into {@code file}.
   */
  public int getRemappedCstCallSiteRefIndex(DexFile file, @Nonnegative int index) {
    IndexedItem indexedItem = file.findItemOrNull(callSites[index]);
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
  public Type getType(@Nonnegative int index) {
    Type cstType = types[index];
    assert cstType != null;
    return cstType;
  }

  @Nonnull
  public CstPrototypeRef getCstPrototype(@Nonnegative int index) {
    CstPrototypeRef cstPrototypeRef = prototypes[index];
    assert cstPrototypeRef != null;
    return cstPrototypeRef;
  }

  @Nonnull
  public CstMethodHandleRef getCstMethodHandle(@Nonnegative int index) {
    CstMethodHandleRef cstMethodHandleRef = methodHandles[index];
    assert cstMethodHandleRef != null;
    return cstMethodHandleRef;
  }

  @Nonnull
  public CstCallSiteRef getCstCallSite(@Nonnegative int index) {
    CstCallSiteRef cstCallSite = callSites[index];
    assert cstCallSite != null;
    return cstCallSite;
  }

  @Nonnull
  public CstCallSiteRef[] getCstCallSitesType() {
    return callSites;
  }
  @Nonnull
  public StdTypeList getStdTypeList(@Nonnull TypeList typeList) {
    short[] type = typeList.getTypes();
    int typesLength = type.length;
    StdTypeList stdTypeList = new StdTypeList(typesLength);
    for (int i = 0; i < typesLength; i++) {
      stdTypeList.set(i, getType(type[i]));
    }
    stdTypeList.setImmutable();
    return stdTypeList;
  }

  @Nonnull
  public CstMethodHandleRef getCstMethodHandleRef(@Nonnull MethodHandleId methodHandleId) {
    MethodHandleKind kind = methodHandleId.getKind();
    CstMethodHandleRef cstMethodHandleRef;

    switch (kind) {
      case PUT_INSTANCE:
      case PUT_STATIC:
      case GET_INSTANCE:
      case GET_STATIC: {
        cstMethodHandleRef = new CstMethodHandleRef(kind,
            getCstFieldRef(methodHandleId.getMemberIndex()));
        break;
      }
      case INVOKE_CONSTRUCTOR:
      case INVOKE_INSTANCE:
      case INVOKE_STATIC: {
        cstMethodHandleRef = new CstMethodHandleRef(kind,
            getCstMethodRef(methodHandleId.getMemberIndex()));
        break;
      }
      default:
        throw new AssertionError();
    }
    return cstMethodHandleRef;
  }
}
