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

package com.android.jack.experimental.incremental;

import com.android.jack.Main;
import com.android.jack.analysis.dependency.type.TypeDependencies;
import com.android.jack.library.FileType;
import com.android.jack.library.FileTypeDoesNotExistException;
import com.android.jack.library.InputJackLibrary;
import com.android.jack.test.helper.IncrementalTestHelper;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.sched.util.file.CannotReadException;
import com.android.sched.vfs.InputVFile;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * JUnit test checking dependencies between Java files.
 */
public class DependenciesTest009 {



  /**
   * Check that usages does not change during incremental compilation and that dependencies are
   * identical.
   */
  @Test
  public void testDependency001() throws Exception {
    IncrementalTestHelper ite =
        new IncrementalTestHelper(AbstractTestTools.createTempDir());

    ite.setIsApiTest();

    ite.addJavaFile("jack.incremental", "A.java", "package jack.incremental; \n"
        + "public class A extends B { }");

    ite.addJavaFile("jack.incremental", "B.java", "package jack.incremental; \n"
        + "public class B extends C { }");

    ite.addJavaFile("jack.incremental", "C.java", "package jack.incremental; \n"
        + "public class C { }");

    ite.incrementalBuildFromFolder();

    InputJackLibrary inputJackLibrary = null;
    try {
      inputJackLibrary = AbstractTestTools.getInputJackLibrary(ite.getCompilerStateFolder());

      TypeDependencies typeDependencies = readTypeDependencies(inputJackLibrary);

      Map<String, Set<String>> dependencies1 = typeDependencies.getRecompileDependencies();

      ite.addJavaFile("jack.incremental", "A.java",
          "package jack.incremental; \n" + "public class A extends B { public int field;}");

      ite.incrementalBuildFromFolder();

      typeDependencies = readTypeDependencies(inputJackLibrary);
      Map<String, Set<String>> dependencies2 = typeDependencies.getRecompileDependencies();

      assert dependencies1.equals(dependencies2);
      Assert.assertEquals(dependencies1, dependencies2);
    } finally {
      if (inputJackLibrary != null) {
        inputJackLibrary.close();
      }
    }
  }

  @Nonnull
  private static TypeDependencies readTypeDependencies(InputJackLibrary inputJackLibrary)
      throws FileTypeDoesNotExistException, CannotReadException {
    TypeDependencies typeDependencies = new TypeDependencies();
    InputVFile typeDependenciesVFile =
        inputJackLibrary.getFile(FileType.DEPENDENCIES, TypeDependencies.vpath);
    InputStreamReader reader = null;
    try {
      reader = new InputStreamReader(typeDependenciesVFile.getInputStream());
      typeDependencies.read(reader);
    } catch (NoSuchElementException e) {
      throw new CannotReadException(typeDependenciesVFile, e);
    } catch (IOException e) {
      throw new CannotReadException(typeDependenciesVFile, e);
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException e) {
        }
      }
    }
    return typeDependencies;
  }
}
