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

package com.android.jack.frontend;

import com.android.jack.Jack;
import com.android.jack.Options;
import com.android.jack.analysis.dependency.file.FileDependencies;
import com.android.jack.analysis.dependency.library.LibraryDependencies;
import com.android.jack.analysis.dependency.type.TypeDependencies;
import com.android.jack.incremental.CommonFilter;
import com.android.jack.incremental.InputFilter;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.Resource;
import com.android.jack.library.InputLibrary;
import com.android.jack.meta.Meta;
import com.android.sched.util.codec.ImplementationName;
import com.android.sched.util.config.Config;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.file.Directory;
import com.android.sched.util.file.FileOrDirectory;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Nonnull;

/**
 * {@link InputFilter} that returns ordered inputs.
 */
@ImplementationName(iface = InputFilter.class, name = "ordered-filter")
public class OrderedInputFilter extends CommonFilter implements InputFilter {

  @Nonnull
  private final Set<String> fileNamesToCompile;

  @Nonnull
  private final List<? extends InputLibrary> importedLibrariesFromCommandLine;

  @Nonnull
  private final List<? extends InputLibrary> librariesOnClasspathFromCommandLine;

  public OrderedInputFilter(@Nonnull Options options) {
    this.fileNamesToCompile = getJavaFileNamesSpecifiedOnCommandLine(options);
    JSession session = Jack.getSession();
    session.setFileDependencies(new FileDependencies());
    session.setTypeDependencies(new TypeDependencies());
    importedLibrariesFromCommandLine =
        getImportedLibraries(ThreadConfig.get(Options.IMPORTED_LIBRARIES));
    librariesOnClasspathFromCommandLine = getClasspathLibraries(
        ThreadConfig.get(Options.CLASSPATH),
        ThreadConfig.get(Jack.STRICT_CLASSPATH).booleanValue());

    LibraryDependencies libraryDependencies = session.getLibraryDependencies();
    libraryDependencies.addImportedLibraries(importedLibrariesFromCommandLine);
    libraryDependencies.addLibrariesOnClasspath(librariesOnClasspathFromCommandLine);
  }

  @Override
  @Nonnull
  public Set<String> getFileNamesToCompile() {
    return fileNamesToCompile;
  }

  @Override
  @Nonnull
  public List<? extends InputLibrary> getClasspath() {
    return librariesOnClasspathFromCommandLine;
  }

  @Override
  @Nonnull
  public List<? extends InputLibrary> getImportedLibraries() {
    return importedLibrariesFromCommandLine;
  }

  @Override
  @Nonnull
  protected Set<String> getJavaFileNamesSpecifiedOnCommandLine(@Nonnull Options options) {
    Config config = options.getConfig();
    final String extension = ".java";

    Set<String> javaFileNames = new TreeSet<String>();
    for (FileOrDirectory file : config.get(Options.SOURCES)) {
      if (file instanceof Directory) {
        fillFiles(((Directory) file).getFile(), extension, javaFileNames);
      } else if (file.getPath().endsWith(extension)) {
        // File already checked by codec
        javaFileNames.add(file.getPath());
      }
    }

    return (javaFileNames);
  }

  @Override
  @Nonnull
  public List<? extends Resource> getImportedResources() {
    return importStandaloneResources();
  }

  @Override
  @Nonnull
  public List<? extends Meta> getImportedMetas() {
    return importStandaloneMetas();
  }
}
