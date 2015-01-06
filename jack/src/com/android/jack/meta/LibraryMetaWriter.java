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

import com.android.jack.ir.ast.JSession;
import com.android.jack.library.FileType;
import com.android.jack.library.InputLibrary;
import com.android.jack.library.OutputJackLibrary;
import com.android.sched.item.Description;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.util.location.FileLocation;
import com.android.sched.util.location.Location;
import com.android.sched.util.location.ZipLocation;
import com.android.sched.util.stream.ByteStreamSucker;
import com.android.sched.vfs.InputVFile;
import com.android.sched.vfs.OutputVFile;
import com.android.sched.vfs.VPath;

import java.io.IOException;
import java.io.InputStream;
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
      if (importedLibrary.containsFileType(FileType.JPP)) {
        Iterator<InputVFile> jppIter = importedLibrary.iterator(FileType.JPP);
        while (jppIter.hasNext()) {
          InputVFile jppFile = jppIter.next();
          String name = getNameFromInputVFile(importedLibrary, jppFile);
          Meta meta = new Meta(new VPath(name, '/'), jppFile);
          addMetaToOutputJackLib(meta, ojl);
        }
      }
    }
  }

  //TODO(jack-team): remove this hack
  private String getNameFromInputVFile(@Nonnull InputLibrary jackLibrary,
      @Nonnull InputVFile jppFile) {
    Location loc = jppFile.getLocation();
    String name;
    if (loc instanceof ZipLocation) {
      name = ((ZipLocation) jppFile.getLocation()).getEntryName();
      if (jackLibrary.getMajorVersion() != 0) {
        name = name.substring(
            FileType.JPP.buildDirVPath(VPath.ROOT).split().iterator().next().length() + 1);
      } else {
        name = name.substring("JACK-INF/".length());
      }
    } else {
      name = ((FileLocation) jppFile.getLocation()).getPath();
      if (jackLibrary.getMajorVersion() != 0) {
        String prefix = FileType.JPP.buildDirVPath(VPath.ROOT).split().iterator().next() + '/';
        name = name.substring(name.lastIndexOf(prefix) + prefix.length());
      } else {
        name = name.substring("JACK-INF/".length());
      }
    }
    return name;
  }

  private void addMetaToOutputJackLib(Meta meta, OutputJackLibrary ojl) throws IOException {
    InputVFile inputFile = meta.getVFile();
    VPath path = meta.getPath();
    OutputVFile outputFile = ojl.createFile(FileType.JPP, path);
    InputStream is = null;
    try {
      is = inputFile.openRead();
      ByteStreamSucker sucker = new ByteStreamSucker(is, outputFile.openWrite(), true /* close */);
      sucker.suck();
    } finally {
      if (is != null) {
        is.close();
      }
    }
  }
}
