/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.jack.analysis.dependency.library;

import com.google.common.io.LineReader;

import com.android.jack.analysis.dependency.Dependency;
import com.android.jack.library.InputLibrary;
import com.android.sched.vfs.VPath;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Class representing dependencies between source file to compile and used libraries.
 * Dependencies are categorized into 2 categories:
 * - Dex digest of libraries on classpath
 * - Dex digest of imported libraries
 */
public class LibraryDependencies extends Dependency {

  @Nonnull
  public static final VPath vpath = new VPath("libraries", '/');

  @Nonnull
  private List<String> importedLibrariesDexDigest = new ArrayList<String>();

  @Nonnull
  private List<String> librariesOnClasspathDexDigest = new ArrayList<String>();

  public void addImportedLibraries(@Nonnull List<InputLibrary> importedLibraries) {
    for (InputLibrary inputLibrary : importedLibraries) {
      importedLibrariesDexDigest.add(inputLibrary.getDigest());
    }
  }

  public void addLibrariesOnClasspath(@Nonnull List<InputLibrary> librariesOnClasspath) {
    for (InputLibrary inputLibrary : librariesOnClasspath) {
      librariesOnClasspathDexDigest.add(inputLibrary.getDigest());
    }
  }

  public void write(@Nonnull PrintStream ps) {
    writeList(ps, librariesOnClasspathDexDigest);
    ps.println();
    writeList(ps, importedLibrariesDexDigest);
    ps.println();
  }

  @Override
  @Nonnull
  public void read(@Nonnull Readable readable) throws IOException {
    LineReader lr = new LineReader(readable);
    librariesOnClasspathDexDigest = readList(lr);
    importedLibrariesDexDigest = readList(lr);
  }

  public boolean hasSameLibraryOnClasspath(@Nonnull LibraryDependencies libraryDependencies) {
    return compare(librariesOnClasspathDexDigest,
        libraryDependencies.librariesOnClasspathDexDigest);
  }

  public boolean hasSameImportedLibrary(@Nonnull LibraryDependencies libraryDependencies) {
    return compare(importedLibrariesDexDigest, libraryDependencies.importedLibrariesDexDigest);
  }

  @Nonnull
  public List<String> getDigestOfImportedLibraries() {
    return importedLibrariesDexDigest;
  }

  @Nonnull
  public List<String> getDigestOfLibrariesOnClasspath() {
    return librariesOnClasspathDexDigest;
  }

  private static boolean compare(@Nonnull List<String> digests1, @Nonnull List<String> digests2) {
    if (digests1.size() != digests2.size()) {
      return false;
    }

    Iterator<String> digestToCompareIt = digests1.iterator();

    for (String digest : digests2) {
      String digestToCompare = digestToCompareIt.next();
      if (digest == null || digestToCompare == null || digest.equals("")
          || digestToCompare.equals("") || !digest.equals(digestToCompare)) {
        return false;
      }
    }
    return true;
  }
}
