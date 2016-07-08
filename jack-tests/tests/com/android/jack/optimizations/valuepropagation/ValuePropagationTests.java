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

package com.android.jack.optimizations.valuepropagation;

import com.android.jack.optimizations.Optimizations;
import com.android.jack.test.dex.DexFileTypesValidator;
import com.android.jack.test.dex.DexMethodDalvikCodeValidator;
import com.android.jack.test.dex.DexOutputBasedTest;
import com.android.jack.test.dex.DexTypeMethodsValidator;
import com.android.jack.test.dex.DexTypeMissingMethodValidator;

import org.junit.Test;

import javax.annotation.Nonnull;

/** Set of value propagation tests */
public class ValuePropagationTests extends DexOutputBasedTest {
  @Nonnull
  private static final String STR = "Ljava/lang/String;";
  @Nonnull
  private static final String OBJ = "Ljava/lang/Object;";

  @Nonnull
  private CompilationProperties defaultProperties() {
    // NOTE: all the tests should be disabled if legacy compiler is used to compile
    //       the code, since we check dalvik code and it will be different
    return CompilationProperties.EMPTY
        .excludeJillToolchain()
        .with(Optimizations.FieldValuePropagation.ENSURE_TYPE_INITIALIZERS.getName(), Boolean.FALSE)
        .with(Optimizations.FieldValuePropagation.PRESERVE_NULL_CHECKS.getName(), Boolean.FALSE)
        .with(Optimizations.FieldValuePropagation.ENABLE.getName(), Boolean.TRUE)
        .with(Optimizations.ArgumentValuePropagation.ENABLE.getName(), Boolean.TRUE);
  }

  @Nonnull
  private DexMethodDalvikCodeValidator dalvik(@Nonnull String test, @Nonnull String expected) {
    return new DexMethodDalvikCodeValidator(resource(test, expected));
  }

  @Nonnull
  private DexTypeMissingMethodValidator missing(@Nonnull String method) {
    return new DexTypeMissingMethodValidator(method);
  }

  @Test
  public void test001() throws Exception {
    String test = "com.android.jack.optimizations.valuepropagation.test001";
    String aType = "Lcom/android/jack/optimizations/valuepropagation/test001/A;";

    compileAndValidate(test, defaultProperties(),
        new DexFileTypesValidator()
            .insert(aType,
                new DexTypeMethodsValidator()
                    .insert("use(I)I", dalvik(test, "A.use.dalvik"))));

    compileAndValidate(test,
        defaultProperties().with(
            Optimizations.FieldValuePropagation.ENABLE.getName(), Boolean.FALSE),
        new DexFileTypesValidator()
            .insert(aType,
                new DexTypeMethodsValidator()
                    .insert("use(I)I", dalvik(test, "A.use.jls.dalvik"))));
  }

  @Test
  public void test002() throws Exception {
    String test = "com.android.jack.optimizations.valuepropagation.test002";
    String aType = "Lcom/android/jack/optimizations/valuepropagation/test002/A;";
    String bType = "Lcom/android/jack/optimizations/valuepropagation/test002/B;";
    String cType = "Lcom/android/jack/optimizations/valuepropagation/test002/C;";
    String dType = "Lcom/android/jack/optimizations/valuepropagation/test002/D;";

    compileAndValidate(test, defaultProperties(),
        new DexFileTypesValidator()
            .insert(aType,
                new DexTypeMethodsValidator()
                    .insert("<init>(I)V", dalvik(test, "A.init.dalvik"))
                    .andAlso(missing("<clinit>()V")))
            .insert(bType,
                new DexTypeMethodsValidator()
                    .insert("<init>(I)V", dalvik(test, "B.init.dalvik"))
                    .insert("<clinit>()V", dalvik(test, "B.clinit.dalvik")))
            .insert(cType,
                new DexTypeMethodsValidator()
                    .insert("<init>()V", dalvik(test, "C.init.dalvik"))
                    .insert("foo()I", dalvik(test, "C.foo.dalvik")))
            .insert(dType,
                new DexTypeMethodsValidator()
                    .insert("foo()I", dalvik(test, "D.foo.dalvik"))));
  }

  @Test
  public void test003() throws Exception {
    String test = "com.android.jack.optimizations.valuepropagation.test003";
    String aType = "Lcom/android/jack/optimizations/valuepropagation/test003/A;";
    String bType = "Lcom/android/jack/optimizations/valuepropagation/test003/B;";
    String cType = "Lcom/android/jack/optimizations/valuepropagation/test003/C;";

    CompilationProperties properties = defaultProperties();

    compileAndValidate(test, properties,
        new DexFileTypesValidator()
            .insert(aType,
                new DexTypeMethodsValidator()
                    .insert("check()V", dalvik(test, "A.check.dalvik")))
            .insert(bType,
                new DexTypeMethodsValidator()
                    .insert("check()Ljava/lang/String;", dalvik(test, "B.check.dalvik")))
            .insert(cType,
                new DexTypeMethodsValidator()
                    .insert("check()Ljava/lang/String;", dalvik(test, "C.check.dalvik"))));

    properties = properties.with(
        Optimizations.FieldValuePropagation.ENSURE_TYPE_INITIALIZERS.getName(), Boolean.TRUE);

    compileAndValidate(test, properties,
        new DexFileTypesValidator()
            .insert(aType,
                new DexTypeMethodsValidator()
                    .insert("check()V", dalvik(test, "A.check.dalvik")))
            .insert(bType,
                new DexTypeMethodsValidator()
                    .insert("check()Ljava/lang/String;", dalvik(test, "B.check.e.t.i.dalvik")))
            .insert(cType,
                new DexTypeMethodsValidator()
                    .insert("check()Ljava/lang/String;", dalvik(test, "C.check.e.t.i.dalvik"))));
  }

  @Test
  public void test004() throws Exception {
    String test = "com.android.jack.optimizations.valuepropagation.test004";
    String aType = "Lcom/android/jack/optimizations/valuepropagation/test004/A;";

    CompilationProperties properties = defaultProperties();

    compileAndValidate(test, properties,
        new DexFileTypesValidator()
            .insert(aType,
                new DexTypeMethodsValidator()
                    .insert("check()I", dalvik(test, "A.check.dalvik"))));

    properties = properties.
        with(Optimizations.FieldValuePropagation.PRESERVE_NULL_CHECKS.getName(), Boolean.TRUE).
        with(Optimizations.FieldValuePropagation.ENSURE_TYPE_INITIALIZERS.getName(), Boolean.TRUE);

    compileAndValidate(test, properties,
        new DexFileTypesValidator()
            .insert(aType,
                new DexTypeMethodsValidator()
                    .insert("check()I", dalvik(test, "A.check.r.n.c.dalvik"))));
  }

  @Test
  public void test005() throws Exception {
    String test = "com.android.jack.optimizations.valuepropagation.test005";
    String aType = "Lcom/android/jack/optimizations/valuepropagation/test005/A;";
    String bType = "Lcom/android/jack/optimizations/valuepropagation/test005/B;";
    String cType = "Lcom/android/jack/optimizations/valuepropagation/test005/C;";

    compileAndValidate(test, defaultProperties(),
        new DexFileTypesValidator()
            .insert(aType,
                new DexTypeMethodsValidator()
                    .insert("check()I", dalvik(test, "A.check.dalvik")))
            .insert(bType,
                new DexTypeMethodsValidator()
                    .insert("check()I", dalvik(test, "B.check.dalvik")))
            .insert(cType,
                new DexTypeMethodsValidator()
                    .insert("check()I", dalvik(test, "C.check.dalvik"))));
  }

  @Test
  public void test101() throws Exception {
    String test = "com.android.jack.optimizations.valuepropagation.test101";
    String aType = "Lcom/android/jack/optimizations/valuepropagation/test101/A;";
    String bType = "Lcom/android/jack/optimizations/valuepropagation/test101/B;";
    String cType = "Lcom/android/jack/optimizations/valuepropagation/test101/C;";

    compileAndValidate(test, defaultProperties(),
        new DexFileTypesValidator()
            .insert(aType,
                new DexTypeMethodsValidator()
                    .insert("virtual(II)I", dalvik(test, "A.virtual.dalvik"))
                    .insert("private_(II)I", dalvik(test, "A.private.dalvik"))
                    .insert("static_(II)I", dalvik(test, "A.static.dalvik"))
                    .insert("interface_(III)I", dalvik(test, "A.interface.dalvik")))
            .insert(bType,
                new DexTypeMethodsValidator()
                    .insert("interface_(III)I", dalvik(test, "B.interface.dalvik")))
            .insert(cType,
                new DexTypeMethodsValidator()
                    .insert("foo(IIII)I", dalvik(test, "C.foo.dalvik"))));
  }

  @Test
  public void test102() throws Exception {
    String test = "com.android.jack.optimizations.valuepropagation.test102";
    String aType = "Lcom/android/jack/optimizations/valuepropagation/test102/A;";
    String bType = "Lcom/android/jack/optimizations/valuepropagation/test102/B;";
    String cType = "Lcom/android/jack/optimizations/valuepropagation/test102/C;";
    String ccType = "Lcom/android/jack/optimizations/valuepropagation/test102/CC;";
    String dType = "Lcom/android/jack/optimizations/valuepropagation/test102/D;";
    String ddType = "Lcom/android/jack/optimizations/valuepropagation/test102/DD;";
    String eType = "Lcom/android/jack/optimizations/valuepropagation/test102/E;";

    String foo = "foo(" + STR + STR + STR + STR + STR + STR + ")V";
    String bar = "bar(" + STR + STR + STR + STR + STR + STR + ")V";

    compileAndValidate(test, defaultProperties(),
        new DexFileTypesValidator()
            .insert(aType,
                new DexTypeMethodsValidator()
                    .insert(foo, dalvik(test, "A.foo.dalvik"))
                    .insert(bar, dalvik(test, "A.bar.dalvik")))
            .insert(bType,
                new DexTypeMethodsValidator()
                    .insert(foo, dalvik(test, "B.foo.dalvik"))
                    .insert(bar, dalvik(test, "B.bar.dalvik")))
            .insert(cType,
                new DexTypeMethodsValidator()
                    .insert(foo, dalvik(test, "C.foo.dalvik"))
                    .andAlso(missing(bar)))
            .insert(ccType,
                new DexTypeMethodsValidator()
                    .insert(foo, dalvik(test, "CC.foo.dalvik"))
                    .insert(bar, dalvik(test, "CC.bar.dalvik")))
            .insert(dType,
                new DexTypeMethodsValidator()
                    .insert(foo, dalvik(test, "D.foo.dalvik"))
                    .insert(bar, dalvik(test, "D.bar.dalvik")))
            .insert(ddType,
                new DexTypeMethodsValidator()
                    .insert(foo, dalvik(test, "DD.foo.dalvik"))
                    .andAlso(missing(bar)))
            .insert(eType,
                new DexTypeMethodsValidator()
                    .insert(foo, dalvik(test, "E.foo.dalvik"))
                    .andAlso(missing(bar))));
  }

  @Test
  public void test103() throws Exception {
    String test = "com.android.jack.optimizations.valuepropagation.test103";
    String aType = "Lcom/android/jack/optimizations/valuepropagation/test103/A;";
    String bType = "Lcom/android/jack/optimizations/valuepropagation/test103/B;";
    String cType = "Lcom/android/jack/optimizations/valuepropagation/test103/C;";
    String dType = "Lcom/android/jack/optimizations/valuepropagation/test103/D;";

    String foo = "foo(" + STR + ")" + STR;

    compileAndValidate(test, defaultProperties(),
        new DexFileTypesValidator()
            .insert(aType,
                new DexTypeMethodsValidator()
                    .insert(foo, dalvik(test, "A.foo.dalvik")))
            .insert(bType,
                new DexTypeMethodsValidator()
                    .insert(foo, dalvik(test, "B.foo.dalvik")))
            .insert(cType,
                new DexTypeMethodsValidator()
                    .insert(foo, dalvik(test, "C.foo.dalvik")))
            .insert(dType,
                new DexTypeMethodsValidator()
                    .insert(foo, dalvik(test, "D.foo.dalvik"))));
  }

  @Test
  public void test104() throws Exception {
    String test = "com.android.jack.optimizations.valuepropagation.test104";
    String aType = "Lcom/android/jack/optimizations/valuepropagation/test104/A;";
    String bType = "Lcom/android/jack/optimizations/valuepropagation/test104/B;";
    String cType = "Lcom/android/jack/optimizations/valuepropagation/test104/C;";
    String dType = "Lcom/android/jack/optimizations/valuepropagation/test104/D;";
    String eType = "Lcom/android/jack/optimizations/valuepropagation/test104/E;";

    String foo = "foo(" + STR + STR + STR + STR + STR + ")V";

    compileAndValidate(test, defaultProperties(),
        new DexFileTypesValidator()
            .insert(aType,
                new DexTypeMethodsValidator()
                    .insert(foo, dalvik(test, "A.foo.dalvik")))
            .insert(bType,
                new DexTypeMethodsValidator()
                    .insert(foo, dalvik(test, "B.foo.dalvik")))
            .insert(cType,
                new DexTypeMissingMethodValidator(foo))
            .insert(dType,
                new DexTypeMethodsValidator()
                    .insert(foo, dalvik(test, "D.foo.dalvik")))
            .insert(eType,
                new DexTypeMethodsValidator()
                    .insert(foo, dalvik(test, "E.foo.dalvik"))));
  }

  @Test
  public void test105() throws Exception {
    String test = "com.android.jack.optimizations.valuepropagation.test105";
    String aType = "Lcom/android/jack/optimizations/valuepropagation/test105/A;";
    String bType = "Lcom/android/jack/optimizations/valuepropagation/test105/B;";
    String cType = "Lcom/android/jack/optimizations/valuepropagation/test105/C;";
    String dType = "Lcom/android/jack/optimizations/valuepropagation/test105/D;";

    String equals = "equals(" + OBJ + ")Z";
    String equals2 = "equals2(" + OBJ + ")Z";
    String cmpTo = "compareTo(" + OBJ + ")I";
    String cmpTo2 = "compareTo2(" + OBJ + ")I";

    compileAndValidate(test, defaultProperties(),
        new DexFileTypesValidator()
            .insert(aType,
                new DexTypeMethodsValidator()
                    .insert(equals, dalvik(test, "A.equals.dalvik"))
                    .insert(equals2, dalvik(test, "A.equals2.dalvik"))
                    .insert(cmpTo, dalvik(test, "A.cmpTo.dalvik"))
                    .insert(cmpTo2, dalvik(test, "A.cmpTo2.dalvik")))
            .insert(bType,
                new DexTypeMethodsValidator()
                    .insert(equals, dalvik(test, "B.equals.dalvik"))
                    .insert(equals2, dalvik(test, "B.equals2.dalvik"))
                    .insert(cmpTo, dalvik(test, "B.cmpTo.dalvik"))
                    .insert(cmpTo2, dalvik(test, "B.cmpTo2.dalvik")))
            .insert(cType,
                missing(equals)
                    .andAlso(missing(equals2))
                    .andAlso(missing(cmpTo))
                    .andAlso(missing(cmpTo2)))
            .insert(dType,
                new DexTypeMethodsValidator()
                    .insert(equals, dalvik(test, "D.equals.dalvik"))
                    .insert(equals2, dalvik(test, "D.equals2.dalvik"))
                    .insert(cmpTo, dalvik(test, "D.cmpTo.dalvik"))
                    .insert(cmpTo2, dalvik(test, "D.cmpTo2.dalvik"))));
  }

  @Test
  public void test106() throws Exception {
    String test = "com.android.jack.optimizations.valuepropagation.test106";
    String aType = "Lcom/android/jack/optimizations/valuepropagation/test106/A;";
    String bType = "Lcom/android/jack/optimizations/valuepropagation/test106/B;";
    String cType = "Lcom/android/jack/optimizations/valuepropagation/test106/C;";
    String dType = "Lcom/android/jack/optimizations/valuepropagation/test106/D;";

    String init1 = "<init>(" + STR + ")V";
    String init2 = "<init>(" + STR + STR + ")V";
    String foo = "foo(" + STR + ")V";

    compileAndValidate(test, defaultProperties(),
        new DexFileTypesValidator()
            .insert(aType,
                new DexTypeMethodsValidator()
                    .insert(init1, dalvik(test, "A.init1.dalvik"))
                    .insert(init2, dalvik(test, "A.init2.dalvik"))
                    .insert(foo, dalvik(test, "A.foo.dalvik")))
            .insert(bType,
                new DexTypeMethodsValidator()
                    .insert(foo, dalvik(test, "B.foo.dalvik")))
            .insert(cType,
                new DexTypeMethodsValidator()
                    .insert(foo, dalvik(test, "C.foo.dalvik")))
            .insert(dType,
                new DexTypeMethodsValidator()
                    .insert(foo, dalvik(test, "D.foo.dalvik"))));
  }
}
