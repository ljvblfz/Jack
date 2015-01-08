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

package com.android.jack.incremental;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;

import com.android.jack.Jack;
import com.android.jack.Options;
import com.android.jack.library.JackLibraryFactory;
import com.android.jack.library.OutputJackLibrary;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.vfs.Container;
import com.android.sched.vfs.InputOutputVFS;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * Common part of {@link InputFilter}
 */
public abstract class CommonFilter {

  @Nonnull
  protected Set<String> getJavaFileNamesSpecifiedOnCommandLine(@Nonnull Options options) {
    final Set<File> folders = new HashSet<File>();
    final String extension = ".java";

    Set<String> javaFileNames =
        Sets.newHashSet(Collections2.filter(options.getEcjArguments(), new Predicate<String>() {
          @Override
          public boolean apply(String arg) {
            File argFile = new File(arg);
            if (argFile.isDirectory()) {
              folders.add(argFile);
            }
            return arg.endsWith(extension);
          }
        }));

    for (File folder : folders) {
      fillFiles(folder, extension, javaFileNames);
    }

    return (javaFileNames);
  }

  private void fillFiles(@Nonnull File folder, @Nonnull String fileExt,
      @Nonnull Set<String> fileNames) {
    for (File subFile : folder.listFiles()) {
      if (subFile.isDirectory()) {
        fillFiles(subFile, fileExt, fileNames);
      } else {
        String path = subFile.getPath();
        if (subFile.getName().endsWith(fileExt)) {
          fileNames.add(path);
        }
      }
    }
  }

  @Nonnull
  protected OutputJackLibrary getOutputJackLibraryFromVfs() {
    InputOutputVFS outputDir;
    Container containerType = ThreadConfig.get(Options.LIBRARY_OUTPUT_CONTAINER_TYPE);

    if (containerType == Container.DIR) {
      outputDir = ThreadConfig.get(Options.LIBRARY_OUTPUT_DIR);
    } else {
      outputDir = ThreadConfig.get(Options.LIBRARY_OUTPUT_ZIP);
    }

    return (JackLibraryFactory.getOutputLibrary(outputDir, Jack.getEmitterId(),
        Jack.getVersionString()));
  }
}
