/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.jack.transformations.cast;


import com.android.jack.Options;
import com.android.jack.TestTools;
import com.android.jack.backend.dex.DexFileWriter;
import com.android.jack.ir.JNodeInternalError;
import com.android.jack.ir.ast.JCastOperation;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JVisitor;

import junit.framework.Assert;

import org.jf.dexlib.CodeItem;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

import javax.annotation.Nonnull;

public class UselessCastRemoverTest {

  @Nonnull
  private static final String CAST = "com/android/jack/transformations/cast/jack/Data";

  @Nonnull
  private static final String CAST_USELESS002 = "com/android/jack/cast/useless002/jack/UselessCast";
  @Nonnull
  private static final String CAST_USELESS003 = "com/android/jack/cast/useless003/jack/UselessCast";

  @BeforeClass
  public static void setUp() throws Exception {
    UselessCastRemoverTest.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  @Test
  public void castObjectToArray() throws Exception {
    final String methodSignature = "castObjectToArray(Ljava/lang/Object;)[Ljava/lang/Object;";
    buildMethodAndCheckUselessCastRemover(CAST, methodSignature, false);
  }

  @Test
  public void castObjectToObject() throws Exception {
    final String methodSignature = "castObjectToObject(Ljava/lang/Object;)Ljava/lang/Object;";

    buildMethodAndCheckUselessCastRemover(CAST, methodSignature, true);
  }

  @Test
  public void castStringToObject() throws Exception {
    final String methodSignature = "castStringToObject(Ljava/lang/String;)Ljava/lang/Object;";

    buildMethodAndCheckUselessCastRemover(CAST, methodSignature, true);
  }

  @Test
  public void castStringToSerializable() throws Exception {
    final String methodSignature =
        "castStringToSerializable(Ljava/lang/String;)Ljava/io/Serializable;";

    buildMethodAndCheckUselessCastRemover(CAST, methodSignature, true);
  }

  @Test
  public void castArrayToCloneable() throws Exception {
    final String methodSignature = "castArrayToCloneable([I)Ljava/lang/Cloneable;";

    buildMethodAndCheckUselessCastRemover(CAST, methodSignature, true);
  }

  @Test
  public void castNullToString() throws Exception {
    final String methodSignature = "castNullToString()Ljava/lang/String;";

    buildMethodAndCheckUselessCastRemover(CAST, methodSignature, true);
  }

  @Test
  public void castExceptionToThrowable() throws Exception {
    final String methodSignature =
        "castExceptionToThrowable(Ljava/lang/Exception;)Ljava/lang/Throwable;";

    buildMethodAndCheckUselessCastRemover(CAST, methodSignature, true);
  }

  @Test
  public void castCharacterIteratorToCloneable() throws Exception {
    final String methodSignature =
        "castCharacterIteratorToCloneable(Ljava/text/CharacterIterator;)Ljava/lang/Cloneable;";

    buildMethodAndCheckUselessCastRemover(CAST, methodSignature, true);
  }

  @Test
  public void castMultiToObjectArray() throws Exception {
    final String methodSignature =
        "castMultiToObjectArray([[I)[Ljava/lang/Object;";

    buildMethodAndCheckUselessCastRemover(CAST, methodSignature, true);
  }

  @Test
  public void castStringArrayToObjectArray() throws Exception {
    final String methodSignature =
        "castStringArrayToObjectArray([Ljava/lang/String;)[Ljava/lang/Object;";

    buildMethodAndCheckUselessCastRemover(CAST, methodSignature, true);
  }

  @Test
  public void castExternalTypes() throws Exception {
    final String methodSignature =
        "castExternalTypes(Ljava/net/Inet4Address;)Ljava/net/InetAddress;";

    buildMethodAndCheckUselessCastRemover(CAST, methodSignature, true);
  }

  @Test
  @Ignore("Suppress optimization removing the cast since it requires to have all usages on a variable" +
      " and not only on definitions")
  public void primitiveCastDueToConditional() throws Exception {
    buildMethodAndCheckUselessCastRemover(CAST_USELESS002, "uselessCast(III)J", true);
  }

  @Test
  public void castDueToNestedAssign() throws Exception {
    buildMethodAndCheckUselessCastRemover(CAST_USELESS003, "nestedAssign()V", true);
  }

  @Test
  public void test001() throws Exception {
    File outFolder = TestTools.createTempDir("uselessCastInstructions", "dex");
    File out = new File(outFolder, DexFileWriter.DEX_FILENAME);

    TestTools.compileSourceToDex(new Options(),
        TestTools.getJackTestsWithJackFolder("cast/test001"),
        TestTools.getClasspathAsString(TestTools.getDefaultBootclasspath()), outFolder, false);

    DexFile dexFile = new DexFile(out);
    CodeItem ci =
        TestTools.getEncodedMethod(dexFile,
            "Lcom/android/jack/cast/test001/jack/Test001;", "get",
            "(Lcom/android/jack/cast/test001/jack/Test001;)I").codeItem;

    Assert.assertFalse(hasOpcode(ci, Opcode.CHECK_CAST));
    Assert.assertFalse(hasOpcode(ci, Opcode.CHECK_CAST_JUMBO));
  }

  @Test
  public void test002() throws Exception {
    File outFolder = TestTools.createTempDir("uselessCastInstructions", "dex");
    File out = new File(outFolder, DexFileWriter.DEX_FILENAME);
    TestTools.compileSourceToDex(new Options(),
        TestTools.getJackTestsWithJackFolder("cast/test002"),
        TestTools.getClasspathAsString(TestTools.getDefaultBootclasspath()), outFolder, false);

    DexFile dexFile = new DexFile(out);
    CodeItem ci =
        TestTools.getEncodedMethod(dexFile,
            "Lcom/android/jack/cast/test002/jack/Test002;", "get",
            "(Lcom/android/jack/cast/test002/jack/Test002;)I").codeItem;

    Assert.assertFalse(hasOpcode(ci, Opcode.CHECK_CAST));
    Assert.assertFalse(hasOpcode(ci, Opcode.CHECK_CAST_JUMBO));
  }

  @Test
  public void test003() throws Exception {
    File outFolder = TestTools.createTempDir("uselessCastInstructions", "dex");
    File out = new File(outFolder, DexFileWriter.DEX_FILENAME);
    TestTools.compileSourceToDex(new Options(),
        TestTools.getJackTestsWithJackFolder("cast/test003"),
        TestTools.getClasspathAsString(TestTools.getDefaultBootclasspath()), outFolder, false);

    DexFile dexFile = new DexFile(out);
    CodeItem ci =
        TestTools.getEncodedMethod(dexFile,
            "Lcom/android/jack/cast/test003/jack/Test003;", "get",
            "(Lcom/android/jack/cast/test003/jack/A;)I").codeItem;

    Assert.assertFalse(hasOpcode(ci, Opcode.CHECK_CAST));
    Assert.assertFalse(hasOpcode(ci, Opcode.CHECK_CAST_JUMBO));
  }

  private boolean hasOpcode(@Nonnull CodeItem codeItem, @Nonnull Opcode opcode) {
    for (Instruction inst : codeItem.getInstructions()) {
      if (inst.opcode == opcode) {
        return true;
      }
    }
    return false;
  }

  private static void buildMethodAndCheckUselessCastRemover(@Nonnull String classBinaryName,
      @Nonnull String methodSignature, boolean castRemoved) throws Exception {
    JMethod m =
        TestTools.getJMethodWithSignatureFilter(
            TestTools.getJackTestFromBinaryName(classBinaryName), "L" + classBinaryName + ";",
            methodSignature);
    Assert.assertNotNull(m);

    try {
      new UselessCastChecker(m).accept(m);
      if (!castRemoved) {
        Assert.fail("Cast not removed");
      }
    } catch (JNodeInternalError e) {
      Throwable cause = e;
      while (cause.getCause() != null) {
        cause = cause.getCause();
      }
      if (cause instanceof CastExists) {
        if (castRemoved) {
          Assert.fail("Cast not removed");
        }
      } else {
        Assert.fail(cause.getMessage());
      }
    }
  }

  private static class CastExists extends RuntimeException {
    private static final long serialVersionUID = 1L;
  }

  private static class UselessCastChecker extends JVisitor {

    @Nonnull
    private final JMethod currentMethod;

    public UselessCastChecker(@Nonnull JMethod currentMethod) {
      this.currentMethod = currentMethod;
    }

    @Override
    public boolean visit(@Nonnull JMethod method) {
      return method == currentMethod;
    }

    @Override
    public void endVisit(@Nonnull JCastOperation cast) {
      throw new CastExists();
    }
  }
}