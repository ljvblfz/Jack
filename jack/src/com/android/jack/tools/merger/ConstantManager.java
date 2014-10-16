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

import com.android.jack.dx.dex.DexFormat;
import com.android.jack.dx.dex.file.DexFile;
import com.android.jack.dx.io.DexBuffer;
import com.android.jack.dx.io.FieldId;
import com.android.jack.dx.io.MethodId;
import com.android.jack.dx.rop.cst.CstFieldRef;
import com.android.jack.dx.rop.cst.CstIndexMap;
import com.android.jack.dx.rop.cst.CstMethodRef;
import com.android.jack.dx.rop.cst.CstString;
import com.android.jack.dx.rop.cst.CstType;
import com.android.jack.dx.rop.type.Type;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * A tool to manage constants during dex merging.
 */
public class ConstantManager extends MergerTools {

  @Nonnull
  private final HashSet<CstString> cstStrings = new HashSet<CstString>();

  @Nonnull
  private final HashSet<CstFieldRef> cstFieldRefs = new HashSet<CstFieldRef>();

  @Nonnull
  private final HashSet<CstMethodRef> cstMethodRefs = new HashSet<CstMethodRef>();

  @Nonnull
  private final HashSet<CstType> cstTypes = new HashSet<CstType>();

  @Nonnull
  private final List<CstIndexMap> cstIndexMaps = new ArrayList<CstIndexMap>();

  @Nonnull
  public CstIndexMap addDexFile(@Nonnull DexBuffer dexBuffer) throws MergingOverflowException {
    CstIndexMap cstIndexMap = new CstIndexMap();

    HashSet<CstString> addedCstStrings = new HashSet<CstString>();
    HashSet<CstFieldRef> addedCstFieldRefs = new HashSet<CstFieldRef>();
    HashSet<CstMethodRef> addedCstMethodRefs = new HashSet<CstMethodRef>();
    HashSet<CstType> addedCstTypes = new HashSet<CstType>();

    int idx = 0;

    for (String string : dexBuffer.strings()) {
      CstString cstString = new CstString(string);
      if (!cstStrings.contains(cstString)) {
        addedCstStrings.add(cstString);
      }
      cstIndexMap.addStringMapping(idx++, cstString);
    }

    idx = 0;
    for (FieldId fieldId : dexBuffer.fieldIds()) {
      CstFieldRef cstFieldRef = getCstFieldRef(dexBuffer, fieldId);
      if (!cstFieldRefs.contains(cstFieldRef)) {
        addedCstFieldRefs.add(cstFieldRef);
      }
      cstIndexMap.addFieldMapping(idx++, cstFieldRef);
    }

    idx = 0;
    for (MethodId methodId : dexBuffer.methodIds()) {
      CstMethodRef cstMethodRef = getCstMethodRef(dexBuffer, methodId);
      if (!cstMethodRefs.contains(cstMethodRef)) {
        addedCstMethodRefs.add(cstMethodRef);
      }
      cstIndexMap.addMethodMapping(idx++, cstMethodRef);
    }

    idx = 0;
    for (String typeNameDesc : dexBuffer.typeNames()) {
      /*
       * Note: VOID isn't put in the intern table of type, since it's special and shouldn't be found
       * by a normal call to intern() from Type.
       */
      CstType cstType = null;
      if (typeNameDesc.equals(Type.VOID.getDescriptor())) {
        cstType = CstType.intern(Type.VOID);
      } else {
        cstType = getCstTypeFromTypeName(typeNameDesc);
      }

      if (!cstTypes.contains(cstType)) {
        addedCstTypes.add(cstType);
      }
      cstIndexMap.addTypeMapping(idx++, cstType);
    }

    if ((cstFieldRefs.size() + addedCstFieldRefs.size()) > DexFormat.MAX_MEMBER_IDX + 1) {
      throw new FieldIdOverflowException();
    }

    if ((cstMethodRefs.size() + addedCstMethodRefs.size()) > DexFormat.MAX_MEMBER_IDX + 1) {
      throw new MethodIdOverflowException();
    }

    if ((cstTypes.size() + addedCstTypes.size()) > DexFormat.MAX_TYPE_IDX + 1) {
      throw new TypeIdOverflowException();
    }

    cstStrings.addAll(addedCstStrings);
    cstFieldRefs.addAll(addedCstFieldRefs);
    cstMethodRefs.addAll(addedCstMethodRefs);
    cstTypes.addAll(addedCstTypes);

    cstIndexMaps.add(cstIndexMap);

    return cstIndexMap;
  }

  @Nonnull
  public List<CstIndexMap> getCstIndexMaps() {
    return cstIndexMaps;
  }

  public boolean validate(@Nonnull DexFile dexFile) {
    return ((dexFile.getStringIds().items().size() == cstStrings.size())
        && (dexFile.getFieldIds().items().size() == cstFieldRefs.size())
        && (dexFile.getMethodIds().items().size() == cstMethodRefs.size())
        && (dexFile.getTypeIds().items().size() == cstTypes.size()));
  }
}
