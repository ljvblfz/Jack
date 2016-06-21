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

package com.android.jack.optimizations.wofr;

import com.android.jack.optimizations.Optimizations;
import com.android.jack.test.dex.DexFileTypesValidator;
import com.android.jack.test.dex.DexMethodDalvikCodeValidator;
import com.android.jack.test.dex.DexOutputBasedTest;
import com.android.jack.test.dex.DexTypeExistingFieldValidator;
import com.android.jack.test.dex.DexTypeMethodsValidator;
import com.android.jack.test.dex.DexTypeMissingFieldValidator;

import org.junit.Test;

import javax.annotation.Nonnull;

/** Set of write-only field removal tests */
public class WriteOnlyFieldRemovalTests extends DexOutputBasedTest {
  @Nonnull
  private static String STR = "Ljava/lang/String;";
  @Nonnull
  private static String OBJ = "Ljava/lang/Object;";

  @Nonnull
  private CompilationProperties defaultProperties() {
    return CompilationProperties.EMPTY
        .excludeJillToolchain()
        .withPreserveJls(false)
        .withPreserveReflections(false)
        .with(Optimizations.WriteOnlyFieldRemoval.ENABLE.getName(), Boolean.TRUE);
  }

  @Nonnull
  private DexMethodDalvikCodeValidator dalvik(@Nonnull String test, @Nonnull String expected) {
    return new DexMethodDalvikCodeValidator(resource(test, expected));
  }

  @Nonnull
  private DexTypeMissingFieldValidator missing(@Nonnull String field) {
    return new DexTypeMissingFieldValidator(field);
  }

  @Nonnull
  private DexTypeExistingFieldValidator existing(@Nonnull String field) {
    return new DexTypeExistingFieldValidator(field);
  }

  @Test
  public void test001() throws Exception {
    String test = "com.android.jack.optimizations.wofr.test001";
    String aType = "Lcom/android/jack/optimizations/wofr/test001/A;";
    String bType = "Lcom/android/jack/optimizations/wofr/test001/B;";
    String cType = "Lcom/android/jack/optimizations/wofr/test001/C;";

    CompilationProperties properties = defaultProperties();

    compileAndValidate(test, properties,
        new DexFileTypesValidator()
            .insert(aType,
                new DexTypeMethodsValidator()
                    .insert("writes()V", dalvik(test, "A.writes.dalvik"))
                    .insert("<clinit>()V", dalvik(test, "A.clinit.dalvik"))
                    .insert("btest(" + bType + ")V", dalvik(test, "A.btest.dalvik"))
                    .andAlso(missing("_0_read_0_writes:I"))
                    .andAlso(existing("_1_read_0_writes:I"))
                    .andAlso(existing("_1_read_1_writes:I"))
                    .andAlso(missing("_0_read_1_writes:I"))
                    .andAlso(existing("_1_read_1_writes_init:I"))
                    .andAlso(missing("_0_read_1_writes_init:I"))
                    .andAlso(existing("_0_read_1_writes_vol:I")))
            .insert(bType,
                new DexTypeMethodsValidator()
                    .insert("btest(" + bType + ")V", dalvik(test, "B.btest.dalvik"))
                    .andAlso(missing("fld:I")))
            .insert(cType,
                new DexTypeMethodsValidator()
                    .insert("test(" + STR + "I)V", dalvik(test, "C.test.dalvik"))
                    .andAlso(missing("iF0:I"))
                    .andAlso(missing("sF1:" + STR))
                    .andAlso(missing("sF2:" + STR))
                    .andAlso(missing("sF2a:" + STR))
                    .andAlso(missing("sF3:" + OBJ))
                    .andAlso(missing("sF4:" + OBJ))));

    properties = properties.withPreserveJls(true);

    compileAndValidate(test, properties,
        new DexFileTypesValidator()
            .insert(aType,
                new DexTypeMethodsValidator()
                    .insert("writes()V", dalvik(test, "A.writes.dalvik"))
                    .insert("<clinit>()V", dalvik(test, "A.clinit.dalvik"))
                    .insert("btest(" + bType + ")V", dalvik(test, "A.btest.jls.dalvik"))
                    .andAlso(missing("_0_read_0_writes:I"))
                    .andAlso(existing("_1_read_0_writes:I"))
                    .andAlso(existing("_1_read_1_writes:I"))
                    .andAlso(missing("_0_read_1_writes:I"))
                    .andAlso(existing("_1_read_1_writes_init:I"))
                    .andAlso(missing("_0_read_1_writes_init:I"))
                    .andAlso(existing("_0_read_1_writes_vol:I")))
            .insert(bType,
                new DexTypeMethodsValidator()
                    .insert("btest(" + bType + ")V", dalvik(test, "B.btest.dalvik"))
                    .andAlso(existing("fld:I")))
            .insert(cType,
                new DexTypeMethodsValidator()
                    .insert("test(" + STR + "I)V", dalvik(test, "C.test.jls.dalvik"))
                    .andAlso(missing("iF0:I"))
                    .andAlso(missing("sF1:" + STR))
                    .andAlso(missing("sF2:" + STR))
                    .andAlso(existing("sF2a:" + STR))
                    .andAlso(missing("sF3:" + OBJ))
                    .andAlso(existing("sF4:" + OBJ))));

    properties = properties.withPreserveReflections(true);

    compileAndValidate(test, properties,
        new DexFileTypesValidator()
            .insert(aType,
                new DexTypeMethodsValidator()
                    .insert("writes()V", dalvik(test, "A.writes.dalvik"))
                    .insert("<clinit>()V", dalvik(test, "A.clinit.dalvik"))
                    .insert("btest(" + bType + ")V", dalvik(test, "A.btest.jls.dalvik"))
                    .andAlso(existing("_0_read_0_writes:I"))
                    .andAlso(existing("_1_read_0_writes:I"))
                    .andAlso(existing("_1_read_1_writes:I"))
                    .andAlso(existing("_0_read_1_writes:I"))
                    .andAlso(existing("_1_read_1_writes_init:I"))
                    .andAlso(existing("_0_read_1_writes_init:I"))
                    .andAlso(existing("_0_read_1_writes_vol:I")))
            .insert(bType,
                new DexTypeMethodsValidator()
                    .insert("btest(" + bType + ")V", dalvik(test, "B.btest.dalvik"))
                    .andAlso(existing("fld:I"))));
  }

  @Test
  public void test002() throws Exception {
    String test = "com.android.jack.optimizations.wofr.test002";
    String aType = "Lcom/android/jack/optimizations/wofr/test002/A;";

    compileAndValidate(test, defaultProperties(),
        new DexFileTypesValidator()
            .insert(aType,
                new DexTypeMethodsValidator()
                    .insert("test(" + aType + "I)V", dalvik(test, "A.test.dalvik"))));

    compileAndValidate(test, defaultProperties().withPreserveJls(true),
        new DexFileTypesValidator()
            .insert(aType,
                new DexTypeMethodsValidator()
                    .insert("test(" + aType + "I)V", dalvik(test, "A.test.jls.dalvik"))));
  }
}
