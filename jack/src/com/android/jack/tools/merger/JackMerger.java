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

import com.android.jack.dx.dex.file.ClassDefItem;
import com.android.jack.dx.dex.file.DexFile;
import com.android.jack.dx.dex.file.EncodedField;
import com.android.jack.dx.dex.file.EncodedMethod;
import com.android.jack.dx.dex.file.ImportedCodeItem;
import com.android.jack.dx.dex.file.ImportedDebugInfoItem;
import com.android.jack.dx.io.ClassData;
import com.android.jack.dx.io.ClassData.Field;
import com.android.jack.dx.io.ClassData.Method;
import com.android.jack.dx.io.ClassDef;
import com.android.jack.dx.io.Code;
import com.android.jack.dx.io.DexBuffer;
import com.android.jack.dx.rop.code.AccessFlags;
import com.android.jack.dx.rop.cst.CstIndexMap;
import com.android.jack.dx.rop.cst.CstMethodRef;
import com.android.jack.dx.rop.cst.CstString;
import com.android.jack.dx.rop.cst.CstType;
import com.android.jack.dx.rop.type.StdTypeList;
import com.android.jack.dx.rop.type.TypeList;

import java.io.IOException;
import java.io.OutputStream;

import javax.annotation.Nonnull;

/**
 * A tool to merge several dex files.
 */
public class JackMerger extends MergerTools {

  @Nonnull
  private final ConstantManager cstManager = new ConstantManager();

  @Nonnull
  private final AnnotationMerger am = new AnnotationMerger();

  @Nonnull
  private final DexFile dexResult;

  private boolean finished = false;

  public JackMerger(@Nonnull DexFile dexResult) {
    this.dexResult = dexResult;
  }

  public void addDexFile(@Nonnull DexBuffer dexToMerge) throws MergingOverflowException {
    if (finished) {
      throw new AssertionError("Merge already finished");
    }

    CstIndexMap cstIndexMap = cstManager.addDexFile(dexToMerge);

    for (ClassDef classDefToMerge : dexToMerge.classDefs()) {
      CstType superType = null;
      int supertypeIndex = classDefToMerge.getSupertypeIndex();
      if (supertypeIndex != ClassDef.NO_INDEX) {
        superType = cstIndexMap.getCstType(supertypeIndex);
      }
      CstString sourceFilename = null;
      int sourceFileIndex = classDefToMerge.getSourceFileIndex();
      if (sourceFileIndex != ClassDef.NO_INDEX) {
        sourceFilename = cstIndexMap.getCstString(sourceFileIndex);
      }

      ClassDefItem newClassDef = new ClassDefItem(
          cstIndexMap.getCstType(classDefToMerge.getTypeIndex()), classDefToMerge.getAccessFlags(),
          superType, getInterfacesList(classDefToMerge, cstIndexMap), sourceFilename);

      dexResult.add(newClassDef);

      mergeAnnotations(dexToMerge, classDefToMerge, newClassDef, cstIndexMap);

      if (classDefToMerge.getClassDataOffset() != 0) {
        ClassData classDataToMerge = dexToMerge.readClassData(classDefToMerge);

        for (Field fieldToMerge : classDataToMerge.getInstanceFields()) {
          newClassDef.addInstanceField(new EncodedField(
              cstIndexMap.getCstFieldRef(fieldToMerge.getFieldIndex()),
              fieldToMerge.getAccessFlags()));
        }

        ConstantValueArrayBuilder cvab = null;
        int staticValuesOffset = classDefToMerge.getStaticValuesOffset();
        if (staticValuesOffset != 0) {
          cvab = new ConstantValueArrayBuilder(dexToMerge, dexToMerge.open(staticValuesOffset));
          cvab.readArray();
        }

        int cstIdx = 0;
        for (Field fieldToMerge : classDataToMerge.getStaticFields()) {
          EncodedField encodedField = new EncodedField(
              cstIndexMap.getCstFieldRef(fieldToMerge.getFieldIndex()),
              fieldToMerge.getAccessFlags());
          newClassDef
              .addStaticField(encodedField,
                  (cvab != null && cstIdx < cvab.getCstSize()) ? cvab.getCstValueAtIdx(cstIdx++)
                      : null);
        }

        for (Method method : classDataToMerge.allMethods()) {
          CstMethodRef cstMethodRef = cstIndexMap.getCstMethodRef(method.getMethodIndex());
          ImportedCodeItem importCode = null;

          if (method.getCodeOffset() != 0) {
            Code code = dexToMerge.readCode(method);
            ImportedDebugInfoItem idii =
                code.getDebugInfoOffset() != 0 ? new ImportedDebugInfoItem(dexToMerge,
                    code.getDebugInfoOffset(), cstIndexMap) : null;

            importCode = new ImportedCodeItem(cstMethodRef, code, idii, cstIndexMap);
          }

          EncodedMethod encodeMethod =
              new EncodedMethod(cstMethodRef, method.getAccessFlags(), importCode);

          if (AccessFlags.isPrivate(encodeMethod.getAccessFlags())
              || AccessFlags.isStatic(encodeMethod.getAccessFlags())
              || AccessFlags.isConstructor(encodeMethod.getAccessFlags())) {
            newClassDef.addDirectMethod(encodeMethod);
          } else {
            newClassDef.addVirtualMethod(encodeMethod);
          }
        }
      }
    }
  }

  public void finish(@Nonnull OutputStream out) throws IOException {
    dexResult.prepare(cstManager.getCstStrings(), cstManager.getCstFieldRefs(),
        cstManager.getCstMethodRefs(), cstManager.getCstTypes(), cstManager.getCstPrototypeRefs());
    if (!cstManager.validate(dexResult)) {
      throw new AssertionError();
    }
    dexResult.writeTo(out, null /* humanOut */, false /* verbose */);
    finished = true;
  }

  private void mergeAnnotations(@Nonnull DexBuffer dexToMerge, @Nonnull ClassDef classDefToMerge,
      @Nonnull ClassDefItem newClassDef,  @Nonnull CstIndexMap cstIndexMap) {
    if (classDefToMerge.getAnnotationsOffset() != 0) {
      am.mergeAnnotationDirectory(dexToMerge, classDefToMerge.getAnnotationsOffset(), newClassDef,
          cstIndexMap);
    }
  }

  @Nonnull
  private TypeList getInterfacesList(@Nonnull ClassDef classDefToMerge,
      @Nonnull CstIndexMap cstIndexMap) {
    int interfaceCount = classDefToMerge.getInterfaces().length;
    if (interfaceCount == 0) {
      return StdTypeList.EMPTY;
    }

    StdTypeList interfaceList = new StdTypeList(interfaceCount);
    int idx = 0;
    for (int interfaceIdx : classDefToMerge.getInterfaces()) {
      interfaceList.set(idx++, cstIndexMap.getCstType(interfaceIdx).getClassType());
    }

    return (interfaceList);
  }
}
