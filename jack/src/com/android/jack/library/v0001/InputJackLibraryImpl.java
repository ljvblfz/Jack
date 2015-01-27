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
import com.android.jack.library.InputLibrary;
import com.android.jack.library.InputLibraryLocation;
import com.android.jack.library.JackLibraryFactory;
import com.android.jack.library.LibraryFormatException;
import com.android.jack.library.LibraryIOException;
import com.android.jack.library.LibraryVersionException;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.file.CannotDeleteFileException;
import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotDirectoryException;
import com.android.sched.util.file.NotFileOrDirectoryException;
import com.android.sched.util.location.Location;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.vfs.GenericInputVFS;
import com.android.sched.vfs.InputVDir;
import com.android.sched.vfs.InputVFS;
import com.android.sched.vfs.InputVFile;
import com.android.sched.vfs.MessageDigestFS;
import com.android.sched.vfs.PrefixedFS;
import com.android.sched.vfs.VFS;
import com.android.sched.vfs.VPath;
import com.android.sched.vfs.WrongVFSFormatException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Jack library used as input.
 */
public class InputJackLibraryImpl extends InputJackLibrary {
  @Nonnull
  private static Logger logger = LoggerFactory.getLogger();

  @Nonnull
  private final Map<FileType, InputVFS> sectionVFS =
      new EnumMap<FileType, InputVFS>(FileType.class);

  @Nonnegative
  private final int minorVersion;

  @Nonnull
  private final VFS vfs;

  @Nonnull
  private final InputLibraryLocation location = new InputLibraryLocation() {
    @Override
    @Nonnull
    public String getDescription() {
      return getVFSLocation().getDescription();
    }

    @Override
    @Nonnull
    public InputLibrary getInputLibrary() {
      return InputJackLibraryImpl.this;
    }

    @Override
    protected Location getVFSLocation() {
      return vfs.getLocation();
    }
  };

  public InputJackLibraryImpl(@Nonnull VFS vfs,
      @Nonnull Properties libraryProperties) throws LibraryVersionException,
      LibraryFormatException {
    super(libraryProperties);
    this.vfs = vfs;

    try {
      minorVersion = Integer.parseInt(getProperty(KEY_LIB_MINOR_VERSION));
    } catch (NumberFormatException e) {
      logger.log(Level.SEVERE, "Fails to parse the property " + KEY_LIB_MINOR_VERSION
          + " from " + getLocation().getDescription(), e);
      throw new LibraryFormatException(getLocation());
    }

    check();
    fillFileTypes();
  }

  @Override
  @Nonnull
  public InputLibraryLocation getLocation() {
    return location;
  }

  @Override
  @Nonnull
  public InputVFile getFile(@Nonnull FileType fileType, @Nonnull VPath typePath)
      throws FileTypeDoesNotExistException {
    try {
      InputVFS currentSectionVFS = getSectionVFS(fileType);
      return currentSectionVFS.getRootInputVDir().getInputVFile(
          buildFileVPath(fileType, typePath));
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
      InputVFS currentSectionVFS = getSectionVFS(fileType);
      return currentSectionVFS.getRootInputVDir().getInputVDir(typePath);
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

    List<InputVFile> inputVFiles = new ArrayList<InputVFile>();
    fillFiles(getSectionVFS(fileType).getRootInputVDir(), fileType, inputVFiles);
    return inputVFiles.listIterator();
  }

  @Nonnull
  private synchronized InputVFS getSectionVFS(@Nonnull FileType fileType) {
    InputVFS currentSectionVFS;
    if (sectionVFS.containsKey(fileType)) {
      currentSectionVFS = sectionVFS.get(fileType);
    } else {
      VFS prefixedInputVFS = null;
      try {
        prefixedInputVFS = new PrefixedFS(vfs, new VPath(fileType.getPrefix(), '/'));
      } catch (CannotCreateFileException e) {
        // If library is well formed this exception can not be triggered
        throw new AssertionError(e);
      } catch (NotDirectoryException e) {
        // If library is well formed this exception can not be triggered
        throw new AssertionError(e);
      }
      if (fileType == FileType.DEX) {
        try {
          currentSectionVFS = new GenericInputVFS(new MessageDigestFS(prefixedInputVFS,
              ThreadConfig.get(JackLibraryFactory.MESSAGE_DIGEST_ALGO)));
        } catch (WrongVFSFormatException e) {
          // If library is well formed this exception can not be triggered
          throw new AssertionError(e);
        }
      } else {
        currentSectionVFS = new GenericInputVFS(prefixedInputVFS);
      }
      sectionVFS.put(fileType, currentSectionVFS);
    }
    return currentSectionVFS;
  }

  @Override
  public synchronized void close() throws LibraryIOException {
    try {
      for (InputVFS currentSectionVFS : sectionVFS.values()) {
        currentSectionVFS.close();
      }
      vfs.close();
    } catch (IOException e) {
      throw new LibraryIOException(getLocation(), e);
    }
  }

  @Override
  @Nonnegative
  public int getMinorVersion() {
    return minorVersion;
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
      InputVFS currentSectionVFS = getSectionVFS(fileType);
      currentSectionVFS.getRootInputVDir().delete(buildFileVPath(fileType, typePath));
    } catch (NotFileOrDirectoryException e) {
      throw new FileTypeDoesNotExistException(getLocation(), typePath, fileType);
    } catch (NoSuchFileException e) {
      throw new FileTypeDoesNotExistException(getLocation(), typePath, fileType);
    }
  }

  @Override
  @Nonnull
  public String getPath() {
    return vfs.getPath();
  }

  @Nonnull
  public VPath buildFileVPath(@Nonnull FileType fileType, @Nonnull VPath vpath) {
    VPath clonedPath = vpath.clone();
    clonedPath.addSuffix(fileType.getFileExtension());
    return clonedPath;
  }

  @Override
  @CheckForNull
  public String getDigest() {
    if (!containsFileType(FileType.DEX)) {
      return null;
    } else {
      return getSectionVFS(FileType.DEX).getDigest();

    }
  }
}
