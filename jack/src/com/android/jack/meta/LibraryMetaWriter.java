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

package com.android.jack.meta;

import com.android.jack.analysis.dependency.Dependency;
import com.android.jack.ir.ast.JSession;
import com.android.jack.library.FileType;
import com.android.jack.library.InputLibrary;
import com.android.jack.library.MetaInInputLibraryLocation;
import com.android.jack.library.OutputJackLibrary;
import com.android.sched.item.Description;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.util.file.CannotCloseInputException;
import com.android.sched.util.file.CannotCloseOutputException;
import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.file.CannotReadException;
import com.android.sched.util.file.CannotWriteException;
import com.android.sched.util.file.WrongPermissionException;
import com.android.sched.util.location.ZipLocation;
import com.android.sched.vfs.InputVFile;
import com.android.sched.vfs.OutputVFile;
import com.android.sched.vfs.VPath;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Writes metas in libraries.
 */
@Description("Writes metas in libraries")
public class LibraryMetaWriter implements RunnableSchedulable<JSession> {

  @Override
  public void run(@Nonnull JSession session) throws Exception {
    OutputJackLibrary ojl = session.getJackOutputLibrary();

    // add metas from --import-meta
    List<Meta> metas = session.getMetas();
    for (Meta meta : metas) {
      addMetaToOutputJackLib(meta, ojl);
    }

    // add metas from --import-jack libs
    for (InputLibrary importedLibrary : session.getImportedLibraries()) {
      if (importedLibrary.containsFileType(FileType.META)) {
        Iterator<InputVFile> metaIter = importedLibrary.iterator(FileType.META);
        while (metaIter.hasNext()) {
          InputVFile metaFile = metaIter.next();
          if (!metaFile.getName().endsWith(Dependency.DEPENDENCY_FILE_EXTENSION)) {
            VPath path = getNameFromInputVFile(importedLibrary, metaFile);
            Meta meta = new Meta(path, metaFile,
                new MetaInInputLibraryLocation(importedLibrary, path));
            addMetaToOutputJackLib(meta, ojl);
          }
        }
      }
    }
  }

  //TODO(jack-team): remove this hack
  @Nonnull
  private VPath getNameFromInputVFile(@Nonnull InputLibrary jackLibrary,
      @Nonnull InputVFile metaFile) {
    VPath path;
    if (jackLibrary.getMajorVersion() == 0) {
      String name = ((ZipLocation) metaFile.getLocation()).getEntryName();
      name = name.substring("JACK-INF/".length());
      path = new VPath(name, '/');
    } else {
      path = metaFile.getPathFromRoot();
    }
    return path;
  }

  private void addMetaToOutputJackLib(@Nonnull Meta meta, @Nonnull OutputJackLibrary ojl)
      throws CannotCreateFileException, WrongPermissionException, CannotCloseInputException,
      CannotCloseOutputException, CannotReadException, CannotWriteException {
    InputVFile inputFile = meta.getVFile();
    VPath path = meta.getPath();
    OutputVFile outputFile = ojl.createFile(FileType.META, path);
    outputFile.copy(inputFile);
  }
}
