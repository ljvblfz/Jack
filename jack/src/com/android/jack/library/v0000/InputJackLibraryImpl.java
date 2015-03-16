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

package com.android.jack.library.v0000;

import com.google.common.collect.Iterators;

import com.android.jack.library.FileType;
import com.android.jack.library.FileTypeDoesNotExistException;
import com.android.jack.library.InputJackLibrary;
import com.android.jack.library.JackLibrary;
import com.android.jack.library.LibraryFormatException;
import com.android.jack.library.LibraryIOException;
import com.android.jack.library.LibraryVersionException;
import com.android.sched.util.file.CannotDeleteFileException;
import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotDirectoryException;
import com.android.sched.util.file.NotFileOrDirectoryException;
import com.android.sched.vfs.GenericInputVFS;
import com.android.sched.vfs.InputVDir;
import com.android.sched.vfs.InputVElement;
import com.android.sched.vfs.InputVFS;
import com.android.sched.vfs.InputVFile;
import com.android.sched.vfs.VFS;
import com.android.sched.vfs.VPath;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Jack library used as input.
 */
public class InputJackLibraryImpl extends InputJackLibrary {

  @Nonnull
  private final List<InputVFile> resources = new ArrayList<InputVFile>();

  @Nonnull
  protected final InputVFS inputVFS;

  public InputJackLibraryImpl(@Nonnull VFS vfs,
      @Nonnull Properties libraryProperties) throws LibraryVersionException,
      LibraryFormatException {
    super(libraryProperties, vfs.getLocation());
    inputVFS = new GenericInputVFS(vfs);

    check();
    fillFileTypes();

    // V0 libraries can have resources added by Jill but without the boolean resource property set
    // to true, thus we add the resource file type if resources exist (files others than
    // jack.properties and jayce files)
    fillResources(inputVFS.getRootInputVDir(), resources);
    if (!resources.isEmpty()) {
      fileTypes.add(FileType.RSC);
    }
  }

  @Override
  public void close() throws LibraryIOException {
    try {
      inputVFS.close();
    } catch (IOException e) {
      throw new LibraryIOException(getLocation(), e);
    }
  }

  @Override
  @Nonnull
  public InputVFile getFile(@Nonnull FileType fileType, @Nonnull VPath typePath)
      throws FileTypeDoesNotExistException {
    try {
      VPath clonedPath = typePath.clone();
      clonedPath.addSuffix(fileType.getFileExtension());
      return inputVFS.getRootInputVDir().getInputVFile(clonedPath);
    } catch (NotFileOrDirectoryException e) {
      throw new FileTypeDoesNotExistException(getLocation(), typePath, fileType);
    } catch (NoSuchFileException e) {
      throw new FileTypeDoesNotExistException(getLocation(), typePath, fileType);
    }
  }

  @Override
  @Nonnull
  public InputVDir getDir(@Nonnull FileType fileType, @Nonnull VPath typePath)
      throws FileTypeDoesNotExistException {
    try {
      return inputVFS.getRootInputVDir().getInputVDir(typePath);
    } catch (NotDirectoryException e) {
      throw new FileTypeDoesNotExistException(getLocation(), typePath, fileType);
    } catch (NoSuchFileException e) {
      throw new FileTypeDoesNotExistException(getLocation(), typePath, fileType);
    }
  }

  @Override
  @Nonnull
  public Iterator<InputVFile> iterator(@Nonnull FileType fileType) {
    if (!containsFileType(fileType)) {
      return Iterators.emptyIterator();
    }

    // Reuse resources found when we have detected that they existed.
    if (fileType == FileType.RSC) {
      return resources.iterator();
    }

    List<InputVFile> inputVFiles = new ArrayList<InputVFile>();
    fillFiles(inputVFS.getRootInputVDir(), fileType, inputVFiles);
    return inputVFiles.iterator();
  }

  @Override
  @Nonnegative
  public int getMajorVersion() {
    return Version.MAJOR;
  }

  @Override
  @Nonnegative
  public int getSupportedMinorMin() {
    return Version.MINOR_MIN;
  }

  @Override
  @Nonnegative
  public int getSupportedMinor() {
    return Version.MINOR;
  }

  @Override
  @Nonnull
  public void delete(@Nonnull FileType fileType, @Nonnull VPath typePath)
      throws CannotDeleteFileException, FileTypeDoesNotExistException {
    try {
      inputVFS.getRootInputVDir().getInputVFile(fileType.buildFileVPath(typePath)).delete();
    } catch (NotFileOrDirectoryException e) {
      throw new FileTypeDoesNotExistException(getLocation(), typePath, fileType);
    } catch (NoSuchFileException e) {
      throw new FileTypeDoesNotExistException(getLocation(), typePath, fileType);
    }
  }

  @Override
  @Nonnull
  public String getPath() {
    return inputVFS.getPath();
  }

  @Override
  @CheckForNull
  public String getDigest() {
    return null;
  }

  private void fillResources(@Nonnull InputVDir vDir, @Nonnull List<InputVFile> files) {
    for (InputVElement subFile : vDir.list()) {
      if (subFile.isVDir()) {
        fillResources((InputVDir) subFile, files);
      } else {
        InputVFile vFile = (InputVFile) subFile;
        if (!FileType.JAYCE.isOfType(vFile)
            && !FileType.JPP.isOfType(vFile)
            && !vFile.getName().equals(JackLibrary.LIBRARY_PROPERTIES_VPATH.getLastElement())) {
          files.add(vFile);
        }
      }
    }
  }
}
