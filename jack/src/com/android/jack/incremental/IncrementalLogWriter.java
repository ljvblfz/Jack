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

package com.android.jack.incremental;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;

import com.android.jack.library.FileType;
import com.android.jack.library.FileTypeDoesNotExistException;
import com.android.jack.library.LibraryIOException;
import com.android.jack.library.OutputJackLibrary;
import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.file.NotFileOrDirectoryException;
import com.android.sched.vfs.VPath;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Collection;

import javax.annotation.Nonnull;

/**
 * Write logs of incremental runs into a section logs of the library representing the incremental
 * state.
 */
class IncrementalLogWriter {

  @Nonnull
  static final VPath vpath = new VPath("logs", '/');

  @Nonnull
  private final PrintStream ps;

  @Nonnull
  private final OutputJackLibrary library;

  IncrementalLogWriter(@Nonnull OutputJackLibrary library, @Nonnull File incrementalFolder)
      throws LibraryIOException {
    this.library = library;
    try {
      library.getFile(FileType.LOG, vpath);
    } catch (FileTypeDoesNotExistException e) {
      try {
        library.createFile(FileType.LOG, vpath);
      } catch (CannotCreateFileException e1) {
        throw new LibraryIOException(library.getLocation(), e1);
      } catch (NotFileOrDirectoryException e1) {
        throw new LibraryIOException(library.getLocation(), e1);
      }
    }

    File logFile = new File(incrementalFolder,
        FileType.LOG.buildFileVPath(vpath).getPathAsString(File.separatorChar));
    try {
      ps = new PrintStream(new FileOutputStream(logFile, true));
      writeString("***");
    } catch (FileNotFoundException e) {
      // File already creates by using VFS
      throw new AssertionError();
    }
  }

  void writeFiles(@Nonnull String prefixStr, @Nonnull Collection<File> files) {
    writeStrings(prefixStr, Collections2.transform(files, new Function<File, String>() {
      @Override
      public String apply(File file) {
        return file.getPath();
      }
    }));
  }

  void writeStrings(@Nonnull String prefixStr, @Nonnull Collection<String> strings) {
    ps.print(prefixStr);
    ps.print(": ");
    ps.print(Joiner.on(",").useForNull("").join(strings));
    ps.println();
  }

  void close() {
    ps.close();
  }

  void writeString(@Nonnull String str) {
    ps.print(str);
    ps.println();
  }
}
