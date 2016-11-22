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

package com.android.jack.optimizations.blockmerger;

import com.android.jack.optimizations.Optimizations;
import com.android.jack.optimizations.cfg.VariablesScope;
import com.android.jack.test.dex.DexFileTypesValidator;
import com.android.jack.test.dex.DexMethod;
import com.android.jack.test.dex.DexMethodDalvikCodeValidator;
import com.android.jack.test.dex.DexNoOpValidator;
import com.android.jack.test.dex.DexOutputBasedTest;
import com.android.jack.test.dex.DexTypeMethodsValidator;
import com.android.jack.test.dex.DexValidator;
import com.android.jack.test.junit.Runtime;

import org.junit.Test;

import javax.annotation.Nonnull;

/** Set of unused local variables removal */
public class BlockMergerTests extends DexOutputBasedTest {
  @Nonnull
  private CompilationProperties properties() {
    return CompilationProperties.EMPTY
        .with(Optimizations.SimpleBasicBlockMerging.ENABLE.getName(), Boolean.FALSE);
  }

  @Nonnull
  private CompilationProperties properties(
      boolean preserveSourceInfo, @Nonnull VariablesScope scope) {
    return CompilationProperties.EMPTY
        .with(Optimizations.SimpleBasicBlockMerging.ENABLE.getName(), Boolean.TRUE)
        .with(Optimizations.SimpleBasicBlockMerging.PRESERVE_SOURCE_INFO.getName(),
            Boolean.valueOf(preserveSourceInfo))
        .with(Optimizations.SimpleBasicBlockMerging.MERGE_VARIABLES.getName(),
            scope.toString());
  }

  @Nonnull
  private DexValidator<DexMethod> dalvik(@Nonnull String test, @Nonnull String expected) {
    return usingLegacyCompiler() ? new DexNoOpValidator<DexMethod>() :
        new DexMethodDalvikCodeValidator(resource(test, expected));
  }

  @Test
  @Runtime
  public void test001() throws Exception {
    String test = "com.android.jack.optimizations.blockmerger.test001";
    String aType = "Lcom/android/jack/optimizations/blockmerger/test001/jack/A;";

    compileAndValidate(test, properties(),
        new DexFileTypesValidator()
            .insert(aType,
                new DexTypeMethodsValidator()
                    .insert("testA(IIIII)I", dalvik(test, "A.testA.no-opt.dalvik"))
                    .insert("testB(IIIII)I", dalvik(test, "A.testB.no-opt.dalvik"))
                    .insert("testC(IIIII)I", dalvik(test, "A.testC.no-opt.dalvik"))));

    compileAndValidate(test, properties(false, VariablesScope.NONE),
        new DexFileTypesValidator()
            .insert(aType,
                new DexTypeMethodsValidator()
                    .insert("testA(IIIII)I", dalvik(test, "A.testA.no-opt.dalvik" /* SAME */))
                    .insert("testB(IIIII)I", dalvik(test, "A.testB.no-opt.dalvik" /* SAME */))
                    .insert("testC(IIIII)I", dalvik(test, "A.testC.opt-none.dalvik"))));

    compileAndValidate(test, properties(false, VariablesScope.SYNTHETIC),
        new DexFileTypesValidator()
            .insert(aType,
                new DexTypeMethodsValidator()
                    .insert("testA(IIIII)I", dalvik(test, "A.testA.opt-syn.dalvik"))
                    .insert("testB(IIIII)I", dalvik(test, "A.testB.no-opt.dalvik" /* SAME */))
                    .insert("testC(IIIII)I", dalvik(test, "A.testC.opt-none.dalvik" /* SAME */))));

    compileAndValidate(test, properties(false, VariablesScope.ALL),
        new DexFileTypesValidator()
            .insert(aType,
                new DexTypeMethodsValidator()
                    .insert("testA(IIIII)I", dalvik(test, "A.testA.opt-syn.dalvik") /* SAME */)
                    .insert("testB(IIIII)I", dalvik(test, "A.testB.opt-all.dalvik"))
                    .insert("testC(IIIII)I", dalvik(test, "A.testC.opt-none.dalvik") /* SAME */)));
  }

  @Test
  @Runtime
  public void test002() throws Exception {
    String test = "com.android.jack.optimizations.blockmerger.test002";
    String aType = "Lcom/android/jack/optimizations/blockmerger/test002/jack/A;";

    compileAndValidate(test, properties(),
        new DexFileTypesValidator()
            .insert(aType,
                new DexTypeMethodsValidator()
                    .insert("testA(I)I", dalvik(test, "A.testA.no-opt.dalvik"))
                    .insert("testB(III)I", dalvik(test, "A.testB.no-opt.dalvik"))
                    .insert("testC(I)I", dalvik(test, "A.testC.no-opt.dalvik"))));

    compileAndValidate(test, properties(true, VariablesScope.ALL),
        new DexFileTypesValidator()
            .insert(aType,
                new DexTypeMethodsValidator()
                    .insert("testA(I)I", dalvik(test, "A.testA.no-opt.dalvik"))
                    .insert("testB(III)I", dalvik(test, "A.testB.no-opt.dalvik"))));

    compileAndValidate(test, properties(false, VariablesScope.SYNTHETIC),
        new DexFileTypesValidator()
            .insert(aType,
                new DexTypeMethodsValidator()
                    .insert("testC(I)I", dalvik(test, "A.testC.opt-syn.dalvik"))));

    compileAndValidate(test, properties(false, VariablesScope.ALL),
        new DexFileTypesValidator()
            .insert(aType,
                new DexTypeMethodsValidator()
                    .insert("testA(I)I", dalvik(test, "A.testA.opt-all.dalvik"))
                    .insert("testB(III)I", dalvik(test, "A.testB.opt-all.dalvik"))));
  }
}
