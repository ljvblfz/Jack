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

package com.android.jack.optimizations.modifiers;

import com.android.jack.optimizations.Optimizations;
import com.android.jack.test.dex.DexFieldFinalValidator;
import com.android.jack.test.dex.DexFileTypesValidator;
import com.android.jack.test.dex.DexMethodFinalValidator;
import com.android.jack.test.dex.DexOutputBasedTest;
import com.android.jack.test.dex.DexTypeFieldsValidator;
import com.android.jack.test.dex.DexTypeFinalValidator;
import com.android.jack.test.dex.DexTypeMethodsValidator;
import com.android.jack.test.junit.KnownIssue;
import com.android.jack.test.toolchain.JillBasedToolchain;

import org.junit.Test;

/** Tests for modifiers optimizations, such as finalizers, etc... */
public class ModifiersTighteningTests extends DexOutputBasedTest {

  @Test
  public void test001() throws Exception {
    String testPackage = "com.android.jack.optimizations.modifiers.test001";

    CompilationProperties properties =
        CompilationProperties.EMPTY
            .withPreserveReflections(true)
            .withPreserveJls(true)
            .with(Optimizations.ClassFinalizer.ENABLE.getName(), Boolean.TRUE)
            .with(Optimizations.MethodFinalizer.ENABLE.getName(), Boolean.FALSE)
            .with(Optimizations.FieldFinalizer.ENABLE.getName(), Boolean.FALSE);

    DexTypeFinalValidator isNotFinal = new DexTypeFinalValidator(false);
    DexTypeFinalValidator isFinal = new DexTypeFinalValidator(true);

    DexFileTypesValidator validators =
        new DexFileTypesValidator()
            .insert("Lcom/android/jack/optimizations/modifiers/test001/A;", isNotFinal)
            .insert("Lcom/android/jack/optimizations/modifiers/test001/A2final;", isNotFinal)
            .insert("Lcom/android/jack/optimizations/modifiers/test001/B;", isNotFinal)
            .insert("Lcom/android/jack/optimizations/modifiers/test001/C2final;", isNotFinal)
            .insert("Lcom/android/jack/optimizations/modifiers/test001/D;", isNotFinal)
            .insert("Lcom/android/jack/optimizations/modifiers/test001/E;", isNotFinal)
            .insert("Lcom/android/jack/optimizations/modifiers/test001/F2final;", isNotFinal)
            .insert("Lcom/android/jack/optimizations/modifiers/test001/IFooA;", isNotFinal)
            .insert("Lcom/android/jack/optimizations/modifiers/test001/IFooB;", isNotFinal)
            .insert("Lcom/android/jack/optimizations/modifiers/test001/IFooAB;", isNotFinal)
            .insert("Lcom/android/jack/optimizations/modifiers/test001/IFooC;", isNotFinal);

    compileAndValidate(testPackage, properties, validators);

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    validators
        .update("Lcom/android/jack/optimizations/modifiers/test001/A2final;", isFinal)
        .update("Lcom/android/jack/optimizations/modifiers/test001/C2final;", isFinal)
        .update("Lcom/android/jack/optimizations/modifiers/test001/F2final;", isFinal);

    compileAndValidate(testPackage, properties.withPreserveReflections(false), validators);
  }

  @Test
  public void test002() throws Exception {
    String testPackage = "com.android.jack.optimizations.modifiers.test002";

    CompilationProperties properties =
        CompilationProperties.EMPTY
            .withPreserveReflections(true)
            .withPreserveJls(true)
            .with(Optimizations.ClassFinalizer.ENABLE.getName(), Boolean.FALSE)
            .with(Optimizations.MethodFinalizer.ENABLE.getName(), Boolean.TRUE)
            .with(Optimizations.FieldFinalizer.ENABLE.getName(), Boolean.FALSE);

    DexMethodFinalValidator isNotFinal = new DexMethodFinalValidator(false);
    DexMethodFinalValidator isFinal = new DexMethodFinalValidator(true);

    DexTypeMethodsValidator mBase =
        new DexTypeMethodsValidator()
            .insert("bar()Ljava/lang/Object;", isNotFinal)
            .insert("foo()Ljava/lang/Object;", isNotFinal)
            .insert("g(Ljava/lang/String;)Ljava/util/AbstractList;", isNotFinal)
            .insert("g(Ljava/lang/String;)Ljava/util/ArrayList;", isNotFinal)
            .insert("<init>()V", isNotFinal);

    DexTypeMethodsValidator mD1 =
        new DexTypeMethodsValidator()
            .insert("bar()Ljava/lang/Object;", isNotFinal)
            .insert("foo()Lcom/android/jack/optimizations/modifiers/test002/Base;", isNotFinal)
            .insert("foo()Ljava/lang/Object;", isNotFinal)
            .insert("<init>()V", isNotFinal);

    DexTypeMethodsValidator mD2 =
        new DexTypeMethodsValidator()
            .insert("bar()Ljava/lang/Object;", isNotFinal)
            .insert("foo()Lcom/android/jack/optimizations/modifiers/test002/Base;", isNotFinal)
            .insert("foo()Lcom/android/jack/optimizations/modifiers/test002/D1;", isNotFinal)
            .insert("<init>()V", isNotFinal);

    DexTypeMethodsValidator mInter =
        new DexTypeMethodsValidator()
            .insert("foo()Lcom/android/jack/optimizations/modifiers/test002/Base;", isNotFinal);

    DexTypeMethodsValidator mPreBase =
        new DexTypeMethodsValidator()
            .insert("foo()Ljava/lang/Object;", isNotFinal)
            .insert("<init>()V", isNotFinal);

    DexTypeMethodsValidator mPrePreBase =
        new DexTypeMethodsValidator()
            .insert("bar()Ljava/lang/Object;", isNotFinal)
            .insert("g(Ljava/lang/String;)Ljava/util/AbstractList;", isNotFinal)
            .insert("<init>()V", isNotFinal);

    DexFileTypesValidator validators =
        new DexFileTypesValidator()
            .insert("Lcom/android/jack/optimizations/modifiers/test002/Base;", mBase)
            .insert("Lcom/android/jack/optimizations/modifiers/test002/D1;", mD1)
            .insert("Lcom/android/jack/optimizations/modifiers/test002/D2;", mD2)
            .insert("Lcom/android/jack/optimizations/modifiers/test002/Inter;", mInter)
            .insert("Lcom/android/jack/optimizations/modifiers/test002/PreBase;", mPreBase)
            .insert("Lcom/android/jack/optimizations/modifiers/test002/PrePreBase;", mPrePreBase);

    compileAndValidate(testPackage, properties, validators);

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    mBase.update("g(Ljava/lang/String;)Ljava/util/AbstractList;", isFinal);
    mBase.update("g(Ljava/lang/String;)Ljava/util/ArrayList;", isFinal);

    // NOTE: the legacy compiler create this method in 'D2' as well,
    //       thus this one is not marked as final in 'D1'.
    if (!usingLegacyCompiler()) {
      mD1.update("foo()Ljava/lang/Object;", isFinal);
    } else {
      mD2.insert("foo()Ljava/lang/Object;", isFinal);
    }

    mD2.update("bar()Ljava/lang/Object;", isFinal);
    mD2.update("foo()Lcom/android/jack/optimizations/modifiers/test002/Base;", isFinal);
    mD2.update("foo()Lcom/android/jack/optimizations/modifiers/test002/D1;", isFinal);

    compileAndValidate(testPackage, properties.withPreserveReflections(false), validators);
  }

  @Test
  @KnownIssue(candidate = JillBasedToolchain.class)
  public void test003() throws Exception {
    String testPackage = "com.android.jack.optimizations.modifiers.test003";

    CompilationProperties properties =
        CompilationProperties.EMPTY
            .withPreserveReflections(true)
            .withPreserveJls(false)
            .with(Optimizations.ClassFinalizer.ENABLE.getName(), Boolean.FALSE)
            .with(Optimizations.MethodFinalizer.ENABLE.getName(), Boolean.FALSE)
            .with(Optimizations.FieldFinalizer.ENABLE.getName(), Boolean.TRUE);

    DexFieldFinalValidator isNotFinal = new DexFieldFinalValidator(false);
    DexFieldFinalValidator isFinal = new DexFieldFinalValidator(true);

    DexTypeFieldsValidator fBase =
        new DexTypeFieldsValidator()
            .insert("bf1_ok:I", isNotFinal)
            .insert("bf2_partially_assigned:I", isNotFinal)
            .insert("bf3_reassigned:I", isNotFinal)
            .insert("bf4_written_from_outside:I", isNotFinal)
            .insert("bf5_assigned_in_delegating_constr:I", isNotFinal)
            .insert("bf6_assigned_from_derived:I", isNotFinal)
            .insert("bf7_ok_but_initialized:I", isNotFinal)
            .insert("bf8_ok_volatile:I", isNotFinal)
            .insert("f0:I", isFinal)
            .insert("sbf1_ok:I", isNotFinal)
            .insert("sbf2_assigned_in_instance_constr:I", isNotFinal)
            .insert("sbf3_assigned_in_static_constr:I", isNotFinal)
            .insert("sbf4_reassigned_in_static_constr:I", isNotFinal)
            .insert("sbf5_not_initialized:I", isNotFinal);

    DexTypeFieldsValidator fDerived =
        new DexTypeFieldsValidator().insert("df0_not_assigned:I", isNotFinal);

    DexTypeFieldsValidator fInner =
        new DexTypeFieldsValidator().insert("a:Ljava/lang/Object;", isNotFinal);

    DexFileTypesValidator validators =
        new DexFileTypesValidator()
            .insert("Lcom/android/jack/optimizations/modifiers/test003/FldBase;", fBase)
            .insert("Lcom/android/jack/optimizations/modifiers/test003/FldDerived;", fDerived)
            .insert("Lcom/android/jack/optimizations/modifiers/test003/Outer$Inner;", fInner);

    compileAndValidate(testPackage, properties, validators);

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    fBase.update("bf1_ok:I", isFinal);
    fBase.update("bf2_partially_assigned:I", isFinal);
    fBase.update("bf3_reassigned:I", isFinal);
    fBase.update("bf5_assigned_in_delegating_constr:I", isFinal);
    fBase.update("bf7_ok_but_initialized:I", isFinal);
    fBase.update("sbf1_ok:I", isFinal);
    fBase.update("sbf3_assigned_in_static_constr:I", isFinal);
    fBase.update("sbf4_reassigned_in_static_constr:I", isFinal);
    fBase.update("sbf5_not_initialized:I", isFinal);

    fDerived.update("df0_not_assigned:I", isFinal);

    properties = properties.withPreserveReflections(false);
    compileAndValidate(testPackage, properties, validators);

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    fBase.update("bf2_partially_assigned:I", isNotFinal);
    fBase.update("bf3_reassigned:I", isNotFinal);
    fBase.update("bf5_assigned_in_delegating_constr:I", isNotFinal);
    fBase.update("bf7_ok_but_initialized:I", isNotFinal);
    fBase.update("sbf4_reassigned_in_static_constr:I", isNotFinal);
    fBase.update("sbf5_not_initialized:I", isNotFinal);

    fDerived.update("df0_not_assigned:I", isNotFinal);

    properties = properties.withPreserveJls(true);
    compileAndValidate(testPackage, properties, validators);
  }
}
