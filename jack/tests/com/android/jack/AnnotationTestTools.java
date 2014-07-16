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

package com.android.jack;

import java.io.File;
import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Tools for annotation tests
 */
public class AnnotationTestTools {

  public static void checkStructure(@CheckForNull File[] bootclasspath,
      @CheckForNull File[] classpath, @Nonnull File fileOrSourceList,
      ProguardFlags[] proguardFlags) throws Exception {

    String classpathStr = TestTools.getClasspathsAsString(bootclasspath, classpath);

    File jackDex = TestTools.createTempFile("jackdex", ".dex");

    boolean useEcjAsRefCompiler = false;

    TestTools.compileSourceToDex(new Options(),
        fileOrSourceList,
        classpathStr,
        jackDex,
        false /* zip */,
        null /* jarjarRules */,
        proguardFlags,
        false /* withDebugInfo */);

    Options refOptions = TestTools.buildCommandLineArgs(bootclasspath, classpath, fileOrSourceList);

    compareDexToReference(jackDex,
        refOptions,
        proguardFlags,
        bootclasspath,
        classpath,
        useEcjAsRefCompiler,
        null);
  }

  private static void compareDexToReference(@Nonnull File jackDex,
      @Nonnull Options compilerArgs,
      @CheckForNull ProguardFlags[] proguardFlags,
      @CheckForNull File[] bootclasspath,
      @CheckForNull File[] classpath,
      boolean useEcjAsRefCompiler,
      @CheckForNull JarJarRules jarjarRules)
      throws IOException, InterruptedException, DifferenceFoundException {

    // Prepare files and directories
    File testDir = TestTools.createTempDir("jacktest", null);

    File refDex = TestTools.createReferenceCompilerFiles(testDir,
        compilerArgs,
        proguardFlags,
        bootclasspath,
        classpath,
        false /* withDebugInfo */,
        useEcjAsRefCompiler,
        jarjarRules).dexFile;

    // Compare Jack Dex file to reference
    DexAnnotationsComparator comparator = new DexAnnotationsComparator();
    comparator.compare(refDex, jackDex);
  }
}
