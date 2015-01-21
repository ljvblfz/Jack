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

package com.android.jack.library.v0001;

import com.google.common.collect.Iterators;

import com.android.jack.library.FileType;
import com.android.jack.library.FileTypeDoesNotExistException;
import com.android.jack.library.InputJackLibrary;
import com.android.jack.library.JackLibraryFactory;
import com.android.jack.library.LibraryIOException;
import com.android.jack.library.OutputJackLibrary;
import com.android.jack.library.OutputLibrary;
import com.android.jack.library.OutputLibraryLocation;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.file.CannotDeleteFileException;
import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotDirectoryException;
import com.android.sched.util.file.NotFileOrDirectoryException;
import com.android.sched.util.location.Location;
import com.android.sched.vfs.InputOutputVFS;
import com.android.sched.vfs.InputVFS;
import com.android.sched.vfs.InputVFile;
import com.android.sched.vfs.MessageDigestOutputVFS;
import com.android.sched.vfs.OutputVFS;
import com.android.sched.vfs.OutputVFile;
import com.android.sched.vfs.PrefixedInputVFS;
import com.android.sched.vfs.PrefixedOutputVFS;
import com.android.sched.vfs.VPath;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * Jack library generated by Jack.
 */
public class OutputJackLibraryImpl extends OutputJackLibrary {

  private boolean closed = false;

  private final boolean generateJacklibDigest =

      ThreadConfig.get(JackLibraryFactory.GENERATE_JACKLIB_DIGEST).booleanValue();

  @Nonnull
  private final Map<FileType, VFSPair> sectionVFS =
      new EnumMap<FileType, VFSPair>(FileType.class);

  @Nonnull
  private final OutputLibraryLocation location = new OutputLibraryLocation() {
    @Override
    @Nonnull
    public String getDescription() {
      return getVFSLocation().getDescription();
    }

    @Override
    @Nonnull
    public OutputLibrary getOutputLibrary() {
      return OutputJackLibraryImpl.this;
    }

    @Override
    protected Location getVFSLocation() {
      return baseVFS.getLocation();
    }
  };

  public OutputJackLibraryImpl(@Nonnull InputOutputVFS baseVFS, @Nonnull String emitterId,
      @Nonnull String emitterVersion) {
    super(baseVFS, emitterId, emitterVersion);
  }

  public OutputJackLibraryImpl(@Nonnull InputJackLibrary inputJackLibrary,
      @Nonnull String emitterId, @Nonnull String emitterVersion) {
    super(inputJackLibrary, emitterId, emitterVersion);
  }

  @Override
  @Nonnull
  public OutputVFile createFile(@Nonnull FileType fileType, @Nonnull final VPath typePath)
      throws CannotCreateFileException, NotFileOrDirectoryException {
    assert !isClosed();
    putProperty(fileType.buildPropertyName(null /*suffix*/), String.valueOf(true));
    addFileType(fileType);
    return getSectionVFS(fileType).getOutputVFS().getRootOutputVDir()
        .createOutputVFile(buildFileVPath(fileType, typePath));
  }

  @Override
  public boolean needsSequentialWriting() {
    return baseVFS.needsSequentialWriting();
  }

  @Override
  @Nonnull
  public OutputLibraryLocation getLocation() {
    return location;
  }

  @SuppressWarnings("resource")
  @Nonnull
  private synchronized VFSPair getSectionVFS(@Nonnull FileType fileType)
      throws NotDirectoryException, CannotCreateFileException {
    VFSPair currentSectionVFS;
    if (sectionVFS.containsKey(fileType)) {
      currentSectionVFS = sectionVFS.get(fileType);
    } else {
      VPath prefixPath = new VPath(fileType.getPrefix(), '/');
      OutputVFS outputVFS = new PrefixedOutputVFS(baseVFS, prefixPath);
      InputVFS inputVFS;
      try {
        inputVFS = new PrefixedInputVFS(baseVFS, prefixPath);
      } catch (NoSuchFileException e) {
        // prefix dir should have been created when instantiating the PrefixedOutputVFS.
        throw new AssertionError(e);
      }
      if (generateJacklibDigest && fileType == FileType.DEX) {
        outputVFS = new MessageDigestOutputVFS(outputVFS,
            ThreadConfig.get(JackLibraryFactory.MESSAGE_DIGEST_ALGO));
      }
      currentSectionVFS = new VFSPair(inputVFS, outputVFS);
      sectionVFS.put(fileType, currentSectionVFS);
    }
    return currentSectionVFS;
  }

  @Override
  public synchronized void close() throws LibraryIOException {
    if (!closed) {
      OutputStream os = null;
      try {
        OutputVFile libraryPropertiesOut =
            baseVFS.getRootInputOutputVDir().createOutputVFile(LIBRARY_PROPERTIES_VPATH);
        os = libraryPropertiesOut.openWrite();
        libraryProperties.store(os, "Library properties");
      } catch (CannotCreateFileException e) {
        throw new LibraryIOException(getLocation(), e);
      } catch (IOException e) {
        throw new LibraryIOException(getLocation(), e);
      } finally {
        if (os != null) {
          try {
            os.close();
          } catch (IOException e) {
            throw new LibraryIOException(getLocation(), e);
          }
        }
      }

      try {
        for (VFSPair currentSectionVFS : sectionVFS.values()) {
          currentSectionVFS.getInputVFS().close();
          currentSectionVFS.getOutputVFS().close();
        }
        baseVFS.close();
      } catch (IOException e) {
        throw new LibraryIOException(getLocation(), e);
      }
      closed = true;
    }
  }

  @Override
  public int getMinorVersion() {
    return Version.MINOR;
  }

  @Override
  public int getMajorVersion() {
    return Version.MAJOR;
  }

  @Override
  @Nonnull
  public Iterator<InputVFile> iterator(@Nonnull FileType fileType) {
    if (!containsFileType(fileType)) {
      return Iterators.emptyIterator();
    }

    List<InputVFile> inputVFiles = new ArrayList<InputVFile>();
    try {
      VFSPair currentSectionVFS = getSectionVFS(fileType);
      fillFiles(currentSectionVFS.getInputVFS().getRootInputVDir(), fileType, inputVFiles);
    } catch (NotDirectoryException e) {
      // we already checked that the library contained the file type
      throw new AssertionError(e);
    } catch (CannotCreateFileException e) {
      // we already checked that the library contained the file type
      throw new AssertionError(e);
    }
    return inputVFiles.listIterator();
  }

  @Override
  @Nonnull
  public InputVFile getFile(@Nonnull FileType fileType, @Nonnull VPath typePath)
      throws FileTypeDoesNotExistException {
    try {
      VFSPair currentSectionVFS = getSectionVFS(fileType);
      return currentSectionVFS.getInputVFS().getRootInputVDir().getInputVFile(
          buildFileVPath(fileType, typePath));
    } catch (NotFileOrDirectoryException e) {
      throw new FileTypeDoesNotExistException(getLocation(), typePath, fileType);
    } catch (NoSuchFileException e) {
      throw new FileTypeDoesNotExistException(getLocation(), typePath, fileType);
    } catch (CannotCreateFileException e) {
      throw new FileTypeDoesNotExistException(getLocation(), typePath, fileType);
    }
  }

  @Override
  @Nonnull
  public void delete(@Nonnull FileType fileType, @Nonnull VPath typePath)
      throws CannotDeleteFileException, FileTypeDoesNotExistException {
    assert !isClosed();
    try {
      VFSPair currentSectionVFS = getSectionVFS(fileType);
      currentSectionVFS.getInputVFS().getRootInputVDir().delete(buildFileVPath(fileType, typePath));
    } catch (NotFileOrDirectoryException e) {
      throw new FileTypeDoesNotExistException(getLocation(), typePath, fileType);
    } catch (CannotCreateFileException e) {
      throw new FileTypeDoesNotExistException(getLocation(), typePath, fileType);
    } catch (NoSuchFileException e) {
      throw new FileTypeDoesNotExistException(getLocation(), typePath, fileType);
    }
  }

  @Override
  @Nonnull
  public String getPath() {
    return baseVFS.getPath();
  }

  @Nonnull
  public VPath buildFileVPath(@Nonnull FileType fileType, @Nonnull VPath vpath) {
    VPath clonedPath = vpath.clone();
    clonedPath.addSuffix(fileType.getFileExtension());
    return clonedPath;
  }

  private synchronized boolean isClosed() {
    return closed;
  }

  private static class VFSPair {

    @Nonnull
    private final InputVFS inputVFS;
    @Nonnull
    private final OutputVFS outputVFS;

    public VFSPair(@Nonnull InputVFS inputVFS, @Nonnull OutputVFS outputVFS) {
      this.inputVFS = inputVFS;
      this.outputVFS = outputVFS;
    }

    @Nonnull
    public InputVFS getInputVFS() {
      return inputVFS;
    }

    @Nonnull
    public OutputVFS getOutputVFS() {
      return outputVFS;
    }
  }
}
