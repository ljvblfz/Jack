/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.jack.backend.dex;

import com.android.jack.JackIOException;
import com.android.jack.backend.dex.DexWritingTool.MatchableInputVFile;
import com.android.jack.ir.ast.JSession;
import com.android.jack.library.FileType;
import com.android.jack.library.OutputJackLibrary;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Produce;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.util.file.CannotCloseException;
import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.file.CannotReadException;
import com.android.sched.util.file.CannotWriteException;
import com.android.sched.util.file.WrongPermissionException;
import com.android.sched.vfs.InputVFile;
import com.android.sched.vfs.OutputVFile;
import com.android.sched.vfs.VPath;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * Write an orphan dex from an imported library to an output library.
 */
@Description("Write an orphan dex from an imported library to an output library")
@Name("OrphanDexFileWriter")
@Produce(DexInLibraryProduct.class)
public class OrphanDexFileWriter extends DexWriter implements RunnableSchedulable<JSession> {

  @Override
  public void run(@Nonnull JSession session) {
    Set<MatchableInputVFile> prebuiltOrphanDexFiles = new HashSet<>();
    OutputJackLibrary outputLibrary = session.getJackOutputLibrary();

    DexWritingTool.addOrphanDexFiles(outputLibrary, prebuiltOrphanDexFiles);

    for (MatchableInputVFile matchableInput : prebuiltOrphanDexFiles) {
      InputVFile in = matchableInput.getInputVFile();
      String dexFilePath = in.getPathFromRoot().getPathAsString('/');
      int indexOfDexExtension = dexFilePath.indexOf(DexFileWriter.DEX_FILE_EXTENSION);
      assert indexOfDexExtension != -1;
      String type = dexFilePath.substring(0, dexFilePath.indexOf(DexFileWriter.DEX_FILE_EXTENSION));
      OutputVFile vFile;
      try {
        vFile = outputLibrary.createFile(FileType.PREBUILT, new VPath(type, '/'));
      } catch (CannotCreateFileException e) {
        throw new JackIOException("Could not create Dex file in output "
            + outputLibrary.getLocation().getDescription() + " for type " + type, e);
      }
      try {
        vFile.copy(in);
      } catch (CannotCloseException | CannotReadException | CannotWriteException
          | WrongPermissionException e) {
        throw new JackIOException("Could not copy Dex file from "
            + in.getLocation().getDescription() + " to " + vFile.getLocation().getDescription(), e);
      }
    }
  }
}
