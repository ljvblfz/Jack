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

import com.google.common.collect.ImmutableSet;

import com.android.jack.analysis.dependency.Dependency;
import com.android.jack.backend.dex.DexFileWriter;
import com.android.jack.backend.jayce.JayceFileImporter;
import com.android.jack.library.FileType;
import com.android.jack.library.FileTypeDoesNotExistException;
import com.android.jack.library.InputJackLibrary;
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

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Jack library used as input.
 */
public class InputJackLibraryImpl extends InputJackLibrary {

  @Nonnull
  private static final VPath RSC_PREFIX = new VPath("rsc", '/');

  @Nonnull
  private static final VPath DEPENDENCY_PREFIX = new VPath("dependencies", '/');

  @Nonnull
  private static final VPath LOG_PREFIX = new VPath("logs", '/');

  @Nonnull
  private static final VPath JPP_PREFIX = new VPath("jpp", '/');

  @Nonnull
  private static final VPath JAYCE_PREFIX = new VPath("jayce", '/');

  @Nonnull
  private static final VPath DEX_PREFIX = new VPath("dex", '/');

  @Nonnull
  private final Map<FileType, InputVFS> sectionVFS =
      new EnumMap<FileType, InputVFS>(FileType.class);

  public InputJackLibraryImpl(@Nonnull VFS vfs,
      @Nonnull Properties libraryProperties) throws LibraryVersionException,
      LibraryFormatException {
    super(libraryProperties, vfs);
    this.vfs = vfs;

    check();
    fillFileTypes();
  }

  @Override
  @Nonnull
  public InputVFile getFile(@Nonnull FileType fileType, @Nonnull VPath typePath)
      throws FileTypeDoesNotExistException {
    if (!containsFileType(fileType)) {
      // Even if the file exists, it is not accessible because fileType does not belong to the
      // library, thus do not return it.
      throw new FileTypeDoesNotExistException(getLocation(), typePath, fileType);
    }
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
      return ImmutableSet.<InputVFile>of().iterator();
    }

    List<InputVFile> inputVFiles = new ArrayList<InputVFile>();
    fillFiles(getSectionVFS(fileType).getRootInputVDir(), inputVFiles);
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
        prefixedInputVFS = new PrefixedFS(vfs, getSectionPath(fileType));
      } catch (CannotCreateFileException e) {
        // If library is well formed this exception can not be triggered
        throw new AssertionError(e);
      } catch (NotDirectoryException e) {
        // If library is well formed this exception can not be triggered
        throw new AssertionError(e);
      }
      if (fileType == FileType.PREBUILT) {
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
      currentSectionVFS.getRootInputVDir().getInputVFile(buildFileVPath(fileType, typePath))
          .delete();
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
    clonedPath.addSuffix(getExtension(fileType));
    return clonedPath;
  }

  @Override
  @CheckForNull
  public String getDigest() {
    if (!containsFileType(FileType.PREBUILT)) {
      return null;
    } else {
      return getSectionVFS(FileType.PREBUILT).getDigest();

    }
  }

  @Override
  @Nonnull
  protected String getPropertyPrefix(@Nonnull FileType type) {
    switch (type) {
      case PREBUILT:
        return "dex";
      case JAYCE:
        return "jayce";
      case META:
        return "jpp";
      case LOG:
        return "logs";
      case DEPENDENCIES:
        return "dependencies";
      case RSC:
        return "rsc";
      default:
        throw new AssertionError();
    }
  }

  @Nonnull
  private static String getExtension(@Nonnull FileType type) {
    switch (type) {
      case PREBUILT:
        return DexFileWriter.DEX_FILE_EXTENSION;
      case JAYCE:
        return JayceFileImporter.JAYCE_FILE_EXTENSION;
      case LOG:
        return ".log";
      case DEPENDENCIES:
        return Dependency.DEPENDENCY_FILE_EXTENSION;
      default:
        return "";
    }
  }
  @Nonnull
  private static VPath getSectionPath(@Nonnull FileType type) {
    switch (type) {
      case PREBUILT:
        return DEX_PREFIX;
      case JAYCE:
        return JAYCE_PREFIX;
      case META:
        return JPP_PREFIX;
      case LOG:
        return LOG_PREFIX;
      case DEPENDENCIES:
        return DEPENDENCY_PREFIX;
      case RSC:
        return RSC_PREFIX;
      default:
        throw new AssertionError();
    }
  }

  @Override
  public boolean hasCompliantPrebuilts() {
    return true;
  }
}
