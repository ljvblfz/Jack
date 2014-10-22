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

package com.android.jack.library;

import com.android.jack.Jack;
import com.android.jack.backend.jayce.JayceFileImporter;
import com.android.jack.jayce.JayceWriter;
import com.android.jack.jayce.v0002.Version;
import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.vfs.OutputVDir;
import com.android.sched.vfs.OutputVFile;
import com.android.sched.vfs.SequentialOutputVDir;
import com.android.sched.vfs.VPath;

import java.io.IOException;
import java.util.Properties;

import javax.annotation.Nonnull;

/**
 * Jack library generated by Jack.
 */
public class OutputJackLibrary implements OutputLibrary, JackLibrary {

  @Nonnull
  private final OutputVDir outputVDir;

  @Nonnull
  private final OutputLibraryLocation location = new OutputLibraryLocation() {
    @Override
    @Nonnull
    public String getDescription() {
      return outputVDir.getLocation().getDescription();
    }

    @Override
    @Nonnull
    public OutputLibrary getOutputLibrary() {
      return OutputJackLibrary.this;
    }

    @Override
    public final boolean equals(Object obj) {
      return obj instanceof OutputLibraryLocation
        && ((OutputLibraryLocation) obj).getOutputLibrary().equals(getOutputLibrary());
    }

    @Override
    public final int hashCode() {
      return OutputJackLibrary.this.hashCode();
    }
  };

  public OutputJackLibrary(@Nonnull OutputVDir outputVDir) {
    this.outputVDir = outputVDir;
  }

  @Override
  @Nonnull
  public OutputVFile getJayceOutputVFile(@Nonnull VPath typePath) throws CannotCreateFileException {
    return outputVDir.createOutputVFile(
        new VPath(typePath.getPathAsString('/') + JayceFileImporter.JAYCE_FILE_EXTENSION, '/'));
  }

  @Override
  public boolean needsSequentialWriting() {
    return outputVDir instanceof SequentialOutputVDir;
  }

  @Override
  @Nonnull
  public OutputVFile getBinaryOutputVFile(@Nonnull VPath typePath, @Nonnull BinaryKind binaryKind)
      throws CannotCreateFileException {
    return outputVDir.createOutputVFile(
        new VPath(typePath.getPathAsString('/') + binaryKind.getFileExtension(), '/'));
  }

  @Override
  @Nonnull
  public OutputLibraryLocation getLocation() {
    return location;
  }

  @Override
  public void close() throws LibraryWritingException {
    Properties jackLibraryProperties = new Properties();
    jackLibraryProperties.put(KEY_LIB_EMITTER, "jack");
    jackLibraryProperties.put(KEY_LIB_EMITTER_VERSION, Jack.getVersionString());
    jackLibraryProperties.put(KEY_LIB_MAJOR_VERSION, String.valueOf(JackLibraryVersion.MAJOR));
    jackLibraryProperties.put(KEY_LIB_MINOR_VERSION, String.valueOf(JackLibraryVersion.MINOR));
    jackLibraryProperties.put(KEY_JAYCE, String.valueOf(true));
    jackLibraryProperties.put(KEY_JAYCE_MAJOR_VERSION,
        String.valueOf(JayceWriter.DEFAULT_MAJOR_VERSION));
    jackLibraryProperties.put(KEY_JAYCE_MINOR_VERSION, String.valueOf(Version.CURRENT_MINOR));

    try {
      OutputVFile libraryPropertiesOut = outputVDir.createOutputVFile(LIBRARY_PROPERTIES_VPATH);
      jackLibraryProperties.store(libraryPropertiesOut.openWrite(), "Library properties");
    } catch (CannotCreateFileException e) {
      throw new LibraryWritingException(e);
    } catch (IOException e) {
      throw new LibraryWritingException(e);
    }
  }
}
