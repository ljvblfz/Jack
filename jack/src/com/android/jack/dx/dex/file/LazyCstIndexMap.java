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

package com.android.jack.dx.dex.file;

import com.android.jack.dx.io.DexBuffer;
import com.android.jack.dx.io.FieldId;
import com.android.jack.dx.io.MethodId;
import com.android.jack.dx.io.ProtoId;
import com.android.jack.dx.rop.cst.CstCallSiteRef;
import com.android.jack.dx.rop.cst.CstFieldRef;
import com.android.jack.dx.rop.cst.CstIndexMap;
import com.android.jack.dx.rop.cst.CstMethodHandleRef;
import com.android.jack.dx.rop.cst.CstMethodRef;
import com.android.jack.dx.rop.cst.CstPrototypeRef;
import com.android.jack.dx.rop.cst.CstString;
import com.android.jack.dx.rop.type.Prototype;
import com.android.jack.dx.rop.type.StdTypeList;
import com.android.jack.dx.rop.type.Type;

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Auto loading index map, creating only used constants.
 */
public class LazyCstIndexMap extends CstIndexMap {

  @Nonnull
  private final DexBuffer dexBuffer;

  public LazyCstIndexMap(@Nonnull DexBuffer dexBuffer) {
    super(dexBuffer);
    this.dexBuffer = dexBuffer;
  }

  @Override
  @Nonnull
  public CstMethodRef getCstMethodRef(@Nonnegative int index) {
    CstMethodRef cstMethodRef = methods[index];
    if (cstMethodRef == null) {
      MethodId id = dexBuffer.methodIds().get(index);
      Type definingClass = getType(id.getDeclaringClassIndex());
      CstString name = getCstString(id.getNameIndex());
      Prototype prototype = getPrototype(id.getProtoIndex());
      cstMethodRef = new CstMethodRef(definingClass, name, prototype);
      methods[index] = cstMethodRef;
    }
    return cstMethodRef;
  }

  @Override
  @Nonnull
  public CstFieldRef getCstFieldRef(@Nonnegative int index) {
    CstFieldRef cstFieldRef = fields[index];
    if (cstFieldRef == null) {
      FieldId id = dexBuffer.fieldIds().get(index);
      Type definingClass = getType(id.getDeclaringClassIndex());
      cstFieldRef = new CstFieldRef(definingClass, getCstString(id.getNameIndex()),
          getType(id.getTypeIndex()));
      fields[index] = cstFieldRef;
    }
    return cstFieldRef;
  }

  @Override
  @Nonnull
  public CstString getCstString(@Nonnegative int index) {
    CstString cstString = strings[index];
    if (cstString == null) {
      cstString = new CstString(dexBuffer.strings().get(index));
      strings[index] = cstString;
    }
    return cstString;
  }

  @Override
  @Nonnull
  public Type getType(@Nonnegative int index) {
    Type type = types[index];
    if (type == null) {
      getCstString(dexBuffer.typeIds().get(index).intValue());
      String typeNameDesc = dexBuffer.typeNames().get(index);
      if (typeNameDesc.equals(Type.VOID.getDescriptor().getString())) {
        type = Type.VOID;
      } else {
        type = Type.intern(typeNameDesc);
      }
      types[index] = type;
    }
    return type;
  }

  @Override
  @Nonnull
  public CstPrototypeRef getCstPrototype(@Nonnegative int index) {
    CstPrototypeRef cstPrototypeRef = prototypes[index];
    if (cstPrototypeRef == null) {
      Prototype prototype = getPrototype(index);
      cstPrototypeRef = new CstPrototypeRef(prototype);
      prototypes[index] = cstPrototypeRef;
    }
    return cstPrototypeRef;
  }

  @Override
  @Nonnull
  public CstMethodHandleRef getCstMethodHandle(@Nonnegative int index) {
    CstMethodHandleRef cstMethodHandleRef = methodHandles[index];
    if (cstMethodHandleRef == null) {
      cstMethodHandleRef = getCstMethodHandleRef(dexBuffer.methodHandleIds().get(index));
      methodHandles[index] = cstMethodHandleRef;
    }
    return cstMethodHandleRef;
  }

  @Override
  @Nonnull
  public CstCallSiteRef getCstCallSite(@Nonnegative int index) {
    CstCallSiteRef cstCallSiteRef = callSites[index];
    if (cstCallSiteRef == null) {
      cstCallSiteRef = dexBuffer.readCstCallSiteRef(this,
          dexBuffer.callSiteIds().get(index).intValue());
      callSites[index] = cstCallSiteRef;
    }
    return cstCallSiteRef;
  }

  @Nonnull
  public Collection<CstString> getUsedCstString() {
    return filterToCollection(strings);
  }

  @Nonnull
  public Collection<Type> getUsedType() {
    return filterToCollection(types);

  }

  @Nonnull
  public Collection<CstFieldRef> getUsedCstFieldRef() {
    return filterToCollection(fields);

  }

  @Nonnull
  public Collection<CstMethodRef> getUsedCstMethodRef() {
    return filterToCollection(methods);
  }

  @Nonnull
  public Collection<CstPrototypeRef> getUsedCstPrototypeRef() {
    return filterToCollection(prototypes);
  }

  @Nonnull
  public Collection<CstMethodHandleRef> getUsedCstMethodHandleRef() {
    return filterToCollection(methodHandles);
  }

  @Nonnull
  public Collection<CstCallSiteRef> getUsedCstCallSiteRef() {
    return filterToCollection(callSites);
  }

  @Nonnull
  private Prototype getPrototype(@Nonnegative int index) {
    ProtoId protoId = dexBuffer.protoIds().get(index);
    getCstString(protoId.getShortyIndex());
    StdTypeList paramTypeList =
        getStdTypeList(dexBuffer.readTypeList(protoId.getParametersOffset()));

    Type returnType = getType(protoId.getReturnTypeIndex()).getType();
    Prototype prototype = Prototype.intern(paramTypeList, returnType);
    return prototype;
  }

  @Nonnull
  private <T> Collection<T> filterToCollection(@Nonnull T[] array) {
    Collection<T> used = new ArrayList<T>(array.length);
    for (T element : array) {
      if (element != null) {
        used.add(element);
      }
    }
    return used;
  }
}
