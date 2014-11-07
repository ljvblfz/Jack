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

import com.android.jack.Jack;
import com.android.jack.library.BinaryDoesNotExistException;
import com.android.jack.library.BinaryKind;
import com.android.jack.library.InputJackLibrary;
import com.android.jack.library.InputLibrary;
import com.android.jack.library.InputLibraryLocation;
import com.android.jack.library.LibraryFormatException;
import com.android.jack.library.LibraryVersionException;
import com.android.jack.library.NotBinaryException;
import com.android.sched.util.file.NotFileOrDirectoryException;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.vfs.InputRootVDir;
import com.android.sched.vfs.InputVDir;
import com.android.sched.vfs.InputVElement;
import com.android.sched.vfs.InputVFile;
import com.android.sched.vfs.VPath;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * Jack library used as input.
 */
public class InputJackLibraryImpl extends InputJackLibrary {

  @Nonnull
  private static Logger logger = LoggerFactory.getLogger();

  @Nonnull
  private final InputRootVDir libraryVDir;

  @Nonnull
  private final InputLibraryLocation location = new InputLibraryLocation() {

    @Override
    @Nonnull
    public String getDescription() {
      return libraryVDir.getLocation().getDescription();
    }

    @Override
    public int hashCode() {
      return InputJackLibraryImpl.this.hashCode();
    }

    @Override
    @Nonnull
    public InputLibrary getInputLibrary() {
      return InputJackLibraryImpl.this;
    }

    @Override
    public boolean equals(Object obj) {
      return obj instanceof InputLibraryLocation
          && ((InputLibraryLocation) obj).getInputLibrary().equals(getInputLibrary());
    }
  };

  @Nonnull
  private final Set<BinaryKind> binaryKinds = new HashSet<BinaryKind>(1);

  public InputJackLibraryImpl(@Nonnull InputRootVDir libraryVDir,
      @Nonnull Properties libraryProperties) throws LibraryVersionException,
      LibraryFormatException {
    super(libraryProperties);
    this.libraryVDir = libraryVDir;
    check();
    fillBinaryKinds(libraryVDir);
  }

  @Override
  @Nonnull
  public InputLibraryLocation getLocation() {
    return location;
  }

  @Override
  @Nonnull
  public Collection<BinaryKind> getBinaryKinds() {
    return Jack.getUnmodifiableCollections().getUnmodifiableCollection(binaryKinds);
  }

  @Override
  public boolean hasBinary(@Nonnull BinaryKind binaryKind) {
    return binaryKinds.contains(binaryKind);
  }

  @Override
  @Nonnull
  public List<InputVFile> getBinaries(@Nonnull BinaryKind binaryKind) {
    List<InputVFile> binaries = new ArrayList<InputVFile>();
    fillBinaries(libraryVDir, binaryKind, binaries);
    return binaries;
  }

  @Override
  @Nonnull
  public InputVFile getBinary(@Nonnull VPath typePath, @Nonnull BinaryKind binaryKind)
      throws BinaryDoesNotExistException {
    try {
      return libraryVDir.getInputVFile(
          new VPath(typePath.getPathAsString('/') + BinaryKind.DEX.getFileExtension(), '/'));
    } catch (NotFileOrDirectoryException e) {
      throw new BinaryDoesNotExistException(getLocation(), typePath, binaryKind);
    }
  }

  @Override
  @Nonnull
  public InputRootVDir getInputVDir() {
    return libraryVDir;
  }

  private void fillBinaryKinds(@Nonnull InputVDir vDir) {
    for (InputVElement subFile : vDir.list()) {
      if (subFile.isVDir()) {
        fillBinaryKinds((InputVDir) subFile);
      } else {
        try {
          binaryKinds.add(BinaryKind.getBinaryKind((InputVFile) subFile));
        } catch (NotBinaryException e) {
          // Ok, nothing to do
        }
      }
    }
  }

  private void fillBinaries(@Nonnull InputVDir vDir, @Nonnull BinaryKind binaryKind,
      @Nonnull List<InputVFile> binaries) {
    for (InputVElement subFile : vDir.list()) {
      if (subFile.isVDir()) {
        fillBinaries((InputVDir) subFile, binaryKind, binaries);
      } else {
        InputVFile vFile = (InputVFile) subFile;
        if (binaryKind.isBinaryFile(vFile)) {
          binaries.add(vFile);
        }
      }
    }
  }

  @Override
  public int getMinorVersion() throws LibraryFormatException {
    int minor;
    try {
      minor = Integer.parseInt(getProperty(KEY_LIB_MINOR_VERSION));
    } catch (NumberFormatException e) {
      logger.log(Level.SEVERE, "Fails to parse the property " + KEY_LIB_MINOR_VERSION
          + " from " + getLocation().getDescription(), e);
      throw new LibraryFormatException(getLocation());
    }
    return minor;
  }

  @Override
  public int getMajorVersion() {
    return Version.MAJOR;
  }

  @Override
  public int getSupportedMinorMin() {
    return Version.MINOR_MIN;
  }

  @Override
  public int getSupportedMinor() {
    return Version.MINOR;
  }
}
