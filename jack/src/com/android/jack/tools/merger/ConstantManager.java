/*
 * Copyright (C) 2014 The Android Open Source Project
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
package com.android.jack.tools.merger;

import com.android.jack.Jack;
import com.android.jack.dx.dex.DexFormat;
import com.android.jack.dx.dex.file.DexFile;
import com.android.jack.dx.io.DexBuffer;
import com.android.jack.dx.io.FieldId;
import com.android.jack.dx.io.MethodId;
import com.android.jack.dx.io.ProtoId;
import com.android.jack.dx.rop.cst.CstFieldRef;
import com.android.jack.dx.rop.cst.CstIndexMap;
import com.android.jack.dx.rop.cst.CstMethodRef;
import com.android.jack.dx.rop.cst.CstNat;
import com.android.jack.dx.rop.cst.CstString;
import com.android.jack.dx.rop.cst.CstType;
import com.android.jack.dx.rop.type.Type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * A tool to manage constants during dex merging.
 */
public class ConstantManager extends MergerTools {

  @Nonnull
  private final Map<String, CstString> string2CstStrings = new HashMap<String, CstString>();

  @Nonnull
  private final HashSet<CstFieldRef> cstFieldRefs = new HashSet<CstFieldRef>();

  @Nonnull
  private final HashSet<CstMethodRef> cstMethodRefs = new HashSet<CstMethodRef>();

  @Nonnull
  private final HashSet<CstType> cstTypes = new HashSet<CstType>();

  @Nonnull
  private final Map<String, CstString> protoStr2CstString = new HashMap<String, CstString>();

  @Nonnull
  public Collection<CstString> getCstStrings() {
    return Jack.getUnmodifiableCollections().getUnmodifiableCollection(string2CstStrings.values());
  }

  @Nonnull
  public Collection<CstFieldRef> getCstFieldRefs() {
    return Jack.getUnmodifiableCollections().getUnmodifiableCollection(cstFieldRefs);
  }

  @Nonnull
  public Collection<CstMethodRef> getCstMethodRefs() {
    return Jack.getUnmodifiableCollections().getUnmodifiableCollection(cstMethodRefs);
  }

  @Nonnull
  public Collection<CstType> getCstTypes() {
    return Jack.getUnmodifiableCollections().getUnmodifiableCollection(cstTypes);
  }

  @Nonnull
  public CstIndexMap addDexFile(@Nonnull DexBuffer dexBuffer) throws MergingOverflowException {
    CstIndexMap cstIndexMap = new CstIndexMap(dexBuffer);

    List<String> cstStringsNewlyAdded = new ArrayList<String>();
    List<CstFieldRef> cstFieldRefsNewlyAdded = new ArrayList<CstFieldRef>();
    List<CstMethodRef> cstMethodRefsNewlyAdded = new ArrayList<CstMethodRef>();
    List<CstType> cstTypesNewlyAdded = new ArrayList<CstType>();

    int idx = 0;
    for (String string : dexBuffer.strings()) {
      CstString cstString = string2CstStrings.get(string);
      if (cstString == null) {
        cstString = new CstString(string);
        string2CstStrings.put(string, cstString);
        cstStringsNewlyAdded.add(string);
      }
      cstIndexMap.addStringMapping(idx++, cstString);
    }

    idx = 0;
    List<String> typeNames = dexBuffer.typeNames();
    for (String typeNameDesc : typeNames) {
      /*
       * Note: VOID isn't put in the intern table of type, since it's special and shouldn't be found
       * by a normal call to intern() from Type.
       */
      CstType cstType = null;
      if (typeNameDesc.equals(Type.VOID.getDescriptor())) {
        cstType = CstType.intern(Type.VOID);
      } else {
        cstType = CstType.intern(Type.intern(typeNameDesc));
      }

      if (cstTypes.add(cstType)) {
        cstTypesNewlyAdded.add(cstType);
      }

      cstIndexMap.addTypeMapping(idx++, cstType);
    }


    idx = 0;
    for (FieldId fieldId : dexBuffer.fieldIds()) {
      CstNat fieldNat = new CstNat(cstIndexMap.getCstString(fieldId.getNameIndex()),
          cstIndexMap.getCstType(fieldId.getTypeIndex()).getDescriptor());
      CstFieldRef cstFieldRef =
          new CstFieldRef(cstIndexMap.getCstType(fieldId.getDeclaringClassIndex()), fieldNat);
      if (cstFieldRefs.add(cstFieldRef)) {
        cstFieldRefsNewlyAdded.add(cstFieldRef);
      }
      cstIndexMap.addFieldMapping(idx++, cstFieldRef);
    }

    idx = 0;
    List<ProtoId> protoIds = dexBuffer.protoIds();
    String[] protoIdx2String = new String[protoIds.size()];

    for (MethodId methodId : dexBuffer.methodIds()) {
      int protoIdx = methodId.getProtoIndex();
      String protoStr = protoIdx2String[protoIdx];
      ProtoId protoId = protoIds.get(protoIdx);

      if (protoStr == null) {
        protoStr = dexBuffer.readTypeList(protoId.getParametersOffset()).toString();
        protoIdx2String[protoIdx] = protoStr;
      }

      protoStr += typeNames.get(protoId.getReturnTypeIndex());

      CstString protoCstString = protoStr2CstString.get(protoStr);
      if (protoCstString == null) {
        protoCstString = new CstString(protoStr);
        protoStr2CstString.put(protoStr, protoCstString);
      }

      CstNat methNat =
          new CstNat(cstIndexMap.getCstString(methodId.getNameIndex()), protoCstString);
      CstMethodRef cstMethodRef =
          new CstMethodRef(cstIndexMap.getCstType(methodId.getDeclaringClassIndex()), methNat);
      if (cstMethodRefs.add(cstMethodRef)) {
        cstMethodRefsNewlyAdded.add(cstMethodRef);
      }
      cstIndexMap.addMethodMapping(idx++, cstMethodRef);
    }

    if ((cstFieldRefs.size()) > DexFormat.MAX_MEMBER_IDX + 1) {
      removeItems(cstStringsNewlyAdded, cstFieldRefsNewlyAdded, cstMethodRefsNewlyAdded,
          cstTypesNewlyAdded);
      throw new FieldIdOverflowException();
    }

    if ((cstMethodRefs.size()) > DexFormat.MAX_MEMBER_IDX + 1) {
      removeItems(cstStringsNewlyAdded, cstFieldRefsNewlyAdded, cstMethodRefsNewlyAdded,
          cstTypesNewlyAdded);
      throw new MethodIdOverflowException();
    }

    if ((cstTypes.size()) > DexFormat.MAX_TYPE_IDX + 1) {
      removeItems(cstStringsNewlyAdded, cstFieldRefsNewlyAdded, cstMethodRefsNewlyAdded,
          cstTypesNewlyAdded);
      throw new TypeIdOverflowException();
    }

    return cstIndexMap;
  }

  private void removeItems(@Nonnull List<String> cstStringsToRemove,
      @Nonnull List<CstFieldRef> cstFieldRefsToRemove,
      @Nonnull List<CstMethodRef> cstMethodRefsToRemove, @Nonnull List<CstType> cstTypesToRemove) {
    string2CstStrings.keySet().removeAll(cstStringsToRemove);
    cstFieldRefs.removeAll(cstFieldRefsToRemove);
    cstMethodRefs.removeAll(cstMethodRefsToRemove);
    cstTypes.removeAll(cstTypesToRemove);
  }

  public boolean validate(@Nonnull DexFile dexFile) {
    return ((dexFile.getStringIds().items().size() == string2CstStrings.size())
        && (dexFile.getFieldIds().items().size() == cstFieldRefs.size())
        && (dexFile.getMethodIds().items().size() == cstMethodRefs.size())
        && (dexFile.getTypeIds().items().size() == cstTypes.size()));
  }
}
