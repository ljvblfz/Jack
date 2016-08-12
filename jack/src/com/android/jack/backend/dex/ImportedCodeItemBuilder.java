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

package com.android.jack.backend.dex;

import com.android.jack.JackEventType;
import com.android.jack.Options;
import com.android.jack.dx.dex.file.ImportedCodeItem;
import com.android.jack.dx.dex.file.ImportedDebugInfoItem;
import com.android.jack.dx.dex.file.LazyCstIndexMap;
import com.android.jack.dx.io.ClassData.Method;
import com.android.jack.dx.io.Code;
import com.android.jack.dx.io.DexBuffer;
import com.android.jack.dx.rop.cst.CstMethodRef;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.scheduling.filter.TypeWithValidMethodPrebuilt;
import com.android.jack.scheduling.filter.TypeWithoutValidTypePrebuilt;
import com.android.jack.scheduling.marker.DexCodeMarker;
import com.android.jack.scheduling.marker.ImportedDexClassMarker;
import com.android.jack.scheduling.marker.ImportedDexMethodMarker;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.log.Event;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;

import javax.annotation.Nonnull;

/**
 * Generates {@link ImportedCodeItem} for {@link JMethod}. The
 * generated {@link ImportedCodeItem} is saved into the {@link DexCodeMarker}.
 */
@Description("Builds ImportedCodeItem for JMethod")
@Constraint(need = {ImportedDexClassMarker.class, ImportedDexMethodMarker.class})
@Transform(add = DexCodeMarker.class)
@Filter({TypeWithValidMethodPrebuilt.class, TypeWithoutValidTypePrebuilt.class})
public class ImportedCodeItemBuilder implements RunnableSchedulable<JMethod> {
  @Nonnull
  private final  Tracer tracer = TracerFactory.getTracer();

  @Nonnull
  private final com.android.jack.util.filter.Filter<JMethod> filter =
      ThreadConfig.get(Options.METHOD_FILTER);

  @Override
  public void run(@Nonnull JMethod jMethod) {
    if (jMethod.isNative()
        || jMethod.isAbstract()
        || !filter.accept(this.getClass(), jMethod)) {
      return;
    }

    try (Event importEvent = tracer.open(JackEventType.DEX_CODE_IMPORT)) {
      JDefinedClassOrInterface enclosingType = jMethod.getEnclosingType();
      ImportedDexClassMarker dexClassMarker = enclosingType.getMarker(ImportedDexClassMarker.class);
      assert dexClassMarker != null;
      synchronized (dexClassMarker) {
        LazyCstIndexMap cstIndexMap = dexClassMarker.getIndexMap();
        DexBuffer importedDexBuffer = dexClassMarker.getDexBuffer();
        ImportedDexMethodMarker importedMethod = jMethod.getMarker(ImportedDexMethodMarker.class);
        assert importedMethod != null;
        Method method = importedMethod.getMethod();
        CstMethodRef cstMethodRef = cstIndexMap.getCstMethodRef(method.getMethodIndex());
        assert method.getCodeOffset() != 0;
        Code code = importedDexBuffer.readCode(method);
        ImportedDebugInfoItem idii =
            code.getDebugInfoOffset() != 0 ? new ImportedDebugInfoItem(importedDexBuffer,
                code.getDebugInfoOffset(), cstIndexMap) : null;

        ImportedCodeItem codeItem = new ImportedCodeItem(cstMethodRef, code, idii, cstIndexMap);

        // parse code and debug info to populate cstIndexMap
        codeItem.loadLazyIndexMap();

        jMethod.addMarker(new DexCodeMarker(codeItem));
      }
    }
  }
}
