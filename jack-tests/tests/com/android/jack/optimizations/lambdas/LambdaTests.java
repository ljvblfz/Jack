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

package com.android.jack.optimizations.lambdas;

import com.google.common.io.Files;

import com.android.jack.Options;
import com.android.jack.test.dex.DexFileTypesValidator;
import com.android.jack.test.dex.DexOutputBasedTest;
import com.android.jack.test.dex.DexTypeAccessValidator;
import com.android.jack.test.helper.IncrementalTestHelper;
import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.junit.Runtime;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.transformations.lambda.LambdaGroupingScope;

import org.junit.Test;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/** Tests for lambdas optimizations */
public class LambdaTests extends DexOutputBasedTest {

  @Nonnull
  private static CompilationProperties config(
      @Nonnull LambdaGroupingScope scope, boolean mergeInterfaces, boolean simplifyStateless) {
    return CompilationProperties
        .EMPTY
        .enableJava8()
        .with(Options.LAMBDA_GROUPING_SCOPE.getName(), scope)
        .with(Options.LAMBDA_MERGE_INTERFACES.getName(), Boolean.valueOf(mergeInterfaces))
        .with(Options.LAMBDA_SIMPLIFY_STATELESS.getName(), Boolean.valueOf(simplifyStateless));
  }

  // ===============================================================================================

  @Nonnull
  private static String type(@Nonnull String pkg, @Nonnull String name) {
    return ("L" + pkg + "/jack/" + name + ";").replace('.', '/');
  }

  @Nonnull
  private static String lambda(@Nonnull String pkg, @Nonnull String sha, @Nonnegative int i) {
    return type(pkg, "-$Lambda$" + i + "$" + sha);
  }

  @Nonnull
  private static String[] types(@Nonnull String pkg, @Nonnull String... names) {
    for (int i = 0; i < names.length; i++) {
      names[i] = type(pkg, names[i]);
    }
    return names;
  }

  private static class Class {
    @Nonnull
    private final StringBuilder builder = new StringBuilder();

    Class(@Nonnull String name, @Nonnull String... interfaces) {
      builder.append(name).append("\n");
      printImpl(interfaces, "  - implements:\n", false);
    }

    @Nonnull
    Class methods(@Nonnull String... signatures) {
      return printImpl(signatures, "  - methods:\n", true);
    }

    @Nonnull
    Class fields(@Nonnull String... fields) {
      return printImpl(fields, "  - fields:\n", true);
    }

    @Nonnull
    private Class printImpl(@Nonnull String[] names, @Nonnull String str, boolean ordered) {
      if (names.length > 0) {
        builder.append(str);
        if (ordered) {
          Arrays.sort(names);
        }
        for (String n : names) {
          builder.append("    ").append(n).append("\n");
        }
      }
      return this;
    }

    @Nonnull
    @Override
    public String toString() {
      return builder.toString();
    }
  }

  // ===============================================================================================

  @Nonnull
  private static final String PKG_001 = "com.android.jack.optimizations.lambdas.test001";
  @Nonnull
  private static final String SHA_001 = "IKUiHr1YvEGLPsKj1XVCnvkJpxY";

  @Nonnull
  private static final String TEST001_NONE = "" +
      new Class(lambda(PKG_001, SHA_001, 0), types(PKG_001, "I0"))
          .methods("<init>()V", "foo()V", "$m$0()V") +
      new Class(lambda(PKG_001, SHA_001, 1), types(PKG_001, "I0"))
          .methods("<init>()V", "foo()V", "$m$0()V") +
      new Class(lambda(PKG_001, SHA_001, 10), types(PKG_001, "TestB"))
          .fields("-$f0:Ljava/lang/Object;", "-$f1:Ljava/lang/Object;")
          .methods("<init>(Ljava/lang/Object;Ljava/lang/Object;)V",
              "foo(Ljava/lang/String;)Ljava/lang/String;",
              "$m$0(Ljava/lang/String;)Ljava/lang/String;") +
      new Class(lambda(PKG_001, SHA_001, 11), types(PKG_001, "TestB"))
          .fields("-$f0:Ljava/lang/Object;", "-$f1:Ljava/lang/Object;")
          .methods("<init>(Ljava/lang/Object;Ljava/lang/Object;)V",
              "foo(Ljava/lang/String;)Ljava/lang/String;",
              "$m$0(Ljava/lang/String;)Ljava/lang/String;") +
      new Class(lambda(PKG_001, SHA_001, 12), types(PKG_001, "TestB"))
          .fields("-$f0:I", "-$f1:Ljava/lang/Object;")
          .methods("<init>(ILjava/lang/Object;)V",
              "foo(Ljava/lang/String;)Ljava/lang/String;",
              "$m$0(Ljava/lang/String;)Ljava/lang/String;") +
      new Class(lambda(PKG_001, SHA_001, 13), types(PKG_001, "TestB"))
          .fields("-$f0:I", "-$f1:Ljava/lang/Object;")
          .methods("<init>(ILjava/lang/Object;)V",
              "foo(Ljava/lang/String;)Ljava/lang/String;",
              "$m$0(Ljava/lang/String;)Ljava/lang/String;") +
      new Class(lambda(PKG_001, SHA_001, 14), types(PKG_001, "TestB"))
          .fields("-$f0:Z", "-$f1:B", "-$f2:C", "-$f3:D", "-$f4:F", "-$f5:I", "-$f6:J", "-$f7:S",
              "-$f8:Ljava/lang/Object;")
          .methods("<init>(ZBCDFIJSLjava/lang/Object;)V",
              "foo(Ljava/lang/String;)Ljava/lang/String;",
              "$m$0(Ljava/lang/String;)Ljava/lang/String;") +
      new Class(lambda(PKG_001, SHA_001, 15), types(PKG_001, "TestB"))
          .fields("-$f0:Z", "-$f1:B", "-$f2:C", "-$f3:D", "-$f4:F", "-$f5:I", "-$f6:J", "-$f7:S",
              "-$f8:Ljava/lang/Object;")
          .methods("<init>(ZBCDFIJSLjava/lang/Object;)V",
              "foo(Ljava/lang/String;)Ljava/lang/String;",
              "$m$0(Ljava/lang/String;)Ljava/lang/String;") +
      new Class(lambda(PKG_001, SHA_001, 16), types(PKG_001, "TestB"))
          .fields("-$f0:Z", "-$f1:B", "-$f2:C", "-$f3:D", "-$f4:F", "-$f5:I", "-$f6:J", "-$f7:S",
              "-$f8:Ljava/lang/Object;")
          .methods("<init>(ZBCDFIJSLjava/lang/Object;)V",
              "foo(Ljava/lang/String;)Ljava/lang/String;",
              "$m$0(Ljava/lang/String;)Ljava/lang/String;") +
      new Class(lambda(PKG_001, SHA_001, 2), types(PKG_001, "I0"))
          .methods("<init>()V", "foo()V", "$m$0()V") +
      new Class(lambda(PKG_001, SHA_001, 3), types(PKG_001, "I1"))
          .methods("<init>()V", "bar(I)I", "$m$0(I)I") +
      new Class(lambda(PKG_001, SHA_001, 4), types(PKG_001, "IA"))
          .methods("<init>()V", "foo()Ljava/lang/Object;", "$m$0()Ljava/lang/Object;") +
      new Class(lambda(PKG_001, SHA_001, 5), types(PKG_001, "IA"))
          .methods("<init>()V", "foo()Ljava/lang/Object;", "$m$0()Ljava/lang/Object;") +
      new Class(lambda(PKG_001, SHA_001, 6), types(PKG_001, "IA"))
          .methods("<init>()V", "foo()Ljava/lang/Object;", "$m$0()Ljava/lang/Object;") +
      new Class(lambda(PKG_001, SHA_001, 7), types(PKG_001, "IB"))
          .methods("<init>()V", "foo()Ljava/lang/Object;", "foo()Ljava/lang/Integer;",
              "$m$0()Ljava/lang/Integer;", "$m$1()Ljava/lang/Object;") +
      new Class(lambda(PKG_001, SHA_001, 8), types(PKG_001, "TestB"))
          .fields("-$f0:Ljava/lang/Object;")
          .methods("<init>(Ljava/lang/Object;)V", "foo(Ljava/lang/String;)Ljava/lang/String;",
              "$m$0(Ljava/lang/String;)Ljava/lang/String;") +
      new Class(lambda(PKG_001, SHA_001, 9), types(PKG_001, "TestB"))
          .fields("-$f0:Ljava/lang/Object;")
          .methods("<init>(Ljava/lang/Object;)V", "foo(Ljava/lang/String;)Ljava/lang/String;",
              "$m$0(Ljava/lang/String;)Ljava/lang/String;") +
      "";

  @Nonnull
  private static final String TEST001_PACKAGE = "" +
      new Class(lambda(PKG_001, SHA_001, 0), types(PKG_001, "I0"))
          .fields("$id:B")
          .methods("<init>(B)V", "$m$0()V", "$m$1()V", "$m$2()V", "foo()V") +
      new Class(lambda(PKG_001, SHA_001, 1), types(PKG_001, "I1"))
          .methods("<init>()V", "$m$0(I)I", "bar(I)I") +
      new Class(lambda(PKG_001, SHA_001, 2), types(PKG_001, "IA"))
          .fields("$id:B")
          .methods("<init>(B)V",
              "$m$0()Ljava/lang/Object;",
              "$m$1()Ljava/lang/Object;",
              "$m$2()Ljava/lang/Object;",
              "foo()Ljava/lang/Object;") +
      new Class(lambda(PKG_001, SHA_001, 3), types(PKG_001, "IB"))
          .methods("<init>()V",
              "$m$0()Ljava/lang/Integer;",
              "$m$1()Ljava/lang/Object;",
              "foo()Ljava/lang/Integer;",
              "foo()Ljava/lang/Object;") +
      new Class(lambda(PKG_001, SHA_001, 4), types(PKG_001, "TestB"))
          .fields("-$f0:Ljava/lang/Object;", "$id:B")
          .methods("<init>(BLjava/lang/Object;)V",
              "$m$0(Ljava/lang/String;)Ljava/lang/String;",
              "$m$1(Ljava/lang/String;)Ljava/lang/String;",
              "foo(Ljava/lang/String;)Ljava/lang/String;") +
      new Class(lambda(PKG_001, SHA_001, 5), types(PKG_001, "TestB"))
          .fields("-$f0:Ljava/lang/Object;", "-$f1:Ljava/lang/Object;", "$id:B")
          .methods("<init>(BLjava/lang/Object;Ljava/lang/Object;)V",
              "$m$0(Ljava/lang/String;)Ljava/lang/String;",
              "$m$1(Ljava/lang/String;)Ljava/lang/String;",
              "foo(Ljava/lang/String;)Ljava/lang/String;") +
      new Class(lambda(PKG_001, SHA_001, 6), types(PKG_001, "TestB"))
          .fields("-$f0:I", "-$f1:Ljava/lang/Object;", "$id:B")
          .methods("<init>(BILjava/lang/Object;)V",
              "$m$0(Ljava/lang/String;)Ljava/lang/String;",
              "$m$1(Ljava/lang/String;)Ljava/lang/String;",
              "foo(Ljava/lang/String;)Ljava/lang/String;") +
      new Class(lambda(PKG_001, SHA_001, 7), types(PKG_001, "TestB"))
          .fields("-$f0:Z", "-$f1:B", "-$f2:C", "-$f3:D", "-$f4:F",
              "-$f5:I", "-$f6:J", "-$f7:S", "-$f8:Ljava/lang/Object;", "$id:B")
          .methods("<init>(BZBCDFIJSLjava/lang/Object;)V",
              "$m$0(Ljava/lang/String;)Ljava/lang/String;",
              "$m$1(Ljava/lang/String;)Ljava/lang/String;",
              "$m$2(Ljava/lang/String;)Ljava/lang/String;",
              "foo(Ljava/lang/String;)Ljava/lang/String;");

  @Nonnull
  private static final String TEST001_PACKAGE_INTERFACE = "" +
      new Class(lambda(PKG_001, SHA_001, 0), types(PKG_001, "I0", "I1", "IA", "IB"))
          .fields("$id:B")
          .methods("<init>(B)V",
              "$m$0()Ljava/lang/Object;",
              "$m$1()Ljava/lang/Object;",
              "$m$2()Ljava/lang/Object;",
              "$m$3()Ljava/lang/Integer;",
              "$m$4()Ljava/lang/Object;",
              "$m$5(I)I",
              "$m$6()V",
              "$m$7()V",
              "$m$8()V",
              "bar(I)I",
              "foo()Ljava/lang/Integer;",
              "foo()Ljava/lang/Object;",
              "foo()V") +
      new Class(lambda(PKG_001, SHA_001, 1), types(PKG_001, "TestB"))
          .fields("$id:B", "-$f0:Ljava/lang/Object;")
          .methods("<init>(BLjava/lang/Object;)V",
              "$m$0(Ljava/lang/String;)Ljava/lang/String;",
              "$m$1(Ljava/lang/String;)Ljava/lang/String;",
              "foo(Ljava/lang/String;)Ljava/lang/String;") +
      new Class(lambda(PKG_001, SHA_001, 2), types(PKG_001, "TestB"))
          .fields("$id:B", "-$f0:Ljava/lang/Object;", "-$f1:Ljava/lang/Object;")
          .methods("<init>(BLjava/lang/Object;Ljava/lang/Object;)V",
              "$m$0(Ljava/lang/String;)Ljava/lang/String;",
              "$m$1(Ljava/lang/String;)Ljava/lang/String;",
              "foo(Ljava/lang/String;)Ljava/lang/String;") +
      new Class(lambda(PKG_001, SHA_001, 3), types(PKG_001, "TestB"))
          .fields("$id:B", "-$f0:I", "-$f1:Ljava/lang/Object;")
          .methods("<init>(BILjava/lang/Object;)V",
              "$m$0(Ljava/lang/String;)Ljava/lang/String;",
              "$m$1(Ljava/lang/String;)Ljava/lang/String;",
              "foo(Ljava/lang/String;)Ljava/lang/String;") +
      new Class(lambda(PKG_001, SHA_001, 4), types(PKG_001, "TestB"))
          .fields("$id:B", "-$f0:Z", "-$f1:B", "-$f2:C", "-$f3:D",
              "-$f4:F", "-$f5:I", "-$f6:J", "-$f7:S", "-$f8:Ljava/lang/Object;")
          .methods("<init>(BZBCDFIJSLjava/lang/Object;)V",
              "$m$0(Ljava/lang/String;)Ljava/lang/String;",
              "$m$1(Ljava/lang/String;)Ljava/lang/String;",
              "$m$2(Ljava/lang/String;)Ljava/lang/String;",
              "foo(Ljava/lang/String;)Ljava/lang/String;");

  @Test
  @Runtime
  public void test001() throws Exception {
    compileAndValidate(PKG_001,
        config(LambdaGroupingScope.NONE, /* interfaces: */false, /* stateless: */false),
        new LambdaClassesValidator(TEST001_NONE));

    compileAndValidate(PKG_001,
        config(LambdaGroupingScope.PACKAGE, /* interfaces: */false, /* stateless: */false),
        new LambdaClassesValidator(TEST001_PACKAGE));

    compileAndValidate(PKG_001,
        config(LambdaGroupingScope.PACKAGE, /* interfaces: */true, /* stateless: */false),
        new LambdaClassesValidator(TEST001_PACKAGE_INTERFACE));
  }

  // ===============================================================================================

  @Nonnull
  private static final String PKG_002 = "com.android.jack.optimizations.lambdas.test002";
  @Nonnull
  private static final String SHA_002 = "rK0qQUOvfPrsTJWpyc2f5mlYYJ8";

  @Nonnull
  private static final String TEST002_NONE = "" +
      new Class(lambda(PKG_002, SHA_002, 0), types(PKG_002, "I0"))
          .methods("<init>()V", "$m$0()Ljava/lang/String;", "foo()Ljava/lang/String;") +
      new Class(lambda(PKG_002, SHA_002, 1), types(PKG_002, "I1"))
          .methods("<init>()V", "$m$0()Ljava/lang/String;", "foo()Ljava/lang/String;") +
      new Class(lambda(PKG_002, SHA_002, 2), types(PKG_002, "I2"))
          .methods("<init>()V", "$m$0()Ljava/lang/String;", "foo()Ljava/lang/String;") +
      new Class(lambda(PKG_002, SHA_002, 3), types(PKG_002, "I3"))
          .methods("<init>()V", "$m$0()Ljava/lang/String;", "foo()Ljava/lang/String;") +
      new Class(lambda(PKG_002, SHA_002, 4), types(PKG_002, "I4"))
          .methods("<init>()V", "$m$0()Ljava/lang/String;", "foo()Ljava/lang/String;") +
      new Class(lambda(PKG_002, SHA_002, 5), types(PKG_002, "I5"))
          .methods("<init>()V", "$m$0()Ljava/lang/String;", "foo()Ljava/lang/String;") +
      new Class(lambda(PKG_002, SHA_002, 6), types(PKG_002, "I6"))
          .methods("<init>()V", "$m$0()Ljava/lang/String;", "foo()Ljava/lang/String;") +
      "";

  @Nonnull
  private static final String TEST002_PACKAGE_INTERFACE = "" +
      new Class(lambda(PKG_002, SHA_002, 0),
          types(PKG_002, "I0", "I1", "I2", "I3", "I4", "I5", "I6"))
          .fields("$id:B")
          .methods("<init>(B)V",
              "$m$0()Ljava/lang/String;",
              "$m$1()Ljava/lang/String;",
              "$m$2()Ljava/lang/String;",
              "$m$3()Ljava/lang/String;",
              "$m$4()Ljava/lang/String;",
              "$m$5()Ljava/lang/String;",
              "$m$6()Ljava/lang/String;",
              "foo()Ljava/lang/String;");

  @Test
  @Runtime
  public void test002() throws Exception {
    compileAndValidate(PKG_002,
        config(LambdaGroupingScope.NONE, /* interfaces: */false, /* stateless: */false),
        new LambdaClassesValidator(TEST002_NONE).
            andAlso(new DexFileTypesValidator()
                .insert(lambda(PKG_002, SHA_002, 0), new DexTypeAccessValidator(false))
                .insert(lambda(PKG_002, SHA_002, 1), new DexTypeAccessValidator(false))
                .insert(lambda(PKG_002, SHA_002, 2), new DexTypeAccessValidator(false))
                .insert(lambda(PKG_002, SHA_002, 3), new DexTypeAccessValidator(false))
                .insert(lambda(PKG_002, SHA_002, 4), new DexTypeAccessValidator(false))
                .insert(lambda(PKG_002, SHA_002, 5), new DexTypeAccessValidator(false))
                .insert(lambda(PKG_002, SHA_002, 6), new DexTypeAccessValidator(false))));

    compileAndValidate(PKG_002,
        config(LambdaGroupingScope.PACKAGE, /* interfaces: */true, /* stateless: */false),
        new LambdaClassesValidator(TEST002_PACKAGE_INTERFACE).
            andAlso(new DexFileTypesValidator()
                .insert(lambda(PKG_002, SHA_002, 0), new DexTypeAccessValidator(false))));
  }

  // ===============================================================================================

  @Nonnull
  private static final String PKG_003 = "com.android.jack.optimizations.lambdas.test003";
  @Nonnull
  private static final String SHA_003 = "ONjwHPCPJTN6WB4tZ9kgaFMFflk";

  @Nonnull
  private static final String TEST003_NONE = "" +
      new Class(lambda(PKG_003, SHA_003, 0), types(PKG_003, "I0"))
          .methods("<init>()V", "$m$0()Ljava/lang/Object;", "foo()Ljava/lang/Object;") +
      new Class(lambda(PKG_003, SHA_003, 1), types(PKG_003, "I1"))
          .methods("<init>()V", "$m$0()Ljava/lang/Object;", "foo()Ljava/lang/Object;") +
      new Class(lambda(PKG_003, SHA_003, 2), types(PKG_003, "I3"))
          .methods("<init>()V", "$m$0()Ljava/lang/Object;", "foo()Ljava/lang/Object;") +
      "";

  @Nonnull
  private static final String TEST003_PACKAGE = TEST003_NONE;

  @Nonnull
  private static final String TEST003_PACKAGE_INTERFACE = "" +
      new Class(lambda(PKG_003, SHA_003, 0), types(PKG_003, "I0", "I1", "I3"))
          .fields("$id:B")
          .methods("<init>(B)V", "$m$0()Ljava/lang/Object;",
              "$m$1()Ljava/lang/Object;", "$m$2()Ljava/lang/Object;", "foo()Ljava/lang/Object;");

  @Test
  @Runtime
  public void test003() throws Exception {
    compileAndValidate(PKG_003,
        config(LambdaGroupingScope.NONE, /* interfaces: */false, /* stateless: */false),
        new LambdaClassesValidator(TEST003_NONE));

    compileAndValidate(PKG_003,
        config(LambdaGroupingScope.PACKAGE, /* interfaces: */false, /* stateless: */false),
        new LambdaClassesValidator(TEST003_PACKAGE));

    compileAndValidate(PKG_003,
        config(LambdaGroupingScope.PACKAGE, /* interfaces: */true, /* stateless: */false),
        new LambdaClassesValidator(TEST003_PACKAGE_INTERFACE));
  }

  // ===============================================================================================

  @Nonnull
  private static final String PKG_004 = "com.android.jack.optimizations.lambdas.test004";
  @Nonnull
  private static final String SHA_004 = "kE8tIAUzNtiGWJ6ulIU0a1IEtL4";

  @Nonnull
  private static final String TEST004_PACKAGE = "" +
      new Class(lambda(PKG_004, SHA_004, 0), types(PKG_004, "I0"))
          .methods("<init>()V", "$m$0()Ljava/lang/String;", "foo()Ljava/lang/String;") +
      new Class(lambda(PKG_004, SHA_004, 1), types(PKG_004, "I0", "MarkerA"))
          .methods("<init>()V", "$m$0()Ljava/lang/String;", "foo()Ljava/lang/String;") +
      new Class(lambda(PKG_004, SHA_004, 2), types(PKG_004, "I0", "MarkerA", "MarkerB"))
          .methods("<init>()V", "$m$0()Ljava/lang/String;", "foo()Ljava/lang/String;") +
      new Class(lambda(PKG_004, SHA_004, 3), types(PKG_004, "I0", "MarkerB"))
          .methods("<init>()V", "$m$0()Ljava/lang/String;", "foo()Ljava/lang/String;") +
      new Class(lambda(PKG_004, SHA_004, 4), types(PKG_004, "I1", "MarkerA", "MarkerB"))
          .methods("<init>()V", "$m$0()Ljava/lang/String;", "foo()Ljava/lang/String;");

  @Nonnull
  private static final String TEST004_LAMBDA_MERGE = "" +
      new Class(lambda(PKG_004, SHA_004, 0), types(PKG_004, "I0", "I1", "MarkerA", "MarkerB"))
          .fields("$id:B")
          .methods("<init>(B)V",
              "$m$0()Ljava/lang/String;",
              "$m$1()Ljava/lang/String;",
              "$m$2()Ljava/lang/String;",
              "$m$3()Ljava/lang/String;",
              "$m$4()Ljava/lang/String;",
              "foo()Ljava/lang/String;");

  @Nonnull
  private static final String TEST004_PACKAGE_INTERFACE_STATELESS = "" +
      new Class(lambda(PKG_004, SHA_004, 0), types(PKG_004, "I0", "I1", "MarkerA", "MarkerB"))
          .fields("$id:B",
              "$INST$0:" + lambda(PKG_004, SHA_004, 0),
              "$INST$1:" + lambda(PKG_004, SHA_004, 0),
              "$INST$2:" + lambda(PKG_004, SHA_004, 0),
              "$INST$3:" + lambda(PKG_004, SHA_004, 0),
              "$INST$4:" + lambda(PKG_004, SHA_004, 0))
          .methods("<clinit>()V", "<init>(B)V",
              "$m$0()Ljava/lang/String;",
              "$m$1()Ljava/lang/String;",
              "$m$2()Ljava/lang/String;",
              "$m$3()Ljava/lang/String;",
              "$m$4()Ljava/lang/String;",
              "foo()Ljava/lang/String;");

  @Nonnull
  private static final String TEST004_PACKAGE_STATELESS = "" +
      new Class(lambda(PKG_004, SHA_004, 0), types(PKG_004, "I0"))
          .fields("$INST$0:" + lambda(PKG_004, SHA_004, 0))
          .methods("<clinit>()V", "<init>()V",
              "$m$0()Ljava/lang/String;",
              "foo()Ljava/lang/String;") +
      new Class(lambda(PKG_004, SHA_004, 1), types(PKG_004, "I0", "MarkerA"))
          .fields("$INST$0:" + lambda(PKG_004, SHA_004, 1))
          .methods("<clinit>()V", "<init>()V",
              "$m$0()Ljava/lang/String;",
              "foo()Ljava/lang/String;") +
      new Class(lambda(PKG_004, SHA_004, 2), types(PKG_004, "I0", "MarkerA", "MarkerB"))
          .fields("$INST$0:" + lambda(PKG_004, SHA_004, 2))
          .methods("<clinit>()V", "<init>()V",
              "$m$0()Ljava/lang/String;",
              "foo()Ljava/lang/String;") +
      new Class(lambda(PKG_004, SHA_004, 3), types(PKG_004, "I0", "MarkerB"))
          .fields("$INST$0:" + lambda(PKG_004, SHA_004, 3))
          .methods("<clinit>()V", "<init>()V",
              "$m$0()Ljava/lang/String;",
              "foo()Ljava/lang/String;") +
      new Class(lambda(PKG_004, SHA_004, 4), types(PKG_004, "I1", "MarkerA", "MarkerB"))
          .fields("$INST$0:" + lambda(PKG_004, SHA_004, 4))
          .methods("<clinit>()V", "<init>()V",
              "$m$0()Ljava/lang/String;",
              "foo()Ljava/lang/String;");

  @Test
  @Runtime
  public void test004() throws Exception {
    compileAndValidate(PKG_004,
        config(LambdaGroupingScope.PACKAGE, /* interfaces: */false, /* stateless: */false),
        new LambdaClassesValidator(TEST004_PACKAGE));

    compileAndValidate(PKG_004,
        config(LambdaGroupingScope.PACKAGE, /* interfaces: */true, /* stateless: */false),
        new LambdaClassesValidator(TEST004_LAMBDA_MERGE));

    compileAndValidate(PKG_004,
        config(LambdaGroupingScope.PACKAGE, /* interfaces: */true, /* stateless: */true),
        new LambdaClassesValidator(TEST004_PACKAGE_INTERFACE_STATELESS));

    compileAndValidate(PKG_004,
        config(LambdaGroupingScope.PACKAGE, /* interfaces: */false, /* stateless: */true),
        new LambdaClassesValidator(TEST004_PACKAGE_STATELESS));
  }

  // ===============================================================================================

  @Nonnull
  private static final String PKG_005 = "com.android.jack.optimizations.lambdas.test005";
  @Nonnull
  private static final String SHA_005 = "mEUip-rZCi8VUKLKTaQF6NNk0aM";

  @Nonnull
  private static final String TEST005_PACKAGE = "" +
      new Class(lambda(PKG_005, SHA_005, 0), types(PKG_005, "I0"))
          .fields("$id:B")
          .methods("<init>(B)V",
              "$m$0()Ljava/lang/String;",
              "$m$1()Ljava/lang/String;",
              "foo()Ljava/lang/String;") +
      new Class(lambda(PKG_005, SHA_005, 1), types(PKG_005, "I1"))
          .methods("<init>()V",
              "$m$0()Ljava/lang/String;",
              "bar()Ljava/lang/String;") +
      new Class(lambda(PKG_005, SHA_005, 2), types(PKG_005, "I0"))
          .fields("$id:B", "-$f0:Ljava/lang/Object;")
          .methods("<init>(BLjava/lang/Object;)V",
              "$m$0()Ljava/lang/String;",
              "$m$1()Ljava/lang/String;",
              "$m$2()Ljava/lang/String;",
              "$m$3()Ljava/lang/String;",
              "$m$4()Ljava/lang/String;",
              "foo()Ljava/lang/String;") +
      new Class(lambda(PKG_005, SHA_005, 3), types(PKG_005, "I1"))
          .fields("$id:B", "-$f0:Ljava/lang/Object;")
          .methods("<init>(BLjava/lang/Object;)V",
              "$m$0()Ljava/lang/String;",
              "$m$1()Ljava/lang/String;",
              "bar()Ljava/lang/String;");

  @Nonnull
  private static final String TEST005_PACKAGE_INTERFACE = "" +
      new Class(lambda(PKG_005, SHA_005, 0), types(PKG_005, "I0", "I1"))
          .fields("$id:B")
          .methods("<init>(B)V",
              "$m$0()Ljava/lang/String;",
              "$m$1()Ljava/lang/String;",
              "$m$2()Ljava/lang/String;",
              "bar()Ljava/lang/String;",
              "foo()Ljava/lang/String;") +
      new Class(lambda(PKG_005, SHA_005, 1), types(PKG_005, "I0", "I1"))
          .fields("$id:B", "-$f0:Ljava/lang/Object;")
          .methods("<init>(BLjava/lang/Object;)V",
              "$m$0()Ljava/lang/String;",
              "$m$1()Ljava/lang/String;",
              "$m$2()Ljava/lang/String;",
              "$m$3()Ljava/lang/String;",
              "$m$4()Ljava/lang/String;",
              "$m$5()Ljava/lang/String;",
              "$m$6()Ljava/lang/String;",
              "bar()Ljava/lang/String;",
              "foo()Ljava/lang/String;");

  @Nonnull
  private static final String TEST005_PACKAGE_INTERFACE_STATELESS = "" +
      new Class(lambda(PKG_005, SHA_005, 0), types(PKG_005, "I0", "I1"))
          .fields("$id:B",
              "$INST$0:" + lambda(PKG_005, SHA_005, 0),
              "$INST$1:" + lambda(PKG_005, SHA_005, 0),
              "$INST$2:" + lambda(PKG_005, SHA_005, 0))
          .methods("<clinit>()V", "<init>(B)V",
              "$m$0()Ljava/lang/String;",
              "$m$1()Ljava/lang/String;",
              "$m$2()Ljava/lang/String;",
              "bar()Ljava/lang/String;",
              "foo()Ljava/lang/String;") +
      new Class(lambda(PKG_005, SHA_005, 1), types(PKG_005, "I0", "I1"))
          .fields("$id:B", "-$f0:Ljava/lang/Object;")
          .methods("<init>(BLjava/lang/Object;)V",
              "$m$0()Ljava/lang/String;",
              "$m$1()Ljava/lang/String;",
              "$m$2()Ljava/lang/String;",
              "$m$3()Ljava/lang/String;",
              "$m$4()Ljava/lang/String;",
              "$m$5()Ljava/lang/String;",
              "$m$6()Ljava/lang/String;",
              "bar()Ljava/lang/String;",
              "foo()Ljava/lang/String;");

  @Test
  @Runtime
  public void test005() throws Exception {
    compileAndValidate(PKG_005,
        config(LambdaGroupingScope.PACKAGE, /* interfaces: */false, /* stateless: */false),
        new LambdaClassesValidator(TEST005_PACKAGE));

    compileAndValidate(PKG_005,
        config(LambdaGroupingScope.PACKAGE, /* interfaces: */true, /* stateless: */false),
        new LambdaClassesValidator(TEST005_PACKAGE_INTERFACE));

    compileAndValidate(PKG_005,
        config(LambdaGroupingScope.PACKAGE, /* interfaces: */true, /* stateless: */true),
        new LambdaClassesValidator(TEST005_PACKAGE_INTERFACE_STATELESS));
  }

  // ===============================================================================================

  @Nonnull
  private static final String PKG_006 = "com.android.jack.optimizations.lambdas.test006";
  @Nonnull
  private static final String SHA_006_A = "wxKW61Ib1whleO_n58u3HsvIMUM";
  @Nonnull
  private static final String SHA_006_B = "N-kUn0qIgGhcb5JJeaFqgDggm3E";

  @Nonnull
  private static final String TEST006_NONE = "" +
      new Class(lambda(PKG_006, SHA_006_A, 0), types(PKG_006, "Producer"))
          .methods("<init>()V", "$m$0()Ljava/lang/String;", "run()Ljava/lang/String;") +
      new Class(lambda(PKG_006, SHA_006_A, 1), types(PKG_006, "Producer"))
          .methods("<init>()V", "$m$0()Ljava/lang/String;", "run()Ljava/lang/String;") +
      new Class(lambda(PKG_006, SHA_006_A, 2), types(PKG_006, "Producer"))
          .methods("<init>()V", "$m$0()Ljava/lang/String;", "run()Ljava/lang/String;") +
      new Class(lambda(PKG_006, SHA_006_A, 3), types(PKG_006, "Producer"))
          .methods("<init>()V", "$m$0()Ljava/lang/String;", "run()Ljava/lang/String;") +
      new Class(lambda(PKG_006, SHA_006_B, 4), types(PKG_006, "Producer"))
          .methods("<init>()V", "$m$0()Ljava/lang/String;", "run()Ljava/lang/String;") +
      new Class(lambda(PKG_006, SHA_006_B, 5), types(PKG_006, "Producer"))
          .methods("<init>()V", "$m$0()Ljava/lang/String;", "run()Ljava/lang/String;") +
      new Class(lambda(PKG_006, SHA_006_B, 6), types(PKG_006, "Producer"))
          .methods("<init>()V", "$m$0()Ljava/lang/String;", "run()Ljava/lang/String;") +
      "";

  @Nonnull
  private static final String TEST006_TYPE = "" +
      new Class(lambda(PKG_006, SHA_006_A, 0), types(PKG_006, "Producer"))
          .fields("$id:B")
          .methods("<init>(B)V", "$m$0()Ljava/lang/String;", "$m$1()Ljava/lang/String;",
              "$m$2()Ljava/lang/String;", "$m$3()Ljava/lang/String;", "run()Ljava/lang/String;") +
      new Class(lambda(PKG_006, SHA_006_B, 1), types(PKG_006, "Producer"))
          .fields("$id:B")
          .methods("<init>(B)V", "$m$0()Ljava/lang/String;", "$m$1()Ljava/lang/String;",
              "$m$2()Ljava/lang/String;", "run()Ljava/lang/String;") +
      "";

  @Nonnull
  private static final String SHA_006_ALL = "aRztprv3kbtT1k5EHkFo2yXbFfg";

  @Nonnull
  private static final String TEST006_PACKAGE = "" +
      new Class(lambda(PKG_006, SHA_006_ALL, 0), types(PKG_006, "Producer"))
          .fields("$id:B")
          .methods("<init>(B)V", "$m$0()Ljava/lang/String;", "$m$1()Ljava/lang/String;",
              "$m$2()Ljava/lang/String;", "$m$3()Ljava/lang/String;",
              "$m$4()Ljava/lang/String;", "$m$5()Ljava/lang/String;",
              "$m$6()Ljava/lang/String;", "run()Ljava/lang/String;") +
      "";

  @Test
  @Runtime
  public void test006() throws Exception {
    compileAndValidate(PKG_006,
        config(LambdaGroupingScope.NONE, /* interfaces: */false, /* stateless: */false),
        new LambdaClassesValidator(TEST006_NONE));

    compileAndValidate(PKG_006,
        config(LambdaGroupingScope.TYPE, /* interfaces: */false, /* stateless: */false),
        new LambdaClassesValidator(TEST006_TYPE));

    compileAndValidate(PKG_006,
        config(LambdaGroupingScope.PACKAGE, /* interfaces: */false, /* stateless: */false),
        new LambdaClassesValidator(TEST006_PACKAGE));
  }

  // ===============================================================================================

  @Nonnull
  private static final String PKG_007 = "com.android.jack.optimizations.lambdas.test007";
  @Nonnull
  private static final String SHA_007 = "VIf7bh4a9R2eWK07TARcQzoADH4";

  @Nonnull
  private static final String TEST007_DEFAULT = "" +
      new Class(lambda(PKG_007, SHA_007, 0), types(PKG_007, "Ic", "Ia", "Ib"))
          .methods("<init>()V", "$m$0()Ljava/lang/String;", "m()Ljava/lang/String;") +
      new Class(lambda(PKG_007, SHA_007, 1), types(PKG_007, "Ic", "Iaa"))
          .methods("<init>()V", "$m$0()Ljava/lang/String;", "m()Ljava/lang/String;") +
      new Class(lambda(PKG_007, SHA_007, 2), types(PKG_007, "Ic", "Ib"))
          .methods("<init>()V", "$m$0()Ljava/lang/String;", "m()Ljava/lang/String;") +
      new Class(lambda(PKG_007, SHA_007, 3), types(PKG_007, "Ic", "Ib", "Ia"))
          .methods("<init>()V", "$m$0()Ljava/lang/String;", "m()Ljava/lang/String;") +
      "";

  @Nonnull
  private static final String TEST007_MERGE_INTERFACES = "" +
      new Class(lambda(PKG_007, SHA_007, 0), types(PKG_007, "Ia", "Iaa", "Ib", "Ic"))
          .fields("$id:B")
          .methods("<init>(B)V", "m()Ljava/lang/String;",
              "$m$0()Ljava/lang/String;", "$m$1()Ljava/lang/String;",
              "$m$2()Ljava/lang/String;", "$m$3()Ljava/lang/String;") +
      "";


  @Test
  @Runtime
  public void test007() throws Exception {
    compileAndValidate(PKG_007,
        config(LambdaGroupingScope.TYPE, /* interfaces: */false, /* stateless: */false)
            .excludeJillToolchain(),
        new LambdaClassesValidator(TEST007_DEFAULT));

    compileAndValidate(PKG_007,
        config(LambdaGroupingScope.TYPE, /* interfaces: */true, /* stateless: */false)
            .excludeJillToolchain(),
        new LambdaClassesValidator(TEST007_MERGE_INTERFACES));
  }

  // ===============================================================================================

  @Nonnull
  private static final String PKG_008 = "com.android.jack.optimizations.lambdas.test008";

  @Test
  @Runtime
  public void test008() throws Exception {
    String pkgDx = PKG_008 + "dx";
    String pkgJack = PKG_008 + "jack";

    IncrementalTestHelper ite =
        new IncrementalTestHelper(AbstractTestTools.createTempDir());

    // Enable Java8
    ite.addProperty("jack.java.source.version", "1.8");

    ite.addJavaFile(pkgDx, "Tests.java", Files.toString(
        new File(new File(AbstractTestTools.getTestRootDir(PKG_008), "dx"), "Tests.java"),
        Charset.defaultCharset()));

    ite.addJavaFile(pkgJack, "B.java", Files.toString(
        new File(new File(AbstractTestTools.getTestRootDir(PKG_008), "jack"), "B.java"),
        Charset.defaultCharset()));

    ite.incrementalBuildFromFolder();

    // Run runtime tests
    RuntimeTestHelper.runOnRuntimeEnvironments(
        Collections.singletonList(PKG_008 + ".dx.Tests"),
        RuntimeTestHelper.getJunitDex(), ite.getDexFile());

    ite.addJavaFile(pkgJack, "A.java", Files.toString(
        new File(new File(AbstractTestTools.getTestRootDir(PKG_008), "jack"), "A.java"),
        Charset.defaultCharset()));

    ite.incrementalBuildFromFolder();

    // Run runtime tests
    RuntimeTestHelper.runOnRuntimeEnvironments(
        Collections.singletonList(PKG_008 + ".dx.Tests"),
        RuntimeTestHelper.getJunitDex(), ite.getDexFile());
  }
}
