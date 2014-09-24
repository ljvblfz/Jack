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

package com.android.jack.errorhandling;

import com.android.jack.Options;
import com.android.jack.TestTools;
import com.android.jack.frontend.FrontendCompilationException;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * JUnit test checking Jack behavior with source errors.
 */
public class SourceErrorTest {

  /**
   * Checks that compilation fails because of invalid "class" keyword.
   * parsingErrorId = syntaxCategory + internalCategory + 204
   */
  @Test
  public void testInvalidSource001() throws Exception {
    TestingEnvironment te = new TestingEnvironment();

    te.addFile(te.getSourceFolder(), "jack.invalidsource", "A.java", "package jack.invalidsource;\n"
        + "public clas A {}\n");

    try {
      te.startOutRedirection();
      te.startErrRedirection();
      te.compile(getOptions(te));
      Assert.fail();
    } catch (FrontendCompilationException ex) {
      // Failure is ok since source does not compile.
    } finally {
      Assert.assertEquals("", te.endOutRedirection());
      Assert.assertTrue(
          te.endErrRedirection().contains("Syntax error on token \"clas\", class expected"));
    }
  }

  /**
   * Checks that compilation fails because of invalid "public" keyword.
   * parsingErrorId = syntaxCategory + internalCategory + 204
   */
  @Test
  public void testInvalidSource002() throws Exception {
    TestingEnvironment te = new TestingEnvironment();

    te.addFile(te.getSourceFolder(), "jack.invalidsource", "A.java", "package jack.invalidsource;\n"
        + "publi class A {}\n");

    try {
      te.startOutRedirection();
      te.startErrRedirection();
      te.compile(getOptions(te));
      Assert.fail();
    } catch (FrontendCompilationException ex) {
      // Failure is ok since source does not compile.
    } finally {
      Assert.assertEquals("", te.endOutRedirection());
      Assert.assertTrue(
          te.endErrRedirection().contains("Syntax error on token \"publi\", public expected"));
    }
  }

  /**
   * Checks that compilation fails because of a class name that does not match the file name.
   * publicClassMustMatchFileNameId = typeRelatedCategory + 325
   */
  @Test
  public void testInvalidSource003() throws Exception {
    TestingEnvironment te = new TestingEnvironment();

    te.addFile(te.getSourceFolder(), "jack.invalidsource", "A.java", "package jack.invalidsource;\n"
        + "public class B {}\n");

    try {
      te.startOutRedirection();
      te.startErrRedirection();
      te.compile(getOptions(te));
      Assert.fail();
    } catch (FrontendCompilationException ex) {
      // Failure is ok since source does not compile.
    } finally {
      Assert.assertEquals("", te.endOutRedirection());
      Assert.assertTrue(
          te.endErrRedirection().contains("The public type B must be defined in its own file"));
    }
  }

  /**
   * Checks that compilation fails because of an import of a class that is not on classpath.
   * importNotFoundId = importRelatedCategory + 390
   */
  @Test
  public void testInvalidSource004() throws Exception {
    TestingEnvironment te = new TestingEnvironment();

    te.addFile(te.getSourceFolder(), "jack.invalidsource", "A.java", "package jack.invalidsource;\n"
        + "import jack.invalidsource.B;\n"
        + "public class A {}\n");

    try {
      te.startOutRedirection();
      te.startErrRedirection();
      te.compile(getOptions(te));
      Assert.fail();
    } catch (FrontendCompilationException ex) {
      // Failure is ok since source does not compile.
    } finally {
      Assert.assertEquals("", te.endOutRedirection());
      Assert.assertTrue(
          te.endErrRedirection().contains("The import jack.invalidsource.B cannot be resolved"));
    }
  }

  /**
   * Checks that compilation fails because there are too many methods in a single class.
   * tooManyMethodsId = internalCategory + 433
   */
  @Test
  public void testInvalidSource005() throws Exception {
    TestingEnvironment te = new TestingEnvironment();

    int methodCount = 65536;
    StringBuilder content =
        new StringBuilder("public class A {");
    // -1 due to implicit init method
    for (int mthIdx = 0; mthIdx < methodCount - 1; mthIdx++) {
      content.append("public void m" + mthIdx + "() {} \n");
    }
    content.append("} \n");

    te.addFile(te.getSourceFolder(), "jack.overflow", "A.java",
        "package jack.overflow; \n" + content.toString());

    try {
      te.startErrRedirection();
      te.startOutRedirection();
      te.compile(getOptions(te));
      Assert.fail();
    } catch (FrontendCompilationException e) {
      // Failure is ok, since there are too many methods.
    } finally {
      Assert.assertEquals("", te.endOutRedirection());
      Assert.assertTrue(
          te.endErrRedirection().contains("Too many methods for type A. Maximum is 65535"));
    }
  }

  /**
   * Checks that compilation fails because of several source errors.
   */
  @Test
  public void testInvalidSource006() throws Exception {
    TestingEnvironment te = new TestingEnvironment();

    te.addFile(te.getSourceFolder(), "jack.invalidsource", "A.java", "package jack.invalidsource;\n"
        + "public class A { private voi m() {} } \n");
    te.addFile(te.getSourceFolder(), "jack.invalidsource", "B.java", "package jack.invalidsource;\n"
        + "public class B { private void m(in a) {}; \n private void n(int a) {re}; } \n");

    try {
      te.startOutRedirection();
      te.startErrRedirection();
      te.compile(getOptions(te));
      Assert.fail();
    } catch (FrontendCompilationException ex) {
      // Failure is ok since source does not compile.
    } finally {
      Assert.assertEquals("", te.endOutRedirection());
      String errorString = te.endErrRedirection();
      Assert.assertTrue(errorString.contains("in cannot be resolved to a type"));
      Assert.assertTrue(errorString.contains(
          "Syntax error, insert \"VariableDeclarators\" to complete LocalVariableDeclaration"));
      Assert.assertTrue(
          errorString.contains("Syntax error, insert \";\" to complete BlockStatements"));
      Assert.assertTrue(errorString.contains("voi cannot be resolved to a type"));
    }
  }

  /**
   * Checks that compilation fails because of a source error, with also some warnings.
   */
  @Test
  public void testInvalidSource007() throws Exception {
    TestingEnvironment te = new TestingEnvironment();

    te.addFile(te.getSourceFolder(), "jack.invalidsource", "A.java", "package jack.invalidsource;\n"
        + "public class A { private void m() {} } \n");
    te.addFile(te.getSourceFolder(), "jack.invalidsource", "B.java", "package jack.invalidsource;\n"
        + "public class B { private void m(in a) {}; \n private void n(int a) {}; } \n");

    try {
      te.startOutRedirection();
      te.startErrRedirection();
      te.compile(getOptions(te));
      Assert.fail();
    } catch (FrontendCompilationException ex) {
      // Failure is ok since source does not compile.
    } finally {
      Assert.assertEquals("", te.endOutRedirection());
      Assert.assertTrue(te.endErrRedirection().contains("in cannot be resolved to a type"));
      Assert.assertTrue(te.endErrRedirection().contains(
          "The method n(int) from the type B is never used locally"));
      Assert.assertTrue(
          te.endErrRedirection().contains("The method m() from the type A is never used locally"));
    }
  }

  /**
   * Checks that compilation succeeds but prints several warnings.
   */
  @Test
  public void testInvalidSource008() throws Exception {
    TestingEnvironment te = new TestingEnvironment();

    te.addFile(te.getSourceFolder(), "jack.invalidsource", "A.java", "package jack.invalidsource;\n"
        + "public class A { private void m() {} } \n");
    te.addFile(te.getSourceFolder(), "jack.invalidsource", "B.java", "package jack.invalidsource;\n"
        + "public class B { private void m(int a) {}; \n private void n(int a) {}; } \n");

    try {
      te.startOutRedirection();
      te.startErrRedirection();
      te.compile(getOptions(te));
    } finally {
      Assert.assertEquals("", te.endOutRedirection());
      Assert.assertTrue(te.endErrRedirection().contains(
          "The method m(int) from the type B is never used locally"));
      Assert.assertTrue(te.endErrRedirection().contains(
          "The method n(int) from the type B is never used locally"));
      Assert.assertTrue(
          te.endErrRedirection().contains("The method m() from the type A is never used locally"));
    }
  }

  /**
   * Checks that compilation fails because of an invalid type.
   * undefinedTypeId = typeRelatedCategory + 2
   */
  @Test
  public void testInvalidSource009() throws Exception {
    TestingEnvironment te = new TestingEnvironment();

    te.addFile(te.getSourceFolder(), "jack.invalidsource", "A.java", "package jack.invalidsource;\n"
        + "public class A { private void m(in a) {}; } \n");

    try {
      te.startOutRedirection();
      te.startErrRedirection();
      te.compile(getOptions(te));
      Assert.fail();
    } catch (FrontendCompilationException ex) {
      // Failure is ok since source does not compile.
    } finally {
      Assert.assertEquals("", te.endOutRedirection());
      Assert.assertTrue(
          te.endErrRedirection().contains("in cannot be resolved to a type"));
    }
  }

  /**
   * Checks that compilation fails because of a parsing error.
   * parsingErrorInsertToCompleteId = syntaxCategory + internalCategory + 240
   */
  @Test
  public void testInvalidSource010() throws Exception {
    TestingEnvironment te = new TestingEnvironment();

    te.addFile(te.getSourceFolder(), "jack.invalidsource", "A.java", "package jack.invalidsource;\n"
        + "public class A { private void n(int a) {re;} } \n");

    try {
      te.startOutRedirection();
      te.startErrRedirection();
      te.compile(getOptions(te));
      Assert.fail();
    } catch (FrontendCompilationException ex) {
      // Failure is ok since source does not compile.
    } finally {
      Assert.assertEquals("", te.endOutRedirection());
      Assert.assertTrue(te.endErrRedirection().contains(
          "Syntax error, insert \"VariableDeclarators\" to complete LocalVariableDeclaration"));
    }
  }

  /**
   * Checks that compilation succeeds but raises a warning because of an unused private method.
   * unusedPrivateMethodId = internalCategory + methodRelatedCategory + 118
   */
  @Test
  public void testInvalidSource011() throws Exception {
    TestingEnvironment te = new TestingEnvironment();

    te.addFile(te.getSourceFolder(), "jack.invalidsource", "A.java", "package jack.invalidsource;\n"
        + "public class A { private void m() {} } \n");

    try {
      te.startOutRedirection();
      te.startErrRedirection();
      te.compile(getOptions(te));
    } finally {
      Assert.assertEquals("", te.endOutRedirection());
      Assert.assertTrue(
          te.endErrRedirection().contains("The method m() from the type A is never used locally"));
    }
  }

  @Nonnull
  private Options getOptions(@Nonnull TestingEnvironment te) {
    Options options = new Options();
    List<String> ecjArgs = new ArrayList<String>();
    ecjArgs.add(te.getSourceFolder().getAbsolutePath());
    options.setEcjArguments(ecjArgs);
    options.setClasspath(TestTools.getDefaultBootclasspathString() + File.pathSeparator
        + te.getJackFolder());
    options.setOutputDir(te.getTestingFolder());
    return options;
  }

}
